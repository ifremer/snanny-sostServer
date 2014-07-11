/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package config;

import messages.SensorNannyException;
import messages.SensorNannyMessages;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Response.Status;
import sos.insert.uuid.handler.Uuids;

/** Getters for SOS server properties (/WEB-INF/sensornanny.properties),
 *  Files and Streams depending on properties values
 *
 * @author mlarour
 */
public class SensorNannyConfig
{
    private static final String PROPERTIES_LOCATION = "/WEB-INF/sensornanny.properties";
    private static final String GETCAPABILITIES_LOCATION = "/locally/getCapabilities.xml";
    private static final String SENSORML_XSL_LOCATION = "/locally/sensorml-sdn-core.xsl";
    private static final String OEM_XSL_LOCATION = "/locally/om-sdn-core.xsl";
    
    
    private static Properties properties;
    private static SensorNannyConfig sensorNannyConfig = null;
    
    // properties keys
    private static String PROPERTY_DATA_DIRETORY = "data_directory";
    private static String PROPERTY_XML_EXTENSION = "xml_extension";
    private static String PROPERTY_XML_CHARSET   = "charset";
    private static String PROPERTY_SENSOR_ML_XSD = "sensor_ml_xsd";
    private static String PROPERTY_OEM_XSD       = "oem_xsd";
    
    // properties values
    private File dataDirectory;
    private String xmlExtension;
    private Charset charset;
    private URL sensorMlXsdUrl;
    private URL oemXsdUrl;

    private ServletContext servletContext;
    
    /** Getter for xml extension of files (eg .xml)
     * @return the xmlExtension
     */
    public String getXmlExtension() {
        return xmlExtension;
    }

    /** Getter for the charset of files (eg UTF-8)
     * @return the charset
     */
    public Charset getCharset() {
        return charset;
    }
    
    /** Getter for the SOS server data directory
     * @return the dataDirectory
     */
    public File getDataDirectory() {
        return dataDirectory;
    }
    
