/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sos.insert.uuid.handler;

import messages.SensorNannyException;
import java.io.IOException;
import java.io.StringReader;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import messages.SensorNannyMessages;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Sax handler to extract uuid from SensorML, this is a singleton.
 * It offers a synchronized method to get uuid from SensorML content file, this method is call one after the other
 * because SAX parsing is not thread safe and The sos server is a servlet which uses threads.<br>
 * eg : retreive 0f088e5f-e0ad-4936-9024-7b5c9a552b0a from &lt;sml:value&gt;0f088e5f-e0ad-4936-9024-7b5c9a552b0a&lt;/sml:value&gt; in xml :<br>
 * <br>
 *&lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;<br>
 *&lt;sml:PhysicalSystem <br>
 *    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"<br> 
 *    xmlns:gmd="http://www.isotc211.org/2005/gmd" <br>
 *    xmlns:gco="http://www.isotc211.org/2005/gco"<br>
 *    xmlns:sml="http://www.opengis.net/sensorml/2.0" <br>
 *    xmlns:xlink="http://www.w3.org/1999/xlink" <br>
 *    xmlns:gml="http://www.opengis.net/gml/3.2" <br>
 *    xmlns:swe="http://www.opengis.net/swe/2.0" <br>   
 *    xsi:schemaLocation="http://www.opengis.net/sensorml/2.0 http://schemas.opengis.net/sensorML/2.0/sensorML.xsd"<br>
 *    gml:id="top"<br>
 *   &gt;<br>
 *            &lt;!-- ##### TITLE from deployment file ######## --&gt;<br>
 *            &lt;gml:description&gt;IFREMER Research Vessels&lt;/gml:description&gt;<br>
 *            &lt;gml:name&gt;IFREMER Research Vessels&lt;/gml:name&gt;<br>
 *    <br>
 *            &lt;sml:identification&gt;<br>
 *                &lt;sml:IdentifierList&gt;<br>
 *                    &lt;!-- identifiant de l'enregistrement de métadonnées en cours, UUID invariant le long du flow d'information --&gt;<br>
 *                    &lt;!-- le codeSpace permet de voir la liste des uuid utilisable dans ce contexte                             --&gt;<br>                   
 *                    &lt;sml:identifier&gt;<br>
 *                        &lt;sml:Term definition="http://www.ifremer.fr/tematres/vocab/index.php?tema=66"&gt;<br>
 *                            &lt;sml:label&gt;uuid&lt;/sml:label&gt;<br>
 *                            &lt;sml:codeSpace xlink:href="http://ubisi54.ifremer.fr/cgi-bin/sos.py?request=getCapabilities"/&gt;<br>
 *                            &lt;sml:value&gt;0f088e5f-e0ad-4936-9024-7b5c9a552b0a&lt;/sml:value&gt;<br>
 *                        &lt;/sml:Term&gt;
 *                    &lt;/sml:identifier&gt; 
 *                    .....
 *
 * @author mlarour
 */
public class SensorMlUuid extends DefaultHandler
{
    public static String NORMAL_STATUS = "OK";
    
    
    // xml tags
    private static String PHYSICAL_SYSTEM = "sml:PhysicalSystem";
    private static String IDENTIFICATION = "sml:identification";
    private static String IDENTIFIER_LIST = "sml:IdentifierList";
    private static String IDENTIFIER = "sml:identifier";
    private static String TERM = "sml:Term";
    private static String LABEL = "sml:label";
    private static String LABEL_EXPECTED = "uuid";
    private static String VALUE = "sml:value";
    
    // xml booleans
    private boolean physicalSystemOn;
    private boolean identificationOn;
    private boolean identifierListOn;
    private boolean identifierOn;     
    private boolean termOn;
    private boolean labelOn;    
    private boolean valueOn;
    
    private  SAXParser parser;
    
    private String label;    
    private String uuid;    
    private StringBuilder buffer;
       
