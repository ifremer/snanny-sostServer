/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sos;

import messages.SensorNannyException;
import messages.SensorNannyMessages;
import javax.ws.rs.core.Response.Status;

/** Manage sos version availability
 *
 * @author mlarour
 */
public class SosVersion {
    
    public static String SOS_VERSION_KEYWORD = "version";
    private static String availableVersions = null;
    
    public static enum SOSVERSION
    {
        VERSION_2_0
    }
    /** Getter for readable version
     * 
     * @param sosVersion the version
     * @return the readable version
     */
    private static String name(SOSVERSION sosVersion)
    {
        switch(sosVersion)
        {
            case VERSION_2_0 : return("2.0");
        }        
        return(sosVersion.name());
    }
    
    /**Getter for sos version from Http version parameter
     * 
     * @param versionString Http version parameter
     * @return the sos version 
     * @throws SensorNannyException for unknown version
     */
    public static SOSVERSION get(String versionString) throws SensorNannyException
    {
        if(versionString.compareTo(name(SOSVERSION.VERSION_2_0)) == 0)
        {
            return(SOSVERSION.VERSION_2_0);
        }
        throw new SensorNannyException(SensorNannyMessages.ERROR_VERSION_UNKNOWN+getAvailableSosVersions(),Status.BAD_REQUEST);
    
    }
    
    /** Getter for the list of the managed sos versions
     * 
     * @return the list of the managed sos versions
     */
    public static String getAvailableSosVersions()
    {
        if(availableVersions != null)
        {
            return(availableVersions);
        }
        availableVersions = "";
        for(SOSVERSION sosVersion : SOSVERSION.values())
        {
            if(isAvailable(sosVersion))
            {
                if(availableVersions.length() > 0)
                {
                    availableVersions+=",";
                }
                availableVersions+=name(sosVersion);            
            }
        }
        return(availableVersions);
    }
    
    /** Getter for the availability of a specific version
     * 
     * @param sosVersion the version to check
     * @return true if the sos version is Available
     */
    public static boolean isAvailable(SOSVERSION sosVersion)
    {
       switch(sosVersion)
       {
           case VERSION_2_0 : return(true);
       }
       return(false);
    }
}
