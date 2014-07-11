/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sos;

import messages.SensorNannyException;
import messages.SensorNannyMessages;
import javax.ws.rs.core.Response.Status;

/** Uuid procedure extraction from the persistent URL
 *
 * @author mlarour
 */
public class SosProcedure {

    public  static final String SOS_PROCEDURE_KEYWORD = "procedure";
    private static final String SOS_PROCEDURE_UUID_PREFIX = "/record/";
    
    /** Getter for uuid from the persistent URL of procedure
     * 
     * @param procedureValue the persistent URL of procedure
     * @return the uuid
     */
    public static String getUuid(String procedureValue)
    {
        int index = procedureValue.indexOf(SOS_PROCEDURE_UUID_PREFIX);
        if(index == -1)
        {
            throw new SensorNannyException(SensorNannyMessages.ERROR_URL_PROCEDURE,Status.BAD_REQUEST);    
        }
        String uuid = procedureValue.substring(index+SOS_PROCEDURE_UUID_PREFIX.length());
        return(uuid);
    }
    
}
