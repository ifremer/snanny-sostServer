/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sos;

import messages.SensorNannyException;
import messages.SensorNannyMessages;
import javax.ws.rs.core.Response.Status;

/** Manage sos formats, availability and corresponding content types
 *
 * @author mlarour
 */
public class SosFormat {
    
    public  static String SOS_RESPONSE_FORMAT_KEYWORD = "responseFormat";
    
    public  static String SOS_RESPONSE_FORMAT_JSON = "application/json;subtype=\"http://www.opengis.net/om/2.0\"";
    public  static String SOS_RESPONSE_FORMAT_XML  = "text/xml;subtype=\"http://www.opengis.net/om/2.0\"";
    
    private static String availableResponseFromats = null;
    
    public enum SOSFORMAT
    {
        JSON,
        XML
    }
    
    /** Content type getter from format
     * 
     * @param sosresponseformat the format
     * @return Content type
     */
    public static String getContentType(SOSFORMAT sosresponseformat)
    {
        switch(sosresponseformat)
        {
            case JSON : return(SOS_RESPONSE_FORMAT_JSON);
            case XML  : return(SOS_RESPONSE_FORMAT_XML);
        }        
        return(sosresponseformat.name());
    }
    
    /** Format getter from content type
     * 
     * @param contentType the content type
     * @return the format
     */
    public static SOSFORMAT getFormat(String contentType)
    {
        if(contentType.compareTo(getContentType(SOSFORMAT.JSON)) == 0)
        {
            return(SOSFORMAT.JSON);
        }
        if(contentType.compareTo(getContentType(SOSFORMAT.XML))== 0)
        {
            return(SOSFORMAT.XML);
        }
        throw new SensorNannyException(SensorNannyMessages.ERROR_RESPONSE_FORMAT_UNKNOWN+getAvailableSosResponseFormats(),Status.BAD_REQUEST);
    
    }
    
    /** Getter for the list of the managed sos formats
     * 
     * @return the list of the managed sos formats
     */
    public static String getAvailableSosResponseFormats()
    {
        if(availableResponseFromats != null)
        {
            return(availableResponseFromats);
        }
        availableResponseFromats = "";
        for(SOSFORMAT sosresponseformat : SOSFORMAT.values())
        {
            if(isAvailable(sosresponseformat))
            {
                if(availableResponseFromats.length() > 0)
                {
                    availableResponseFromats+=",";
                }
                availableResponseFromats+=getContentType(sosresponseformat);            
            }
        }
        return(availableResponseFromats);
    }
    
    /** Getter for the availability of a specific format 
     * 
     * @param sosResponseFormat the format to check
     * @return true if the format is Available
     */
    public static boolean isAvailable(SOSFORMAT sosResponseFormat)
    {
       switch(sosResponseFormat)
       {
           case JSON : return(true);
           case XML  : return(true);
       }
       return(false);
    }
}