    /** SensorNannyConfig constructor
     * 
     * @param servletContext context of deployed servlet
     * @throws SensorNannyException if properties can't be loaded
     */
    private SensorNannyConfig(ServletContext servletContext) throws SensorNannyException
    {
        this.servletContext = servletContext;
        properties = new Properties();
        // load properties
        try
        {    
           properties.load(servletContext.getResourceAsStream(PROPERTIES_LOCATION));
        }
        catch(IOException ioe)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_PROPERTIES,Status.SERVICE_UNAVAILABLE);
        }
        // initialize data directiory 
        if(properties.getProperty(PROPERTY_DATA_DIRETORY) == null)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_PROPERTIES_DATA_CONFIGURATION,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            dataDirectory = new File(properties.getProperty(PROPERTY_DATA_DIRETORY));
            if(!dataDirectory.isDirectory() ||
               !dataDirectory.canRead() ||
               !dataDirectory.canWrite())
            {
                throw new SensorNannyException(SensorNannyMessages.ERROR_DATA_PERMISSION,Status.SERVICE_UNAVAILABLE);
            }
        }
        
        // initialize xml extension
        if(properties.getProperty(PROPERTY_XML_EXTENSION) == null)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_PROPERTIES_XML_EXTENSION,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            xmlExtension = properties.getProperty(PROPERTY_XML_EXTENSION);
        }
        
        // initialize charset
        if(properties.getProperty(PROPERTY_XML_CHARSET) == null)
        {            
           throw new SensorNannyException(SensorNannyMessages.ERROR_PROPERTIES_XML_CHARSET,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            try
            {
                charset = Charset.forName(properties.getProperty(PROPERTY_XML_CHARSET));
            }
            catch(Exception ex)
            {
                throw new SensorNannyException(SensorNannyMessages.ERROR_XML_CHARSET,Status.SERVICE_UNAVAILABLE);        
            }
        }
        
        // initialize sensor_ml_xsd
        if(properties.getProperty(PROPERTY_SENSOR_ML_XSD) == null)
        {            
           throw new SensorNannyException(SensorNannyMessages.ERROR_PROPERTIES_SENSOR_ML_XSD,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            try
            {
                sensorMlXsdUrl = new URL(properties.getProperty(PROPERTY_SENSOR_ML_XSD));
            }
            catch(Exception ex)
            {
                throw new SensorNannyException(SensorNannyMessages.ERROR_URL_SENSOR_ML_XSD,Status.SERVICE_UNAVAILABLE);        
            }
        }

        // initialize oem_xsd
        if(properties.getProperty(PROPERTY_OEM_XSD) == null)
        {            
           throw new SensorNannyException(SensorNannyMessages.ERROR_PROPERTIES_OEM_XSD,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            try
            {
                oemXsdUrl = new URL(properties.getProperty(PROPERTY_OEM_XSD));
            }
            catch(Exception ex)
            {
                throw new SensorNannyException(SensorNannyMessages.ERROR_URL_OEM_XSD,Status.SERVICE_UNAVAILABLE);        
            }
        }
    }
    /** Getter for the singleton, perform initialization at first call
     * 
     * @param servletContext the servlet context
     * @return the the unique instance
     * @throws SensorNannyException if properties can't be loaded at first call
     */
    public static synchronized SensorNannyConfig singleton(ServletContext servletContext) throws SensorNannyException
    {
        if(sensorNannyConfig == null)
        {
            sensorNannyConfig = new SensorNannyConfig(servletContext);
        }
        return(sensorNannyConfig);                        
    }
    
    /** Builder for SensorMl java.io.File from uuid.
     * (file isn't created)
     * @param uuid SensorMl identifier
     * @return SensorMl java.io.File
     */
    public File newSensorMlFile(String uuid)
    {
        return(new File(dataDirectory+File.separator+uuid+xmlExtension));
    }
    
    /** Builder for SensorMl java.io.File directory from uuids
     * (directory isn't created)
     * @param uuids SensorMl and O&amp;M identifiers
     * @return SensorMl java.io.File directory
     */
    public File newSensorMlDir(Uuids uuids)
    {
        return(new File(dataDirectory+File.separator+uuids.getSensorMLuuid()));
    }
    /** Builder for O&amp; java.io.File from uuids.
     * (file isn't created)
     * @param uuids SensorMl and O&amp;M identifiers
     * @return O&amp; java.io.File
     */
    public File newOemFile(Uuids uuids)
    {
        return(new File(dataDirectory+File.separator+uuids.getSensorMLuuid()+File.separator+uuids.getOeMuuid()+xmlExtension));
    }
    
    /** Getter for SensorMl Xsl java.io.Stream
     * 
     * @return SensorMl Xsl java.io.Stream
     */
    public InputStream getSensorMlXslStream()
    {
        return(servletContext.getResourceAsStream(SENSORML_XSL_LOCATION));
    }
    
    /** Getter for O&amp;M Xsl java.io.Stream
     * 
     * @return O&amp;M Xsl java.io.Stream
     */
    public InputStream getOemXslStream()
    {
        return(servletContext.getResourceAsStream(OEM_XSL_LOCATION));
    }
    
    /** Getter for Cababilities java.io.InputStream
     * 
     * @return Cababilities java.io.InputStream
     */
    public InputStream getCababilitiesInputstream()
    {
       return(servletContext.getResourceAsStream(SensorNannyConfig.GETCAPABILITIES_LOCATION));
    }
    
    /** Getter for valid SensorMl java.io.File
     * 
     * @param uuid SensorMl identifier
     * @return valid SensorMl java.io.File
     * @throws SensorNannyException if SensorMl File doesn't exist or doesn't have required permissions
     */
    public File getSensorMlFile(String uuid) throws SensorNannyException
    {
        File file = newSensorMlFile(uuid);        
        if(file.isFile() && file.canRead() && file.canWrite())
        {
            return(file);
        }
        else
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_NO_UUID_RECORD_1of2+
                                              uuid+
                                              SensorNannyMessages.ERROR_NO_UUID_RECORD_2of2,
                                              Status.NOT_FOUND); 
        }
    }
    
    /**
     * 
     * @param uuid O&amp;M identifier
     * @return valid O&amp;M java.io.File
     * @throws SensorNannyException if O&amp;M File doesn't exist or doesn't have required permissions
     */
    public File getOemFile(String uuid) throws SensorNannyException
    {
        for(File dir : dataDirectory.listFiles())
        {
            if(dir.isDirectory() && dir.canRead())
            for(File file : dir.listFiles())
            {                    
                if(file.getName().compareTo(uuid+xmlExtension) == 0)
                {
                    if(file.isFile() && file.canRead() && file.canWrite())
                    {
                        return(file);
                    } 
                }
            }
        }        
        throw new SensorNannyException(SensorNannyMessages.ERROR_NO_UUID_RECORD_1of2+
                                       uuid+
                                       SensorNannyMessages.ERROR_NO_UUID_RECORD_2of2,
                                       Status.NOT_FOUND);         
    }
    
    /** Getter for sensorMl Xsd Url
     * @return the sensorMl Xsd Url
     */
    public URL getSensorMlXsdUrl() {
        return sensorMlXsdUrl;
    }

    /** Getter for the O&amp;M Xsd Url
     * @return the O&amp;M Xsd Url
     */
    public URL getOemXsdUrl() {
        return oemXsdUrl;
    }
}
