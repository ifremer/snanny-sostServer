/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sos;

import messages.SnannySostServerException;
import messages.SnannySostServerMessages;
import javax.ws.rs.core.Response;

/** Manage sos service availability
 *
 * @author mlarour
 */
public class SosService {
    
    public static String SOS_SERVICE_KEYWORD = "service";
    private static String availableServices = null;
    
    public static enum SOSSERVICE
    {
        SOS
    }
    
            
    /** Getter for the list of the managed sos services
     *     
     * @return the list of the managed sos services
     */        
    public static String getAvailableSosServices()
    {
        if(availableServices != null)
        {
            return(availableServices);
        }
        availableServices = "";
        for(SOSSERVICE service : SOSSERVICE.values())
        {
            if(isAvailable(service))
            {
                if(availableServices.length() > 0)
                {
                    availableServices+=",";
                }
                availableServices+=service.name();            
            }
        }
        return(availableServices);
    }
    
    /**Getter for the availability of a specific service
     * 
     * @param sosService the service to check
     * @return true if the sos service is Available
     */
    public static boolean isAvailable(SOSSERVICE sosService)
    {
       switch(sosService)
       {
           
           case SOS : return(true);                          
           default : return(false);
       }
    }
    /** Getter for sos service from Http service parameter
     * 
     * @param serviceString Http service parameter
     * @return the sos service
     * @throws SnannySostServerException for unknown service
     */
    public static SOSSERVICE get(String serviceString) throws SnannySostServerException
    {
        if(serviceString != null)
        {
            for(SosService.SOSSERVICE service : SosService.SOSSERVICE.values())
            {
                if(serviceString.compareTo(service.name()) == 0)
                {
                    return(service);
                }
            }
        }
        throw new SnannySostServerException(SnannySostServerMessages.ERROR_SERVICE_UNKNOWN+getAvailableSosServices(),Response.Status.BAD_REQUEST);
    }
}
