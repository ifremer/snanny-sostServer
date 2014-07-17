/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package messages;

/** Static values for SOS server messages
 *
 * @author mlarour
 */
public class SensorNannyMessages
{   
    // 
    public static String LINE   = " line:";
    public static String COLUMN = " column:";
    
    
    // uuid between 1of2 and 2of2
    public static String IMPORT_SENSOR_OK_1of2 = "SensorML ";
    public static String IMPORT_SENSOR_OK_2of2 = " successfully imported";    
    
// uuid between 1of2 and 2of2
    public static String IMPORT_OBSERVATION_OK_1of3 = "OeM ";
    public static String IMPORT_OBSERVATION_OK_2of3 = " / ";
    public static String IMPORT_OBSERVATION_OK_3of3 = " successfully imported";    
    
    // uuid between 1of2 and 2of2
    public static String DELETE_SENSOR_OK_1of2 = "SensorML ";
    public static String DELETE_SENSOR_OK_2of2 = " successfully deleted";    
    
    // uuid between 1of2 and 2of2
    public static String DELETE_OBSERVATION_OK_1of2 = "OeM ";
    public static String DELETE_OBSERVATION_OK_2of2 = " successfully deleted";    
    // xml root tag for errors
    public static String OPEN_XML_ERROR                      = "<error>";
    public static String CLOSE_XML_ERROR                     = "</error>";
    
    // configuration errors
    public static String ERROR_PROPERTIES                    = "Can't get sensorNanny properties";
    public static String ERROR_PROPERTIES_DATA_CONFIGURATION = "Unknown configuration for data in sensorNanny properties";
    public static String ERROR_PROPERTIES_XML_EXTENSION      = "Unknown configuration for xml extension in sensorNanny properties";
    public static String ERROR_PROPERTIES_XML_CHARSET        = "Unknown configuration for charset in sensorNanny properties";
    public static String ERROR_PROPERTIES_SENSOR_ML_XSD      = "Unknown configuration for SemsorML xsd in sensorNanny properties";
    public static String ERROR_PROPERTIES_OEM_XSD            = "Unknown configuration for O&M xsd in sensorNanny properties";
    
    
    
    // errors
    public static String ERROR_XML_CHARSET                   = "Unknown charset in sensorNanny properties";    
    public static String ERROR_DATA_DIRECTORY                = "Directory expected for data in sensorNanny properties";
    public static String ERROR_DATA_PERMISSION               = "Bad permissions for data directory";
    public static String ERROR_DATA_FILE_READING             = "Unable to read data file";
    public static String ERROR_RESPONSE_WRITER               = "Unable to get response writer";        
    public static String ERROR_getCapabilities               = "getCapabilities failed";
    public static String ERROR_URL_PROCEDURE                 = "Error in the persistent URL of the procedure";
    public static String ERROR_PROCEDURE_REQUIRED            = "procedure parameter is required";
    public static String ERROR_describeSensor                = "describeSensor failed";
    public static String ERROR_URL_SENSOR_ML_XSD             = "Can't use SemsorML xsd url";
    public static String ERROR_URL_OEM_XSD                   = "Can't use O&M xsd url";
    public static String ERROR_SENSOR_ML_XSD                 = "Can't initialize SemsorML xsd";
    public static String ERROR_OEM_XSD                       = "Can't initialize O&M xsd";
    public static String ERROR_SCHEMA_FACTORY                = "Cant get schema factory for xsd validation";
    public static String ERROR_URL_OBSERVATION               = "Error in the persistent URL of the observation";
    public static String ERROR_OBSERVATION_REQUIRED          = "observation parameter is required";
    public static String ERROR_ONLY_POST_INSERTSENSOR        = "insertSensor works only with post method";
    public static String ERROR_ONLY_POST_INSERTOBSERVATION   = "insertObservation works only with post method";
    public static String ERROR_SAXPARSER_INSERT              = "Can't initialize SAX parser for insert query string";
    public static String ERROR_SAXPARSER_UUID                = "Can't initialize SAX parser to get uuid from post content";    
    public static String ERROR_PARSE_UUID                    = "parse error for getting uuid from post content";
    public static String ERROR_IO_UUID                       = "I/O exception for parsing uuid from post content";        
    public static String ERROR_PARSE_INSERT                  = "parse error for insert query string";
    public static String ERROR_IO_INSERT                     = "I/O exception for insert";
    public static String ERROR_IO_POST                       = "I/O exception for post content";
    public static String ERROR_POST_FORMAT_REQUIRED          = "format required in insert query string";
    public static String ERROR_JSON_INSERT                   = "json error in insert query string";
    public static String ERROR_STORE_INSERT                  = "Can't store insert on system";
    public static String ERROR_SENSORML_TRANSFORMER_FACTORY  = "Cant get transformer for shematron validation via xsl";
    public static String ERROR_SENSOR_ML_XSL                 = "Schematron as xsl for SensorML is not valid";
    public static String ERROR_SENSOR_ML_XSL_VALIDATION      = "SensorML is not valid for Schematron as xsl : ";
    public static String ERROR_OEM_XSL                       = "Schematron as xsl for SensorML is not valid";
    public static String ERROR_OEM_XSL_VALIDATION            = "SensorML is not valid for Schematron as xsl : ";
    public static String ERROR_SAXPARSER_XSLT                = "Can't initialize SAX parser to get xslt result for schematron validation";    
    public static String ERROR_PARSE_XSLT                    = "parse error for parsing xsl schematron result";
    public static String ERROR_IO_XSLT                       = "I/O exception for parsing xsl schematron result";        
    public static String ERROR_IO_deleteSensor               = "I/O exception, delete Sensor failed ";
    public static String ERROR_IO_deleteObservation          = "I/O exception, delete Observation failed ";

