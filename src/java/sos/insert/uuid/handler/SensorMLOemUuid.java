/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sos.insert.uuid.handler;

import java.io.IOException;
import java.io.StringReader;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import messages.SensorNannyException;
import messages.SensorNannyMessages;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import sos.SosProcedure;

/** Sax handler to extract uuids from O&amp;M, this is a singleton.
 * It offers a synchronized method to get uuids (one for SensorML, the other for O&amp;M)
 * from O&amp;M content file, this method is call one after the other
 * because SAX parsing is not thread safe and The sos server is a servlet which uses threads.<br>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;om:OM_Observation gml:id="top" 
 *	xmlns:om="http://www.opengis.net/om/2.0" 
 *	xmlns:gco="http://www.isotc211.org/2005/gco"
 *	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
 *	xmlns:xlink="http://www.w3.org/1999/xlink" 
 *	xmlns:gml="http://www.opengis.net/gml/3.2" 
 *	xmlns:swe="http://schemas.opengis.net/sweCommon/2.0/"
 *	xsi:schemaLocation="http://www.opengis.net/om/2.0 http://schemas.opengis.net/om/2.0/observation.xsd"&gt;
 *	&lt;gml:description&gt;CTD from vessel Thalassa during cruise PELGAS&lt;/gml:description&gt;
 *	&lt;gml:identifier codeSpace="uuid"&gt;8ee7be29-ce7d-43ed-8bdf-aad3a7bd8099&lt;/gml:identifier&gt;	
 *	&lt;gml:name&gt;CTD 24 on cruise PELGAS&lt;/gml:name&gt;
 *	    
 *			
 *	&lt;!-- location of the observation, for generic usage --&gt;			
 *	&lt;gml:boundedBy&gt;
 *	   &lt;gml:Envelope&gt;
 *	      &lt;gml:lowerCorner&gt;52.9 7.52&lt;/gml:lowerCorner&gt;
 *	      &lt;gml:upperCorner&gt;52.9 7.52&lt;/gml:upperCorner&gt;      						
 *	   &lt;/gml:Envelope&gt;				
 *	&lt;/gml:boundedBy&gt;
 *	
 *			
 *	&lt;!-- always : 	hhttp://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement	 --&gt; 
 *	&lt;om:type xlink:href="http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement"/&gt; 
 *			
 *	&lt;!-- may be used to point on CDI or on the observation collection metadata --&gt;
 *	&lt;om:metadata xlink:href="http://seadatanet.maris2.nl/v_cdi_v3/print_xml.asp?n_code=1977862"/&gt;							
 *			
 *	&lt;!-- temporal extent which the observation relates to, for profile ONE time value (begin=end) --&gt;
 *	&lt;om:phenomenonTime&gt;						 
 *				&lt;gml:TimePeriod gml:id="temporalExtent"&gt;
 *					&lt;gml:beginPosition&gt;2012-05-12T08:00:00&lt;/gml:beginPosition&gt;
 *					&lt;gml:endPosition&gt;2012-05-12T08:30:00&lt;/gml:endPosition&gt;
 *				&lt;/gml:TimePeriod&gt;				
 *	&lt;/om:phenomenonTime&gt;		
 *			
 *			&lt;!-- update time of the observation result --&gt;
 *			&lt;om:resultTime&gt;
 *				&lt;gml:TimeInstant  gml:id="updateDate"&gt;
 *					&lt;gml:timePosition&gt;2005-01-11T17:22:25.00&lt;/gml:timePosition&gt;
 *				&lt;/gml:TimeInstant&gt;
 *			&lt;/om:resultTime&gt;
 *			
 *			&lt;!-- descriptor of acquisition procedure which includes :                                      --&gt;
 *			&lt;!-- 	1) the observation system or sensors	(including its configuration or details valid  --&gt;
 *			&lt;!--       at the time of the observation				                                       --&gt;
 *			&lt;!--    2) the calibration, quality assesment method applied on result                         --&gt;
 *			&lt;!--        (different versions of post-processing may be detailed here)                       --&gt;	
 *			&lt;!-- for standard seadatanet obs, could point on EDIOS record                                  --&gt;
 *           &lt;om:procedure xlink:href="http://localhost:8080/sensornanny/record/0f088e5f-e0ad-4936-9024-7b5c9a552b0a" /&gt;
 * ....
 *				
 *
 * @author mlarour
 */
public class SensorMLOemUuid extends DefaultHandler
{
    public static String NORMAL_STATUS = "OK";
    
    // xml tags
    private static String ROOT = "om:OM_Observation";    
    // OEM_UUID_ATT attribute for OeM uuid in OEM_UUID_TAG tag (under root)
    private static String OEM_UUID_TAG = "gml:identifier";
    private static String OEM_UUID_ATT = "codeSpace";
    private static String OEM_UUID_ATT_VALUE = "uuid";
    
    // SML_UUID_ATT attribute for SensorML procedure in SML_PROCEDURE_TAG tag (under root)
    private static String SML_PROCEDURE_TAG = "om:procedure";
    private static String SML_PROCEDURE_ATT = "xlink:href";
    
    // xml booleans
    private boolean rootOn;            
    private boolean oemUuidTagOn;
    
    private StringBuilder buffer;
    private SAXParser parser;
    private Uuids uuids;
    
    // singleton
    private static SensorMLOemUuid sensorMLOemUuid = null;
    
    /** private constructor, this class is a singleton
     * @throws SensorNannyException if sax parser can't be build
     */
    protected SensorMLOemUuid()
    {
        super();
        buffer = new StringBuilder();
        uuids = new Uuids();
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
    public synchronized static SensorMLOemUuid singleton()
    {
        if(sensorMLOemUuid == null)
        {
           sensorMLOemUuid = new SensorMLOemUuid();
        }
        return(sensorMLOemUuid);
    }
    /** initialization before a next sax parsing
     * 
     */
    private void reset()
    {
        uuids.clear();
        buffer.delete(0,buffer.length());
        rootOn = false;
        oemUuidTagOn = false;        
    }
    
    public synchronized Uuids getUuids(String xml)
    {
        
        try
        {           
           InputSource is = new InputSource(new StringReader(xml));           
           sensorMLOemUuid.reset();           
           parser.parse(is,sensorMLOemUuid);
        }
        catch(SAXException ex)
        {
            if(ex.getMessage().compareTo(NORMAL_STATUS) == 0)
            {
                return(uuids);
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
        if(qName.compareTo(ROOT) == 0)
        {
            rootOn = true;
        }
        else if(qName.compareTo(SML_PROCEDURE_TAG) == 0)
        {
            uuids.setSensorMLuuid(SosProcedure.getUuid(atts.getValue(SML_PROCEDURE_ATT)));
            if(uuids.filled())
            {
                throw new SAXException(NORMAL_STATUS);
            }
        }
        else if(qName.compareTo(OEM_UUID_TAG) == 0)
        {
            if(atts.getValue(OEM_UUID_ATT).compareTo(OEM_UUID_ATT_VALUE) ==0)
            {
                oemUuidTagOn = true;
                buffer.delete(0,buffer.length());
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
        if(oemUuidTagOn)
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
        if(qName.compareTo(OEM_UUID_TAG) == 0)
        {
            oemUuidTagOn = false;
            uuids.setOeMuuid(buffer.toString());
            if(uuids.filled())
            {
                throw new SAXException(NORMAL_STATUS);
            }
        }
    }
}
