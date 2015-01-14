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
import messages.SnannySostServerException;
import messages.SnannySostServerMessages;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import messages.Success;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONML;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import sos.Sos;
import sos.SosFormat;
import sos.SosFormat.SOSFORMAT;
import sos.couchbase.CouchbaseManager;
import sos.insert.uuid.handler.SensorMlUuid;
import validation.SosValidation;

/** Manage sensor insertion, check format and store in sos server, this is a singleton.
 * This class is also a SAX handler for extracting the content file (the sensor) 
 * from the POST content. The insert method is synchronized, the insertions are done
 * one after the other because SAX parsing is not thread safe.
 * The sos server is a servlet which uses threads, SAX parsing is fast but for
 * schematron validation I convert it to xsl and then use xslt : this phase is really slow,
 * so the idea of singleton for schematron validation should be revised.
 *
 * @author mlarour
 */
public class InsertSensor extends  DefaultHandler
{
    private static final String TYPE = "SensorMl";
        
    // xml tags
    private static String INSERT_SENSOR = "swes:InsertSensor";
    private static String FORMAT        = "swes:procedureDescriptionFormat";
    private static String PROCEDURE     = "swes:procedureDescription";
    private static String OBSERVABLE_PROPERTY = "swes:observableProperty";
    
    // xml booleans
    boolean insertSensorOn;
    boolean formatOn;
    boolean procedureOn;
    boolean observablePropertyOn;
    
    // parse variables
    private StringBuilder buffer;        
    private String sensorML;
    private String format;
    
    private SAXParser postParser;
    
    // singleton
    private static InsertSensor insertSensor = null;
        
