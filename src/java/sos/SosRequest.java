/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sos;

import messages.SnannySostServerException;
import messages.SnannySostServerMessages;
import javax.ws.rs.core.Response.Status;

/** Manage sos request availability
 *
 * @author mlarour
 */
public class SosRequest {
         
    
    public static String SOS_REQUEST_KEYWORD = "request";
    
    private static String availableRequests = null;
    
    public static enum SOSREQUEST
    {
        getCapabilities,
        describeSensor,
        getObservation,
        getObservationById,
        getResult,
        insertSensor,
        deleteSensor,
        insertObservation,
        deleteObservation, //(not in official SOS protocol ?)
        insertResult,
        deleteResult
    };
    
    /** Getter for the list of the managed sos requests
     * 
     * @return the list of the managed sos requests
     */
    public static String getAvailableSosRequests()
    {
        if(availableRequests != null)
        {
            return(availableRequests);
        }
        availableRequests = "";
        for(SOSREQUEST request : SOSREQUEST.values())
        {
            if(isAvailable(request))
            {
               if(availableRequests.length() > 0)
               {
                  availableRequests+=",";
               }
               availableRequests+=request.name();            
            }
        }
        return(availableRequests);
    }
    
    /** Getter for sos request from Http request parameter
     * 
     * @param requestString Http request parameter
     * @return the sos request
     * @throws SnannySostServerException for unknown request
     */
    public static SOSREQUEST get(String requestString) throws SnannySostServerException
    {
        if(requestString != null)
        {
            for(SOSREQUEST request : SOSREQUEST.values())
            {
                if(requestString.compareTo(request.name()) == 0)
                {
                    return(request);
                }
            }
        }
        throw new SnannySostServerException(SnannySostServerMessages.ERROR_REQUEST_UNKNOWN+getAvailableSosRequests(),Status.BAD_REQUEST);
    }
    
    /** Getter for the availability of a specific request
     * 
     * @param sosRequest the sos request to check
     * @return true if the sos request is Available
     */
    public static boolean isAvailable(SOSREQUEST sosRequest)
    {
       switch(sosRequest)
       {
           // For a version 0, some requests are not implemented:
           case getObservation :
           case getResult :
           case insertResult :
           case deleteResult: return(false);
               
           default : return(true);
       }
    }
}
