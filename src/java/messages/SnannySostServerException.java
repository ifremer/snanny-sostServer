/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package messages;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/** Web application Exception class for snanny-sostServer
 *
 * @author mlarour
 */
public class SnannySostServerException extends WebApplicationException
{
    private final String message;
    
    /** Web application Exception constructor, log error to application server
     * 
     * @param message error message
     * @param status Servlet status 
     */
    public SnannySostServerException(String message,Status status)
    {
        super(status);
        this.message = message;
        System.out.println("SensorNanny Exception : "+getMessage());
    }
    
    /** Getter for error message (java message completed with sos message)
     * 
     * @return the error message
     */
    @Override
    public final String getMessage()
    {
        return(super.getMessage()+" ("+message+")");
    }
         
    
}
