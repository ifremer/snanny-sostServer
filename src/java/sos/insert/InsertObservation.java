/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sos.insert;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.transcoder.JsonArrayTranscoder;
import com.couchbase.client.java.transcoder.JsonTranscoder;
import config.SnannySostServerConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import messages.SnannySostServerException;
import messages.SnannySostServerMessages;
import messages.Success;
import org.json.JSONML;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import sos.Sos;
import sos.couchbase.CouchbaseManager;
import sos.insert.uuid.handler.SensorMLOemUuid;
import sos.insert.uuid.handler.Uuids;
import validation.SosValidation;

/** Manage observations insertion, check format and store in sos server, this is a singleton.
 * This class is also a SAX handler for extracting the content files (the observations) 
 * from the POST content. The insert method is synchronized, the insertions are done
 * one after the other because SAX parsing is not thread safe.
 * The sos server is a servlet which uses threads, SAX parsing is fast but for
 * schematron validation I convert it to xsl and then use xslt : this phase is really slow,
 * so the idea of singleton for schematron validation should be revised.
 * @author mlarour
 */
public class InsertObservation extends  DefaultHandler
{
    
    // xml tags
    private static String INSERT_OBSERVATION  = "sos:InsertObservation";
    private static String OFFERING            = "sos:offering";
    private static String OBSERVATION         = "sos:observation";
    
    // xml booleans
    boolean insertObservationOn;
    boolean offeringOn;            
    boolean observationOn;
    
    
    private static InsertObservation insertObservation = null;
    
    // parse variables
    private StringBuilder buffer;        
    private ArrayList<String> oems;
    
    private SAXParser postParser;
      
    private JsonArrayTranscoder jsonArrayTranscoder = null;
    private JsonTranscoder jsonTranscoder = null;
    
