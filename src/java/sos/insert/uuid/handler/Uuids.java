/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sos.insert.uuid.handler;

/** Couple of uuid, SensorML and O&amp;M
 *
 * @author mlarour
 */
public class Uuids
{
    
    private String sensorMLuuid;
    private String oeMuuid;

    /** Couple constructor
     * 
     * @param SensorMLuuid the SensorML uuid
     * @param OeMuuid the O&amp;M uuid
     */
    public Uuids(String SensorMLuuid, String OeMuuid) {
        this.sensorMLuuid = SensorMLuuid;
        this.oeMuuid = OeMuuid;
    }
    /** Empty  Couple constructor
     * 
     */
    public Uuids()
    {
        clear();
    }

    /** Getter for Uuids state (filled or not)
     * 
     * @return true if both uuid are known
     */
    public boolean filled()
    {
        return(sensorMLuuid != null && oeMuuid != null);
    }
    /** clear Couple of uuid
     * 
     */
    public void clear()
    {
        sensorMLuuid = null;
        oeMuuid      = null;
    }
    
    /** Getter for SensorMLuuid
     * @return the SensorMLuuid
     */
    public String getSensorMLuuid() {
        return sensorMLuuid;
    }

    /** Getter for OeMuuid
     * @return the OeMuuid
     */
    public String getOeMuuid() {
        return oeMuuid;
    }

    /**
     * @param SensorMLuuid the SensorMLuuid to set
     */
    public void setSensorMLuuid(String SensorMLuuid) {
        this.sensorMLuuid = SensorMLuuid;
    }

    /**
     * @param OeMuuid the OeMuuid to set
     */
    public void setOeMuuid(String OeMuuid) {
        this.oeMuuid = OeMuuid;
    }
    
    
}
