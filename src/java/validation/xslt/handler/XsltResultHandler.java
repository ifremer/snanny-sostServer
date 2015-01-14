/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package validation.xslt.handler;

import messages.SnannySostServerException;
import messages.SnannySostServerMessages;
import java.io.IOException;
import java.io.StringReader;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Manage XSLT results for schematron validation via XSL.
 * This class is also a SAX handler for extracting the validation errors,
 * the analyse method is synchronized, because SAX parsing is not thread safe and
 * The sos server is a servlet which uses threads
 * @author mlarour
 */
public class XsltResultHandler extends DefaultHandler
{
    private static SAXParser parser;    
    private static StringBuilder buffer;
    
    // xml tags
    private static String FAILED = "svrl:failed-assert";
    private boolean failed;
    
    public static String NORMAL_STATUS = "OK";
    
    // singleton
    private static XsltResultHandler xsltResultHandler = null;
    
    /** Singleton getter.
     * 
     * @return the unique instance
     */
    public static synchronized XsltResultHandler singleton()
    {        
        if(xsltResultHandler == null)
        {
            xsltResultHandler = new XsltResultHandler();           
        }
        return(xsltResultHandler);
    }
    
    /** private constructor, this class is a singleton.
     * 
     */
    private XsltResultHandler()
    {
        super();
        buffer = new StringBuilder();
        try
        {
           parser = SAXParserFactory.newInstance().newSAXParser();
        }
        catch(ParserConfigurationException|SAXException ex)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_SAXPARSER_XSLT,Status.SERVICE_UNAVAILABLE);
        }        
    }
    
    /** initialization before a next sax parsing
     * 
     */
    private void reset()
    {
        buffer.delete(0,buffer.length());
        failed = false;
    }
    
    /** Extract errors from xml resulting of schematron validation via xslt.
     * 
     * @param xsltResult xml resulting of schematron validation via xslt
     * @return null if xml is schematron valid, errors elsewhere
     */
    public synchronized String analyse(String xsltResult)
    {
        try
        {
           InputSource is = new InputSource(new StringReader(xsltResult));
           xsltResultHandler.reset();
           parser.parse(is,xsltResultHandler);
        }
        catch(SAXException ex)
        {
            if(ex.getMessage().compareTo(NORMAL_STATUS) == 0)
            {
                return(buffer.toString());
            }
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_PARSE_XSLT,Status.BAD_REQUEST);
        }
        catch(IOException ex)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_IO_XSLT,Status.SERVICE_UNAVAILABLE);
        } 
        return(null);
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
        if(qName.compareTo(FAILED) == 0)
        {
            failed = true;
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
            buffer.append('>');
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
        if(failed)
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
        if(qName.compareTo(FAILED) == 0)
        {
            if(failed)
            {
                buffer.append("</");
                buffer.append(qName);
                buffer.append(">");
                throw new SAXException(NORMAL_STATUS);
            }            
        }
    }
    
}