    /** private constructor, this class is a singleton
     * @throws SnannySostServerException if sax parser can't be build
     */    
    private InsertObservation() throws SnannySostServerException
    {
        super();        
        buffer = new StringBuilder();
        oems = new ArrayList<>();
        reset();
        try
        {
           SAXParserFactory spf = SAXParserFactory.newInstance();
           //spf.setNamespaceAware(true);
           postParser = spf.newSAXParser();           
        }
        catch(ParserConfigurationException|SAXException ex)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_SAXPARSER_INSERT,Status.SERVICE_UNAVAILABLE);
        } 
        jsonArrayTranscoder = new JsonArrayTranscoder();
        jsonTranscoder = new JsonTranscoder();
    }
    
    
    /** Singleton getter
     * 
     * @return the unique instance
     */
    public static synchronized InsertObservation singleton()
    {
        if(insertObservation == null)
        {

            insertObservation = new InsertObservation();
            SensorMLOemUuid.singleton();
        }
        return(insertObservation);
    }
    
    /** initialization before a next sax parsing
     * 
     */
    private void reset()
    {
        buffer.delete(0,buffer.length());
        oems.clear();
        insertObservationOn = false;
        offeringOn          = false;            
        observationOn       = false;
    }
    
    /** check observations format and insert observations in sos server
     * 
     * @param snannySostServerConfig the sos server configuration
     * @param contentPost the content post with one or more observations
     * @param response Http Servlet response
     * @param out PrintWriter for Http Servlet Response
     * @param sampleBean
     * @throws SnannySostServerException if insertion failed
     */
    public synchronized void insert(SnannySostServerConfig snannySostServerConfig,
                                    String contentPost,
                                    HttpServletResponse response,
                                    PrintWriter out) throws SnannySostServerException
    {
        Uuids uuids;
        insertObservation.reset();
        try
        {   
            InputSource is = new InputSource(new StringReader(contentPost));
            postParser.parse(is,insertObservation);
        }
        catch(SAXException ex)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_PARSE_INSERT,Status.BAD_REQUEST);
        }
        catch(IOException ex)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_IO_INSERT,Status.SERVICE_UNAVAILABLE);
        }
        
        for(String oem : oems)
        {
            // validate xml
            SosValidation.singleton(snannySostServerConfig).xsdValidateOem(oem);
            SosValidation.singleton(snannySostServerConfig).schematronValidateOem(oem);
            uuids = SensorMLOemUuid.singleton().getUuids(oem);
            if(snannySostServerConfig.isCouchbase())
            {
                try
                {    
                    JsonObject jsonObject = JsonObject.empty();                                             
                    if(snannySostServerConfig.isJsonObject())
                    {
                       jsonObject.put(SnannySostServerConfig.FilejsonField,jsonTranscoder.stringToJsonObject(JSONML.toJSONObject(oem).toString()));
                    }
                    else
                    {
                       jsonObject.put(SnannySostServerConfig.FilejsonField,jsonArrayTranscoder.stringToJsonArray(JSONML.toJSONArray(oem).toString()));                    
                    }
                    jsonObject.put(SnannySostServerConfig.AuthorNameField,SnannySostServerConfig.AuthorNameValue);
                    CouchbaseManager.getObservationBucket().upsert(JsonDocument.create(uuids.getOeMuuid(),jsonObject),
                                                                   snannySostServerConfig.getCouchbaseTimeOutMS(),
                                                                   TimeUnit.MILLISECONDS);
                    Sos.SAMPLE_OBSERVATION_UUID = uuids.getOeMuuid();
                }
                catch(Exception ex)
                {
                    throw new SnannySostServerException(SnannySostServerMessages.ERROR_COUCHBASE_ERROR+ex.getMessage(),Status.SERVICE_UNAVAILABLE);
                }
            }
            else
            {
            // write xml
                try 
                {
                    
                    File dir = snannySostServerConfig.newSensorMlDir(uuids);
                    if(!dir.isDirectory())
                    {
                        dir.mkdir();
                    }
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(snannySostServerConfig.newOemFile(uuids)),snannySostServerConfig.getCharset());
                    outputStreamWriter.write(oem);
                    outputStreamWriter.close();   
                }
                catch(IOException ex)
                {
                    throw new SnannySostServerException(SnannySostServerMessages.ERROR_STORE_INSERT,Status.SERVICE_UNAVAILABLE);
                }
            }
            if(snannySostServerConfig.isCouchbase())
            {
                Success.submit(uuids.getOeMuuid()+
                           SnannySostServerMessages.IMPORT_OBSERVATION_OK_3of3,response,out,snannySostServerConfig);
            }
            else
            {
                Success.submit(SnannySostServerMessages.IMPORT_OBSERVATION_OK_1of3+uuids.getSensorMLuuid()+
                           SnannySostServerMessages.IMPORT_OBSERVATION_OK_2of3+uuids.getOeMuuid()+
                           SnannySostServerMessages.IMPORT_OBSERVATION_OK_3of3,response,out,snannySostServerConfig);
            }
        
        }
        
        
        
        
        
    }
    
    /** SAX handling, Receive notification of the start of an element. 
     * 
     * @param namespaceURI  The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
     * @param atts The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
        if(qName.compareTo(INSERT_OBSERVATION) == 0)
        {
            insertObservationOn = true;
        }
        else if(insertObservationOn)
        {
            if(qName.compareTo(OFFERING) == 0)
            {
                // It is not managed here
            }
            else if(qName.compareTo(OBSERVATION) == 0)
            {
                buffer.delete(0,buffer.length());
                observationOn = true;
            }
            else if(observationOn)
            {
                buffer.append("<");
                buffer.append(qName);
                for(int i=0;i<atts.getLength();i++)
                {
                    buffer.append(" ");
                    buffer.append(atts.getQName(i));
                    buffer.append("=\"");
                    buffer.append(org.json.XML.escape(atts.getValue(i)));
                    buffer.append("\"");
                }
                buffer.append(">");
            }
        }
        else
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_XML_INSERT+qName,Status.BAD_REQUEST);
        }
    }
    /** SAX handling, Receive notification of character data inside an element. 
     * 
     * @param ch The characters.
     * @param start The start position in the character array.
     * @param length The number of characters to use from the character array.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if(observationOn)
        {
            buffer.append(org.json.XML.escape(new String(ch,start,length)));
        }
    }
    /** SAX handling, Receive notification of the end of an element. 
     * 
     * @param namespaceURI The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    { 

        if(qName.compareTo(OFFERING) == 0)
        {
            // It is not managed here
        }
        else if(insertObservationOn)
        {
            if(qName.compareTo(OBSERVATION) == 0)
            {
                oems.add(buffer.toString());
                observationOn = false;
            }
            else if(observationOn)
            {
                buffer.append("</");
                buffer.append(qName);
                buffer.append(">");
            }
        }
        else if(qName.compareTo(INSERT_OBSERVATION) != 0)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_XML_INSERT+qName,Status.BAD_REQUEST);
        }
    }
}