    // not available uuid betwenn 1of2 and 2of2
    public static String ERROR_NO_UUID_RECORD_1of2           = "record ";
    public static String ERROR_NO_UUID_RECORD_2of2           = " is not available";
    
    // request usage error  (add allowed request list after)
    public static String ERROR_REQUEST_NOT_IMPLEMENTED       = "Request not implemented, allowed are : ";
    public static String ERROR_REQUEST_UNKNOWN               = "Request unknown, allowed are : ";
    public static String ERROR_REQUEST_REQUIRED              = "Request parameter required, allowed are : ";
    
    // version usage error  (add allowed request list after)
    public static String ERROR_VERSION_UNKNOWN               = "Version unknown, allowed are : ";
    public static String ERROR_VERSION_REQUIRED              = "Version parameter required, allowed are : ";
    
    // service usage error  (add allowed request list after)
    public static String ERROR_SERVICE_UNKNOWN               = "Service unknown, allowed are : ";
    public static String ERROR_SERVICE_REQUIRED              = "Service parameter required, allowed are : ";
    
    // responseFormat usage
    public static String ERROR_RESPONSE_FORMAT_UNKNOWN       = "responseFormat unknown, allowed are : ";
    public static String ERROR_RESPONSE_FORMAT_REQUIRED      = "responseFormat parameter required, allowed are : ";
    
    // xml not correct in insert post (add concerned qName after)
    public static String ERROR_XML_INSERT                    = "unexpected xml in insert query string : ";
    
    // xsd validation failed (add detail after)
    public static String ERROR_SENSORML_NOT_VALID_4XSD       = "SensorMl is not valid for xsd : ";
    public static String ERROR_OEM_NOT_VALID_4XSD            = "O&M is not valid for xsd : ";
   
    // transform xml for json
    public static String ERROR_SAXPARSER_XML_JSON            = "Can't initialize SAX parser to prepare xml for json conversion";
    public static String ERROR_SAXPARSER_PREPARE_XML_JSON    = "parse error to prepare xml for json conversion : ";
    public static String ERROR_IO_PREPARE_XML_JSON           = "I/O exception to prepare xml for json conversion : ";
    public static String ERROR_XML_JSON_CONVERSION           = "Xml to Json conversion error : ";
   
    
    
           
}
