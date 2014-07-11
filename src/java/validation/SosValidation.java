/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package validation;

import config.SensorNannyConfig;
import messages.SensorNannyException;
import messages.SensorNannyMessages;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.ws.rs.core.Response.Status;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import validation.xslt.handler.XsltResultHandler;


/** Manage format validation with xsd and shematron (via xslt), this is a singleton.
 *  public <b>synchronized</b> void schematronValidateXXXXXX should be changed, validation is too long.
 * This method is synchronized, the checks are done one after the other because Transformer is not thread safe.
 * 
 * @author mlarour
 */
public class SosValidation
{
        
    private SchemaFactory schemaFactory = null;    
    private Validator     oemXsdValidator = null;
    private Validator     sensorMlXsdValidator = null;
    private Transformer   sensorMlRransformer = null;
    private Transformer   oemRransformer = null;
    
    private static SosValidation sosValidation = null;
    private static SensorNannyConfig sensorNannyConfig = null;
   
    /** private constructor, this class is a singleton.
     * 
     */
    private SosValidation()
    {
       schemaFactory        = null;    
       oemXsdValidator      = null;
       sensorMlXsdValidator = null; 
       oemRransformer       = null;
    }
    
    /** Singleton getter.
     * 
     * @param sensorNannyConfig the sos server configuration
     * @return the unique instance
     */
    public static synchronized SosValidation singleton(SensorNannyConfig sensorNannyConfig)
    {        
        if(sosValidation == null)
        {
            SosValidation.sensorNannyConfig = sensorNannyConfig;
            sosValidation = new SosValidation();
            sosValidation.getSchemaFactory();
            sosValidation.getSensorMlXsdValidator();
            sosValidation.getOemXsdValidator();
            sosValidation.getSensorMlTransformer();
            XsltResultHandler.singleton();
        } 
        return(sosValidation);
    }
    /** validate SensorML with xsd
     * 
     * @param sensorML SensorML content file
     * @throws SensorNannyException xml not valid or I/O error 
     */
    public synchronized void xsdValidateSensorMl(String sensorML) throws SensorNannyException
    {
        try
        {
           getSensorMlXsdValidator().validate(new SAXSource(new InputSource(new StringReader(sensorML))));
        }
        catch(SAXParseException spe)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_SENSORML_NOT_VALID_4XSD+
                                           spe.getMessage()+
                                           SensorNannyMessages.LINE+spe.getLineNumber()+
                                           SensorNannyMessages.COLUMN+spe.getColumnNumber()+
                                           spe.getPublicId(),
                                           Status.BAD_REQUEST);            
        }
        catch(SAXException se)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_SENSORML_NOT_VALID_4XSD+
                                           se.getMessage(),
                                           Status.BAD_REQUEST);            
        }
        catch(IOException ioe)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_IO_POST,Status.SERVICE_UNAVAILABLE);
        }        
    }
    /** validate O&amp;M with xsd
     * 
     * @param oem  O&amp;M content file
     * @throws SensorNannyException xml not valid or I/O error
     */
    public synchronized void xsdValidateOem(String oem) throws SensorNannyException
    {                                
        try
        {
           getOemXsdValidator().validate(new SAXSource(new InputSource(new StringReader(oem))));
        }
        catch(SAXParseException spe)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_OEM_NOT_VALID_4XSD+
                                           spe.getMessage()+
                                           SensorNannyMessages.LINE+spe.getLineNumber()+
                                           SensorNannyMessages.COLUMN+spe.getColumnNumber()+
                                           spe.getPublicId(),
                                           Status.BAD_REQUEST);            
        }
        catch(SAXException se)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_OEM_NOT_VALID_4XSD+
                                           se.getMessage(),
                                           Status.BAD_REQUEST);
        }
        catch(IOException ioe)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_IO_POST,Status.SERVICE_UNAVAILABLE);
        }        
    }
    
    /** Getter for SensorML xsd validator.
     * (first call in synchronized singleton() method)
     * @param sensorNannyConfig the sos server configuration
     * @return the sensorMl Xsd Validator
     */
    private Validator getSensorMlXsdValidator() throws SensorNannyException
    {
        if(sensorMlXsdValidator == null)
        {        
            try
            {
                sensorMlXsdValidator = getSchemaFactory().newSchema(sensorNannyConfig.getSensorMlXsdUrl()).newValidator();
                return(sensorMlXsdValidator);            
            }
            catch(SAXException sAXException) 
            {
                throw new SensorNannyException(SensorNannyMessages.ERROR_SENSOR_ML_XSD,Status.SERVICE_UNAVAILABLE);
            }
        }        
        return(sensorMlXsdValidator);
    }

    /** Getter for O&amp;M xsd validator.
     * (first call in synchronized singleton() method)
     * @param sensorNannyConfig the sos server configuration
     * @return the O&amp;M Xsd Validator
     */
    private Validator getOemXsdValidator() throws SensorNannyException
    {
        if(oemXsdValidator == null)
        {        
            try
            {
                oemXsdValidator = getSchemaFactory().newSchema(sensorNannyConfig.getOemXsdUrl()).newValidator();
                return(oemXsdValidator);            
            }
            catch(SAXException sAXException) 
            {
                throw new SensorNannyException(SensorNannyMessages.ERROR_OEM_XSD,Status.SERVICE_UNAVAILABLE);
            }
        }
        return(oemXsdValidator);
    }

    
    
    /** Getter for the schema factory.
     * (first call in synchronized singleton() method)
     * @return the schemaFactory
     */
    private SchemaFactory getSchemaFactory()
    {
        if(schemaFactory == null)
        {
            try
            {
                schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            }
            catch(Exception ex) 
            {
                throw new SensorNannyException(SensorNannyMessages.ERROR_SCHEMA_FACTORY,Status.SERVICE_UNAVAILABLE);
            }    
        }
        return schemaFactory;
    }
    
    /** Getter for the SensorML transformer, XSLT is used to validate schematron.
     * (first call in synchronized singleton() method)<br>
     * Schematron is tranformed in xsl to apply on SensorML content.<br>
     * java -jar saxon/saxon9he.jar -o:sensorml-sdn-core.xsl sensorml-sdn-core.sch  schematron/iso_svrl_for_xslt1.xsl
     * @return the SensorML transformer
     */
    private Transformer getSensorMlTransformer() 
    {
        if(sensorMlRransformer == null)
        {
           Source sensormlXsl = new StreamSource(sensorNannyConfig.getSensorMlXslStream());
           try
           {
                sensorMlRransformer = TransformerFactory.newInstance().newTransformer(sensormlXsl);
           }
           catch(Exception ex) 
           {
                throw new SensorNannyException(SensorNannyMessages.ERROR_SENSORML_TRANSFORMER_FACTORY,Status.SERVICE_UNAVAILABLE);
           }           
        }
        return(sensorMlRransformer);
    }
    /** Getter for the SensorML transformer, XSLT is used to validate schematron.
     * (first call in synchronized singleton() method)<br>
     * Schematron is tranformed in xsl to apply on SensorML content.<br>
     * java -jar saxon/saxon9he.jar -o:sensorml-sdn-core.xsl sensorml-sdn-core.sch  schematron/iso_svrl_for_xslt1.xsl
     * @return the SensorML transformer
     */
    private Transformer getOemTransformer() 
    {
        if(oemRransformer == null)
        {
           Source oemXsl = new StreamSource(sensorNannyConfig.getOemXslStream());
           try
           {
                oemRransformer = TransformerFactory.newInstance().newTransformer(oemXsl);
           }
           catch(Exception ex) 
           {
                throw new SensorNannyException(SensorNannyMessages.ERROR_SENSORML_TRANSFORMER_FACTORY,Status.SERVICE_UNAVAILABLE);
           }           
        }
        return(sensorMlRransformer);
    }
    
    /** validate SensorML with schematron,XSLT is used to validate schematron.
     * Schematron is tranformed in xsl to apply on SensorML content.<br>
     * java -jar saxon/saxon9he.jar -o:sensorml-sdn-core.xsl sensorml-sdn-core.sch  schematron/iso_svrl_for_xslt1.xsl
     * 
     * @param sensorML SensorML content file
     * @throws SensorNannyException if tranformer failed or xml isn't valid
     */
    public synchronized void schematronValidateSensorMl(String sensorML) throws SensorNannyException
    {
        try
        {      
           StringWriter outWriter = new StringWriter();
           getSensorMlTransformer().transform(new StreamSource(new StringReader(sensorML)),new StreamResult(outWriter));
           String error = XsltResultHandler.singleton().analyse(outWriter.getBuffer().toString());
           if(error != null)
           {               
               throw new SensorNannyException(SensorNannyMessages.ERROR_SENSOR_ML_XSL_VALIDATION+error,Status.BAD_REQUEST);           
           }
        }
        catch(TransformerException te)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_SENSOR_ML_XSL,Status.SERVICE_UNAVAILABLE);
        }        
    }
      
    /** validate O&amp;M with schematron,XSLT is used to validate schematron.
     * Schematron is tranformed in xsl to apply on O&amp;M content.<br>
     * java -jar saxon/saxon9he.jar -o:om-sdn-core.xsl om-sdn-core.sch  schematron/iso_svrl_for_xslt1.xsl
     * 
     * @param oem O&amp;M content file
     * @throws SensorNannyException if tranformer failed or xml isn't valid
     */
    public synchronized void schematronValidateOem(String oem) throws SensorNannyException
    {
        try
        {      
           StringWriter outWriter = new StringWriter();
           getOemTransformer().transform(new StreamSource(new StringReader(oem)),new StreamResult(outWriter));
           String error = XsltResultHandler.singleton().analyse(outWriter.getBuffer().toString());
           if(error != null)
           {               
               throw new SensorNannyException(SensorNannyMessages.ERROR_OEM_XSL_VALIDATION+error,Status.BAD_REQUEST);           
           }
        }
        catch(TransformerException te)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_OEM_XSL,Status.SERVICE_UNAVAILABLE);
        }        
    }
}
