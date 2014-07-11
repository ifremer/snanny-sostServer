/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package record;

import config.SensorNannyConfig;
import java.io.File;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * REST Web Service
 *
 * @author mlarour
 */
@Path("/{uuid}")
public class Record {
    
    
    @Context ServletContext servletContext;
        
    
    /**
     * Creates a new instance of Record
     */
    public Record()
    {
        
    }

    @GET
    @Produces("application/xml")
    public Response getXml(@PathParam("uuid")  String uuid)  
    {
        File uuidFile = SensorNannyConfig.singleton(servletContext).getSensorMlFile(uuid);
        ResponseBuilder response = Response.ok((Object) uuidFile);        
        return response.build();        
    }        

}
