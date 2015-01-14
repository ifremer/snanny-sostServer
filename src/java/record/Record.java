/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package record;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.transcoder.JsonArrayTranscoder;
import com.couchbase.client.java.transcoder.JsonTranscoder;
import config.SnannySostServerConfig;
import java.io.File;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import messages.SnannySostServerException;
import messages.SnannySostServerMessages;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONML;
import org.json.JSONObject;
import sos.couchbase.CouchbaseManager;

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
        SnannySostServerConfig snannySostServerConfig = SnannySostServerConfig.singleton(servletContext);
        ResponseBuilder response = null;
        if(snannySostServerConfig.isCouchbase())
        {
            JsonDocument jsonDocument = CouchbaseManager.getSystemBucket().get(uuid);
            if(jsonDocument == null)
            {
                jsonDocument = CouchbaseManager.getObservationBucket().get(uuid);
            }
            if(jsonDocument == null)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_NO_UUID_RECORD_1of2+
                                          uuid+
                                          SnannySostServerMessages.ERROR_NO_UUID_RECORD_2of2,
                                          Status.NOT_FOUND);
            }
            try
            {
                if(snannySostServerConfig.isJsonObject())
                {
                    JSONObject jSONObject = new JSONObject(StringEscapeUtils.unescapeXml(new JsonTranscoder().jsonObjectToString(jsonDocument.content().getObject(SnannySostServerConfig.FilejsonField))));
                    response = Response.ok((Object) JSONML.toString(jSONObject));
                }
                else
                {
                    JSONArray jsonArray = new JSONArray(StringEscapeUtils.unescapeXml(new JsonArrayTranscoder().jsonArrayToString(jsonDocument.content().getArray(SnannySostServerConfig.FilejsonField))));                                
                    response = Response.ok((Object)JSONML.toString(jsonArray));
                }
            }
            catch(Exception ex)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_COUCHBASE_ERROR+ex.getMessage(),Status.SERVICE_UNAVAILABLE);
            }            
        } 
        else
        {
           File uuidFile; 
           try
           {
               uuidFile = SnannySostServerConfig.singleton(servletContext).getSensorMlFile(uuid);
           }
           catch(SnannySostServerException ssse)
           {
               uuidFile = SnannySostServerConfig.singleton(servletContext).getOemFile(uuid);
           }
           response = Response.ok((Object) uuidFile);        
           
        }
        return response.build();
    }
}