    private static SensorMlUuid sensorMlUuid = null;
    
    
    /** private constructor, this class is a singleton
     * @throws SensorNannyException if sax parser can't be build
     */
    protected SensorMlUuid() throws SensorNannyException
    {
        super();
        buffer = new StringBuilder();
        reset();
        try
        {
           parser = SAXParserFactory.newInstance().newSAXParser();
        }
        catch(ParserConfigurationException|SAXException ex)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_SAXPARSER_UUID,Status.SERVICE_UNAVAILABLE);
        } 
    }
    
    /** Singleton getter
     * 
     * @return the unique instance
     */
    public synchronized static SensorMlUuid singleton()
    {
        if(sensorMlUuid == null)
        {
           sensorMlUuid = new SensorMlUuid();
        }
        return(sensorMlUuid);
    } 
    /** initialization before a next sax parsing
     * 
     */
    private void reset()
    {
        physicalSystemOn =  false;
        identificationOn =  false;
        identifierListOn =  false;
        identifierOn =  false;     
        termOn =  false;
        labelOn =  false;
        valueOn =  false;
        
        label = null;
        uuid = null;
        
        buffer.delete(0,buffer.length());
    }
            
    /** Getter for SensorML uuid from SensorML content file
     * 
     * @param xml valid SensorML xml
     * @return the SensorML uuid
     */
    public synchronized String getUuid(String xml)
    {
        try
        {           
           InputSource is = new InputSource(new StringReader(xml));           
           sensorMlUuid.reset();           
           parser.parse(is,sensorMlUuid);
        }
        catch(SAXException ex)
        {
            if(ex.getMessage().compareTo(NORMAL_STATUS) == 0)
            {
                return(uuid);
            }
            throw new SensorNannyException(SensorNannyMessages.ERROR_PARSE_UUID,Status.BAD_REQUEST);
        }
        catch(IOException ex)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_IO_UUID,Status.SERVICE_UNAVAILABLE);
        }
        throw new SensorNannyException(SensorNannyMessages.ERROR_PARSE_UUID,Status.BAD_REQUEST);
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
        if(qName.compareTo(PHYSICAL_SYSTEM) == 0)
        {
            physicalSystemOn = true;
        }
        else if(physicalSystemOn)
        {
            if(qName.compareTo(IDENTIFICATION) == 0)
            {
                identificationOn = true;
            }
            else if(identificationOn)
            {
                if(qName.compareTo(IDENTIFIER_LIST) == 0)
                {
                    identifierListOn = true;
                }
                else if(identifierListOn)
                {
                    if(qName.compareTo(IDENTIFIER) == 0)
                    {
                        identifierOn = true;
                    }
                    else if(identifierOn)
                    {
                        if(qName.compareTo(TERM) == 0)
                        {
                            termOn = true;
                        }
                        else if(termOn)
                        {
                            if(qName.compareTo(LABEL) == 0)
                            {
                                labelOn = true;
                                buffer.delete(0,buffer.length());
                            }
                            else if(qName.compareTo(VALUE) == 0)
                            {                                
                                valueOn = true;
                                buffer.delete(0,buffer.length());
                            }
                        }
                    }
                } 
            }
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
        if(labelOn || valueOn)
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
        
        if(qName.compareTo(PHYSICAL_SYSTEM) == 0)
        {
            physicalSystemOn = false;
        }
        else if(physicalSystemOn)
        {
            if(qName.compareTo(IDENTIFICATION) == 0)
            {
                identificationOn = false;
            }
            else if(identificationOn)
            {
                if(qName.compareTo(IDENTIFIER_LIST) == 0)
                {
                    identifierListOn = false;
                }
                else if(identifierListOn)
                {
                    if(qName.compareTo(IDENTIFIER) == 0)
                    {
                        identifierOn = false;
                    }
                    else if(identifierOn)
                    {
                        if(qName.compareTo(TERM) == 0)
                        {
                            termOn = false;
                            label=null;
                            uuid=null;
                            labelOn = false;
                            valueOn = false;
                        }
                        else if(termOn)
                        {
                            if(qName.compareTo(LABEL) == 0)
                            {
                                if(labelOn)
                                {
                                    if(buffer.toString().compareTo(LABEL_EXPECTED) == 0)
                                    {
                                        label=LABEL_EXPECTED;
                                        if(uuid != null)
                                        {
                                            throw new SAXException(NORMAL_STATUS);
                                        }
                                    }
                                }
                                labelOn = false;                                
                            }
                            else if(qName.compareTo(VALUE) == 0)
                            {
                                if(valueOn)
                                {
                                    uuid = buffer.toString();
                                    if(label != null)
                                    {
                                        throw new SAXException(NORMAL_STATUS);
                                    }
                                }
                                valueOn = false;
                            }
                        }
                    }
                } 
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        throw new SAXException(SensorNannyMessages.ERROR_PARSE_UUID);
    }

    
}
