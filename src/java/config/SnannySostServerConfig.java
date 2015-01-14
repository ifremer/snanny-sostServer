/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package config;

import messages.SnannySostServerException;
import messages.SnannySostServerMessages;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Response.Status;
import sos.insert.uuid.handler.Uuids;

/** Getters for SOS server properties (/WEB-INF/snanny-sostServer.properties),
 *  Files and Streams depending on properties values
 *
 * @author mlarour
 */
public class SnannySostServerConfig implements Serializable
{
    public static String AuthorNameField = "Authorname";
    public static String AuthorNameValue = "sostServer";
    public static String FilejsonField = "Filejson";
    private static String extraInfo = "";
    
    
    private static final String PROPERTIES_LOCATION = "/WEB-INF/snanny-sostServer.properties";
    private static final String GETCAPABILITIES_LOCATION = "/locally/getCapabilities.xml";
    private static final String SENSORML_XSL_LOCATION = "/locally/sensorml-sdn-core.xsl";
    private static final String OEM_XSL_LOCATION = "/locally/om-sdn-core.xsl";
    
    private static final String STORAGE_COUCHBASE = "couchbase";
    private static final String STORAGE_DISK = "disk";
    
    private static final String JSON_OBJECT = "object";
    private static final String JSON_ARRAY  = "array";
    
    private static SnannySostServerConfig snannySostServerConfig = null;
    
    // properties keys
    private static String PROPERTY_STORAGE = "storage";
    private static String PROPERTY_JSON = "json";
    
    private static String PROPERTY_DATA_DIRETORY = "data_directory";
    private static String PROPERTY_XML_EXTENSION = "xml_extension";
    private static String PROPERTY_XML_CHARSET   = "charset";
    private static String PROPERTY_SENSOR_ML_XSD = "sensor_ml_xsd";
    private static String PROPERTY_OEM_XSD       = "oem_xsd";
    
    private static String PROPERTY_COUCHBASE_HOSTS              = "CouchbaseHosts";
    private static String PROPERTY_COUCHBASE_PASSWD             = "CouchbasePasswd";
    private static String PROPERTY_COUCHBASE_SYSTEM_BUCKET      = "CouchbaseSystemBucket";
    private static String PROPERTY_COUCHBASE_OBSERVATION_BUCKET = "CouchbaseObservationBucket";            
    private static String PROPERTY_COUCHBASE_TIME_OUT_MS        ="CouchbaseTimeOutMS";
    
    private static String PROPERTY_SQUID_HOST = "squid_host";            
    private static String PROPERTY_SQUID_PORT = "squid_port";
    

    /**
     * @return the extraInfo
     */
    public static String getExtraInfo() {
        return extraInfo;
    }
    
    // properties values
    private String xmlExtension;
    private Charset charset;
    private URL sensorMlXsdUrl;
    private URL oemXsdUrl;

    private boolean couchbase;
    private boolean jsonObject;
            
    private File dataDirectory;
        
    private String[] couchbaseHosts;
    private String   couchbasePasswd;
    private String   couchbaseSystemBucket;
    private String   couchbaseObservationBucket;
    private int      couchbaseTimeOutMS; 
    private String   squidHost;
    private String   squidPort;
    
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
    