    private JsonArrayTranscoder jsonArrayTranscoder = null;
    private JsonTranscoder jsonTranscoder = null;
    
    
    /** private constructor, this class is a singleton
     * 
     * @throws SnannySostServerException if sax parser can't be build 
     */
    private InsertSensor() throws SnannySostServerException
    {
        super();
        buffer = new StringBuilder();
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
    public static synchronized InsertSensor singleton()
    {
        if(insertSensor == null)
        {
            insertSensor = new InsertSensor();
            SensorMlUuid.singleton();
        }
        return(insertSensor);
    } 
    /** initialization before a next sax parsing
     * 
     */
    private void reset()
    {
        insertSensorOn = false;
        formatOn = false;
        procedureOn = false;
        observablePropertyOn = false;
        
        format="";
        sensorML="";
        
        buffer.delete(0,buffer.length());
    }        
    
    /** Check format, convert from JSON to XML as needed, insert sensor in sos server
     * 
     * @param snannySostServerConfig the sos server configuration
     * @param contentPost the content post with sensor in JSON or XML format
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
        String uuid = "";
        insertSensor.reset();
        // parse post content
        try
        {   
            InputSource is = new InputSource(new StringReader(contentPost));
            postParser.parse(is,insertSensor);         
        }
        catch(SAXException ex)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_PARSE_INSERT,Status.BAD_REQUEST);
        }
        catch(IOException ex)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_IO_INSERT,Status.SERVICE_UNAVAILABLE);
        }
        
        // if json content convert it to xml                        
        if(format.isEmpty())
        {
           throw new SnannySostServerException(SnannySostServerMessages.ERROR_POST_FORMAT_REQUIRED+SosFormat.getAvailableSosResponseFormats(),Status.BAD_REQUEST);
        }
        SOSFORMAT sosformat = SosFormat.getFormat(format);
        try
        {
            switch(sosformat)
            {
                
                case JSON : if(snannySostServerConfig.isJsonObject())
                            {
                                JSONObject jSONOject = new JSONObject(StringEscapeUtils.unescapeXml(sensorML));
                                sensorML = JSONML.toString(jSONOject);
                            }
                            else
                            {
                                JSONArray jsonArray = new JSONArray(StringEscapeUtils.unescapeXml(sensorML));
                                sensorML = JSONML.toString(jsonArray);
                            }
                            break;

                case XML :  break;    
            }
        }
        catch(Exception ex)
        {
           throw new SnannySostServerException(SnannySostServerMessages.ERROR_JSON_INSERT+ex.getMessage(),Status.BAD_REQUEST);         
        }       
        // validate xml
        SosValidation.singleton(snannySostServerConfig).xsdValidateSensorMl(sensorML);
        SosValidation.singleton(snannySostServerConfig).schematronValidateSensorMl(sensorML);
        
        uuid = SensorMlUuid.singleton().getUuid(sensorML);
        if(snannySostServerConfig.isCouchbase())
        {  
            try
            {    
                JsonObject jsonObject = JsonObject.empty();
                if(snannySostServerConfig.isJsonObject())
                {
                    jsonObject.put(SnannySostServerConfig.FilejsonField,jsonTranscoder.stringToJsonObject(JSONML.toJSONObject(sensorML).toString()));                    
                }
                else
                {
                    jsonObject.put(SnannySostServerConfig.FilejsonField,jsonArrayTranscoder.stringToJsonArray(JSONML.toJSONArray(sensorML).toString()));
                }                                 
                jsonObject.put(SnannySostServerConfig.AuthorNameField,SnannySostServerConfig.AuthorNameValue);
                CouchbaseManager.getSystemBucket().upsert(JsonDocument.create(uuid,jsonObject),
                                                          snannySostServerConfig.getCouchbaseTimeOutMS(),
                                                          TimeUnit.MILLISECONDS);
                Sos.SAMPLE_SYSTEM_UUID = uuid;
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
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(snannySostServerConfig.newSensorMlFile(uuid)),snannySostServerConfig.getCharset());
                outputStreamWriter.write(sensorML);
                outputStreamWriter.close();            
            }
            catch(IOException ex)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_STORE_INSERT,Status.SERVICE_UNAVAILABLE);
            }
        }
        Success.submit(SnannySostServerMessages.IMPORT_SENSOR_OK_1of2+uuid+SnannySostServerMessages.IMPORT_SENSOR_OK_2of2,response,out,snannySostServerConfig);        
    }
    
    /** SAX handling, Receive notification of the start of an element. 
     * 
     * @param namespaceURI The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
     * @param atts The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
        if(qName.compareTo(INSERT_SENSOR) == 0)
        {
            insertSensorOn = true;
        }
        else if(insertSensorOn)
        {
            if(qName.compareTo(FORMAT) == 0)
            {
                if(procedureOn || observablePropertyOn)
                {
                    throw new SnannySostServerException(SnannySostServerMessages.ERROR_XML_INSERT+qName,Status.BAD_REQUEST);
                }
                formatOn = true;
                format = "";
                buffer.delete(0,buffer.length());
            }
            else if(qName.compareTo(PROCEDURE) == 0)
            {
                if(formatOn || observablePropertyOn)
                {
                    throw new SnannySostServerException(SnannySostServerMessages.ERROR_XML_INSERT+qName,Status.BAD_REQUEST);
                }
                procedureOn = true;
                sensorML = "";
                buffer.delete(0,buffer.length());
            }
            else if(qName.compareTo(OBSERVABLE_PROPERTY) == 0)
            {
                if(formatOn || procedureOn)
                {
                    throw new SnannySostServerException(SnannySostServerMessages.ERROR_XML_INSERT+qName,Status.BAD_REQUEST);
                }
                observablePropertyOn = true;
            }
            else if(procedureOn)
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
        if(procedureOn)
        {
            buffer.append(org.json.XML.escape(new String(ch,start,length)));
        }  
        else if(formatOn)
        {
            buffer.append(ch,start,length);
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
        if(qName.compareTo(INSERT_SENSOR) == 0)
        {            
            insertSensorOn = false;
        }
        else if(insertSensorOn)
        {
            if(qName.compareTo(FORMAT) == 0)
            {
                if(procedureOn || observablePropertyOn)
                {
                    throw new SnannySostServerException(SnannySostServerMessages.ERROR_XML_INSERT+qName,Status.BAD_REQUEST);
                }
                formatOn = false;
                format = buffer.toString();
            }
            else if(qName.compareTo(PROCEDURE) == 0)
            {
                if(formatOn || observablePropertyOn)
                {
                    throw new SnannySostServerException(SnannySostServerMessages.ERROR_XML_INSERT+qName,Status.BAD_REQUEST);
                }
                procedureOn = false;
                sensorML = buffer.toString();
            }
            else if(qName.compareTo(OBSERVABLE_PROPERTY) == 0)
            {
                if(formatOn || procedureOn)
                {
                    throw new SnannySostServerException(SnannySostServerMessages.ERROR_XML_INSERT+qName,Status.BAD_REQUEST);
                }
                observablePropertyOn = false;
            }
            else if(procedureOn)
            {
                buffer.append("</");
                buffer.append(qName);
                buffer.append(">");
            }
        }
        else
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_XML_INSERT+qName,Status.BAD_REQUEST);
        }
    }
}
