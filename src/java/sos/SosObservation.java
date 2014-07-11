/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sos;

import messages.SensorNannyException;
import messages.SensorNannyMessages;
import javax.ws.rs.core.Response.Status;

/** Uuid observation extraction from the persistent URL
 *
 * @author mlarour
 */
public class SosObservation 
{
    public  static final String SOS_OBSERVATION_KEYWORD = "observation";
    private static final String SOS_OBSERVATION_UUID_PREFIX = "/record/";
    
    /** Getter for uuid from the persistent URL of observation
     * 
     * @param observationValue the persistent URL of observation
     * @return the uuid
     */
    public static String getUuid(String observationValue)
    {
        int index = observationValue.indexOf(SOS_OBSERVATION_UUID_PREFIX);
        if(index == -1)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_URL_OBSERVATION,Status.BAD_REQUEST);    
        }
        String uuid = observationValue.substring(index+SOS_OBSERVATION_UUID_PREFIX.length());
        return(uuid);
    }
}
