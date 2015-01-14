/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package messages;

import config.SnannySostServerConfig;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

/** Xml response for success message from SOS servlet
 *
 * @author mlarour
 */
public class Success
{
    /** submit xml succes message to servlet response and log messge to sos server 
     * 
     * @param message the succes message
     * @param response Servlet response
     * @param out PrintWriter of servlet response
     * @param snannySostServerConfig Sos server properties
     */
    public static void submit(String message,HttpServletResponse response,PrintWriter out,SnannySostServerConfig snannySostServerConfig)
    {
        response.setContentType("application/xml;charset="+snannySostServerConfig.getCharset().name());                        
        out.println("<SensorNannySuccess>"+message+"</SensorNannySuccess>");                
        System.out.println("SensorNanny Success : "+message);
    }
}