    /** SnannySostServerConfig constructor
     * 
     * @param servletContext context of deployed servlet
     * @throws SnannySostServerException if properties can't be loaded
     */
    private SnannySostServerConfig(ServletContext servletContext) throws SnannySostServerException
    {
        this.servletContext = servletContext;
        Properties properties = new Properties();
        // load properties
        try
        {    
           properties.load(servletContext.getResourceAsStream(PROPERTIES_LOCATION));
        }
        catch(IOException ioe)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTIES,Status.SERVICE_UNAVAILABLE);
        }
        
        // initialize storage
        if(properties.getProperty(PROPERTY_STORAGE) == null)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTY_STORAGE,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            couchbase = properties.getProperty(PROPERTY_STORAGE).compareTo(STORAGE_COUCHBASE) == 0;
            extraInfo += " / "+properties.getProperty(PROPERTY_STORAGE);
        }
        
        // initialize json
        if(properties.getProperty(PROPERTY_JSON) == null)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTY_JSON,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            jsonObject = properties.getProperty(PROPERTY_JSON).compareTo(JSON_OBJECT) == 0;
            extraInfo += " / "+properties.getProperty(PROPERTY_JSON);
        }
        
        // initialize data directiory 
        if(!isCouchbase())
        {
            if(properties.getProperty(PROPERTY_DATA_DIRETORY) == null)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTIES_DATA_CONFIGURATION,Status.SERVICE_UNAVAILABLE);
            }
            else
            {
                dataDirectory = new File(properties.getProperty(PROPERTY_DATA_DIRETORY));
                if(!dataDirectory.isDirectory() ||
                   !dataDirectory.canRead() ||
                   !dataDirectory.canWrite())
                {
                    throw new SnannySostServerException(SnannySostServerMessages.ERROR_DATA_PERMISSION,Status.SERVICE_UNAVAILABLE);
                }
            }
        }
        
        // initialize xml extension
        if(properties.getProperty(PROPERTY_XML_EXTENSION) == null)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTIES_XML_EXTENSION,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            xmlExtension = properties.getProperty(PROPERTY_XML_EXTENSION);
        }
        
        // initialize charset
        if(properties.getProperty(PROPERTY_XML_CHARSET) == null)
        {            
           throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTIES_XML_CHARSET,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            try
            {
                charset = Charset.forName(properties.getProperty(PROPERTY_XML_CHARSET));
            }
            catch(Exception ex)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_XML_CHARSET,Status.SERVICE_UNAVAILABLE);        
            }
        }
        
        // initialize sensor_ml_xsd
        if(properties.getProperty(PROPERTY_SENSOR_ML_XSD) == null)
        {            
           throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTIES_SENSOR_ML_XSD,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            try
            {
                sensorMlXsdUrl = new URL(properties.getProperty(PROPERTY_SENSOR_ML_XSD));
            }
            catch(Exception ex)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_URL_SENSOR_ML_XSD,Status.SERVICE_UNAVAILABLE);        
            }
        }

        // initialize oem_xsd
        if(properties.getProperty(PROPERTY_OEM_XSD) == null)
        {            
           throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTIES_OEM_XSD,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            try
            {
                oemXsdUrl = new URL(properties.getProperty(PROPERTY_OEM_XSD));
            }
            catch(Exception ex)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_URL_OEM_XSD,Status.SERVICE_UNAVAILABLE);        
            }
        }
        
        
        if(isCouchbase())
        {
            // initialize couchbase Host
            if(properties.getProperty(PROPERTY_COUCHBASE_HOSTS) == null)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTY_COUCHBASE_HOST,Status.SERVICE_UNAVAILABLE);
            }
            else
            {            
                couchbaseHosts = properties.getProperty(PROPERTY_COUCHBASE_HOSTS).split(",");            
            }

            // initialize Couchbase Passwd
            if(properties.getProperty(PROPERTY_COUCHBASE_PASSWD) == null)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTY_COUCHBASE_PASSWD,Status.SERVICE_UNAVAILABLE);
            }
            else
            {
                couchbasePasswd = properties.getProperty(PROPERTY_COUCHBASE_PASSWD);            
            }

            // initialize Couchbase System Bucket
            if(properties.getProperty(PROPERTY_COUCHBASE_SYSTEM_BUCKET) == null)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTY_COUCHBASE_SYSTEM_BUCKET,Status.SERVICE_UNAVAILABLE);
            }
            else
            {
                couchbaseSystemBucket = properties.getProperty(PROPERTY_COUCHBASE_SYSTEM_BUCKET);
            }

            // initialize Couchbase Observation Bucket
            if(properties.getProperty(PROPERTY_COUCHBASE_OBSERVATION_BUCKET) == null)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTY_COUCHBASE_OBSERVATION_BUCKET,Status.SERVICE_UNAVAILABLE);
            }
            else
            {
                couchbaseObservationBucket = properties.getProperty(PROPERTY_COUCHBASE_OBSERVATION_BUCKET);
            }
            
            // initialize Couchbase Observation Bucket
            if(properties.getProperty(PROPERTY_COUCHBASE_TIME_OUT_MS) == null)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTY_COUCHBASE_TIME_OUT_MS,Status.SERVICE_UNAVAILABLE);
            }
            else
            {
                couchbaseTimeOutMS = Integer.parseInt(properties.getProperty(PROPERTY_COUCHBASE_TIME_OUT_MS));
            }                        
        }
        // initialize Squid host
        if(properties.getProperty(PROPERTY_SQUID_HOST) == null)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTY_SQUID_HOST,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            squidHost = properties.getProperty(PROPERTY_SQUID_HOST);
        }
        // initialize Squid port
        if(properties.getProperty(PROPERTY_SQUID_PORT) == null)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROPERTY_SQUID_PORT,Status.SERVICE_UNAVAILABLE);
        }
        else
        {
            squidPort = properties.getProperty(PROPERTY_SQUID_PORT);
        }
        
    }
    /** Getter for the singleton, perform initialization at first call
     * 
     * @param servletContext the servlet context
     * @return the the unique instance
     * @throws SnannySostServerException if properties can't be loaded at first call
     */
    public static synchronized SnannySostServerConfig singleton(ServletContext servletContext) throws SnannySostServerException
    {
        if(snannySostServerConfig == null)
        {
            snannySostServerConfig = new SnannySostServerConfig(servletContext);
        }
        return(snannySostServerConfig);                        
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
       return(servletContext.getResourceAsStream(SnannySostServerConfig.GETCAPABILITIES_LOCATION));
    }
    
    /** Getter for valid SensorMl java.io.File
     * 
     * @param uuid SensorMl identifier
     * @return valid SensorMl java.io.File
     * @throws SnannySostServerException if SensorMl File doesn't exist or doesn't have required permissions
     */
    public File getSensorMlFile(String uuid) throws SnannySostServerException
    {
        File file = newSensorMlFile(uuid);        
        if(file.isFile() && file.canRead() && file.canWrite())
        {
            return(file);
        }
        else
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_NO_UUID_RECORD_1of2+
                                              uuid+
                                              SnannySostServerMessages.ERROR_NO_UUID_RECORD_2of2,
                                              Status.NOT_FOUND); 
        }
    }
    
    /**
     * 
     * @param uuid O&amp;M identifier
     * @return valid O&amp;M java.io.File
     * @throws SnannySostServerException if O&amp;M File doesn't exist or doesn't have required permissions
     */
    public File getOemFile(String uuid) throws SnannySostServerException
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
        throw new SnannySostServerException(SnannySostServerMessages.ERROR_NO_UUID_RECORD_1of2+
                                       uuid+
                                       SnannySostServerMessages.ERROR_NO_UUID_RECORD_2of2,
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

    /**
     * @return the couchbaseHost
     */
    public String[] getCouchbaseHosts() {
        return couchbaseHosts;
    }    

    /**
     * @return the couchbasePasswd
     */
    public String getCouchbasePasswd() {
        return couchbasePasswd;
    }

    /**
     * @return the couchbaseSystemBucket
     */
    public String getCouchbaseSystemBucket() {
        return couchbaseSystemBucket;
    }

    /**
     * @return the couchbaseObservationBucket
     */
    public String getCouchbaseObservationBucket() {
        return couchbaseObservationBucket;
    }

    /**
     * @return the couchbase
     */
    public final boolean isCouchbase() {
        return couchbase;
    }

    /**
     * @return the jsonObject
     */
    public boolean isJsonObject() {
        return jsonObject;
    }

    /**
     * @return the couchbaseTimeOutMS
     */
    public int getCouchbaseTimeOutMS() {
        return couchbaseTimeOutMS;
    }

    /**
     * @param couchbaseBucketTimeOutMS the couchbaseTimeOutMS to set
     */
    public void setCouchbaseTimeOutMS(int couchbaseBucketTimeOutMS) {
        this.couchbaseTimeOutMS = couchbaseBucketTimeOutMS;
    }

    /**
     * @return the squidHost
     */
    public String getSquidHost() {
        return squidHost;
    }

    /**
     * @return the squidPort
     */
    public String getSquidPort() {
        return squidPort;
    }
}
