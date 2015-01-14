/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sos;

import com.couchbase.client.java.PersistTo;
import com.couchbase.client.java.ReplicateTo;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.transcoder.JsonArrayTranscoder;
import com.couchbase.client.java.transcoder.JsonTranscoder;
import config.SnannySostServerConfig;
import messages.SnannySostServerException;
import messages.SnannySostServerMessages;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;
import messages.Success;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONML;
import org.json.JSONObject;
import sos.SosRequest.SOSREQUEST;
import sos.SosFormat.SOSFORMAT;
import static sos.SosFormat.SOSFORMAT.XML;
import sos.SosService.SOSSERVICE;
import sos.SosVersion.SOSVERSION;
import sos.couchbase.CouchbaseManager;
import sos.insert.InsertObservation;
import sos.insert.InsertSensor;
import validation.SosValidation;

/** Sos server is this servlet
 *
 * @author mlarour
 */
@WebServlet(name = "Sos", urlPatterns = {"/sos"})
public class Sos extends HttpServlet {        
    
    private static String SERVLET_PRELOAD = "preload";
    private static boolean preloaded = false;
    private static String info = "snanny-sostServer";
    public static String SAMPLE_SYSTEM_UUID = "snimport-web0-0000-0000-417685250788";
    public static String SAMPLE_OBSERVATION_UUID = "snimport-web0-0000-0000-417685250790";

    /**
     * @return the info
     */
    public static String getInfo() {
        return info;
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @param post true for post method
     * @throws SnannySostServerException for bad request or service unavailable
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response,boolean post)
            throws SnannySostServerException
    {
        long t1 = System.currentTimeMillis();
        System.out.println("Sos.processRequest starts");
        // retreive configuration
        SnannySostServerConfig snannySostServerConfig = SnannySostServerConfig.singleton(request.getServletContext());
        info = "snanny-sostServer"+snannySostServerConfig.getExtraInfo();
        
        // way to force initialization
        if(request.getParameter(SERVLET_PRELOAD) != null)
        {
            if(!preloaded)
            {
                System.out.println("Sos.processRequest preload");                                                               
                
                SosValidation.singleton(snannySostServerConfig);
                InsertSensor.singleton();
                InsertObservation.singleton();
                
                System.setProperty("http.proxyHost",snannySostServerConfig.getSquidHost());
                System.setProperty("http.proxyPort",snannySostServerConfig.getSquidPort());
                System.out.println("System.setProperty(\"http.proxyHost\","+snannySostServerConfig.getSquidHost()+");");
                System.out.println("System.setProperty(\"http.proxyPort\","+snannySostServerConfig.getSquidPort()+");");
                        
            }
            preloaded = true;
            try
            {
                long t2 = System.currentTimeMillis();
                long t = t2-t1;
                System.out.println("Sos.processRequest stops after : "+t+"ms");
                response.sendRedirect("/snanny-sostServer/");
                return;                
            }
            catch(IOException lost){}
        }
                   
        // get servlet response writer
        PrintWriter out = getPrintWriter(response);                                        
        
        // read request parameter                   
        SOSREQUEST sosRequest = getRequestParameter(request);
        
        String uuid;
        
        // process sos request
        switch(sosRequest)
        {            
            case getCapabilities :     getServiceParameter(request);
                                       getVersionParameter(request);
                                       getCapabilities(request,response,out,snannySostServerConfig);
                                       break;
            case describeSensor :      getServiceParameter(request);
                                       getVersionParameter(request);
                                       describeSensor(request,response,out,snannySostServerConfig);
                                       break;
            case getObservationById :  getServiceParameter(request);
                                       getVersionParameter(request);
                                       getObservationById(request,response,out,snannySostServerConfig);
                                       break;                
            case insertSensor       :  System.out.println("Sos.processRequest insertSensor");        
                                       checkPost(post, sosRequest);
                                       InsertSensor.singleton().insert(snannySostServerConfig,getPostContent(request,snannySostServerConfig),response,out);
                                       break;            
            case deleteSensor       :  getServiceParameter(request);
                                       getVersionParameter(request);
                                       uuid = getSosProcedureUuid(request);
                                       deleteSensor(request,response,out,snannySostServerConfig,uuid);
                                       break;            
            case insertObservation  :  checkPost(post, sosRequest);
                                       InsertObservation.singleton().insert(snannySostServerConfig,getPostContent(request,snannySostServerConfig),response,out);
                                       break;                
            case deleteObservation  :  getServiceParameter(request);
                                       getVersionParameter(request);
                                       uuid = getSosObservationUuid(request);
                                       deleteObservation(request,response,out,snannySostServerConfig,uuid);
                                       break;
            
            //For a version 0, some requests are not implemented:
            case getObservation :    break;
            case getResult :         break;
            case insertResult:       break;
            case deleteResult:       break;
        }
                        
        // close response writer
        out.close();
        
        long t2 = System.currentTimeMillis();
        long t = t2-t1;
        System.out.println("Sos.processRequest stops after : "+t+"ms");
        
                
    }
    
    /** Getter for post content
     * 
     * @param request
     * @return the post content
     */
    private String getPostContent(HttpServletRequest request,SnannySostServerConfig snannySostServerConfig)
    {
        try
        {
            // Don't use request.getReader() , fix charset with InputStreamReader
            ServletInputStream  servletInputStream = request.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(request.getInputStream(),snannySostServerConfig.getCharset());
            StringBuilder buffer = new StringBuilder();
            char[] buf = new char[4 * 1024]; // 4Kchar buffer
            int len;
            while ((len = inputStreamReader.read(buf, 0, buf.length)) != -1)
            {                
               buffer.append(buf, 0, len);             
            }            
            return(buffer.toString());
        }
        catch(IOException ex)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_IO_POST,Status.SERVICE_UNAVAILABLE);
        }
    }
    /** check errors for insert without post content
     * 
     * @param post
     * @param sosRequest
     * @throws SnannySostServerException 
     */
    private void checkPost(boolean post,SOSREQUEST sosRequest) throws SnannySostServerException
    {
        switch(sosRequest)
        {
            case insertSensor : 
                    if(!post)
                    {
                       throw new SnannySostServerException(SnannySostServerMessages.ERROR_ONLY_POST_INSERTSENSOR,Status.SERVICE_UNAVAILABLE);
                    }
            
            case insertObservation :
                    if(!post)
                    {
                       throw new SnannySostServerException(SnannySostServerMessages.ERROR_ONLY_POST_INSERTOBSERVATION,Status.SERVICE_UNAVAILABLE);
                    }
        }
    }
    
    /** Getter for servlet response writer
     * 
     * @param response
     * @return the writer for servlet response
     */
    private PrintWriter getPrintWriter(HttpServletResponse response)
    {
        try            
        {
            return(response.getWriter());
        }
        catch(IOException ioe) 
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_RESPONSE_WRITER,Status.SERVICE_UNAVAILABLE);
        }   
    }
    
    /** Getter for sos service parameter value
     * 
     * @param request servlet request
     * @return the sos service
     */
    private SOSSERVICE getServiceParameter(HttpServletRequest request)
    {
        String serviceValue = request.getParameter(SosService.SOS_SERVICE_KEYWORD);
        if(serviceValue == null)
        {
           throw new SnannySostServerException(SnannySostServerMessages.ERROR_SERVICE_REQUIRED+SosService.getAvailableSosServices(),Status.BAD_REQUEST);
        }
        return(SosService.get(serviceValue));
    }
    
    /** Getter for sos version parameter value
     * 
     * @param request servlet request
     * @return the sos version
     */
    private SOSVERSION getVersionParameter(HttpServletRequest request)
    {
        String versionValue = request.getParameter(SosVersion.SOS_VERSION_KEYWORD);
        if(versionValue == null)
        {
           throw new SnannySostServerException(SnannySostServerMessages.ERROR_VERSION_REQUIRED+SosVersion.getAvailableSosVersions(),Status.BAD_REQUEST);
        }
        return(SosVersion.get(versionValue));
    }
      
    /** Getter for sos request parameter
     * 
     * @param request Http Servlet Request
     * @return the sos request
     */
    private SOSREQUEST getRequestParameter(HttpServletRequest request)
    {
        
        String requestValue = request.getParameter(SosRequest.SOS_REQUEST_KEYWORD);
        if(requestValue == null)
        {
           throw new SnannySostServerException(SnannySostServerMessages.ERROR_REQUEST_REQUIRED+SosRequest.getAvailableSosRequests(),Status.BAD_REQUEST);
        }
        SOSREQUEST sosRequest = SosRequest.get(requestValue);
        if(!SosRequest.isAvailable(sosRequest))
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_REQUEST_NOT_IMPLEMENTED+SosRequest.getAvailableSosRequests(),Status.BAD_REQUEST);        
        }
        return(sosRequest);
    }
    
    /** Getter for preloaded state of SOS server (preload perform xsd download from the web)
     * 
     * @return true if preloaded
     */
    public static boolean preloaded()
    {
        return(preloaded);
    }
           
    /** returns an XML service description with information about the interface 
     * (offered operations and endpoints) as well as the available sensor data,
     * such as the period for which sensor data is available,
     * sensors that produce the measured values, 
     * or phenomena that are observed (for example air temperature).
     * 
     * @param request Http Servlet Request
     * @param response Http Servlet Response
     * @param out PrintWriter for Http Servlet Response
     * @param snannySostServerConfig the sos server configuration
     * @throws SnannySostServerException  server exception (I/O problem)
     */
    private void getCapabilities(HttpServletRequest request,HttpServletResponse response,PrintWriter out,SnannySostServerConfig snannySostServerConfig) throws SnannySostServerException
    {
        try
        {                        
            InputStream in = snannySostServerConfig.getCababilitiesInputstream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            response.setContentType("application/xml;charset="+snannySostServerConfig.getCharset().name());                        
            while ((line = reader.readLine()) != null)
            {
                out.append(line);
            }
            reader.close();
        }
        catch(IOException ioe)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_getCapabilities,Status.SERVICE_UNAVAILABLE);      
        }
    }
    
    /** Getter for uuid of procedure parameter from the persistent URL
     * 
     * @param request Http Servlet Request
     * @return the uuid
     */
    private String getSosProcedureUuid(HttpServletRequest request) throws SnannySostServerException
    {
        // read procedure parameter
        String procedureValue = request.getParameter(SosProcedure.SOS_PROCEDURE_KEYWORD);
        if(procedureValue == null)
        {
           throw new SnannySostServerException(SnannySostServerMessages.ERROR_PROCEDURE_REQUIRED,Status.BAD_REQUEST);
        }
        return(SosProcedure.getUuid(procedureValue));
    }
    /** Getter for format(eg XML or JSON)
     * 
     * @param request Http Servlet Request
     * @return the sos format (eg XML or JSON)
     * @throws SnannySostServerException 
     */
    private SOSFORMAT getResponseFormatParameter(HttpServletRequest request) throws SnannySostServerException
    {
        String responseFormatValue = request.getParameter(SosFormat.SOS_RESPONSE_FORMAT_KEYWORD);
        if(responseFormatValue == null)
        {
           throw new SnannySostServerException(SnannySostServerMessages.ERROR_RESPONSE_FORMAT_REQUIRED+SosFormat.getAvailableSosResponseFormats(),Status.BAD_REQUEST);
        }
        return(SosFormat.getFormat(responseFormatValue));
    }
    
    /** Getter for the observation uuid from the persistent URL
     * 
     * @param request Http Servlet Request
     * @return the observation uuid 
     */
    private String getSosObservationUuid(HttpServletRequest request)
    {
        String observationValue = request.getParameter(SosObservation.SOS_OBSERVATION_KEYWORD);        
        if(observationValue == null)
        {
           throw new SnannySostServerException(SnannySostServerMessages.ERROR_OBSERVATION_REQUIRED,Status.BAD_REQUEST);
        }
        return(SosObservation.getUuid(observationValue));
    }
    
    /**provides sensor metadata in SensorML. The sensor description can contain information about the sensor in general,
     * the identifier and classification,position and observed phenomena, but also details such as calibration data. 
     * 
     * @param request Http Servlet Request
     * @param response Http Servlet Response
     * @param out PrintWriter for Http Servlet Response
     * @param snannySostServerConfig the sos server configuration
     * @throws SnannySostServerException (I/O or conversion problem)
     */
    private void describeSensor(HttpServletRequest request,HttpServletResponse response,PrintWriter out,SnannySostServerConfig snannySostServerConfig) throws SnannySostServerException
    {                
        String uuid = getSosProcedureUuid(request);
        SOSFORMAT sosresponseformat = getResponseFormatParameter(request);
        if(snannySostServerConfig.isCouchbase())
        {
            try
            {                                
               JsonDocument jsonDocument = CouchbaseManager.getSystemBucket().get(uuid);
                              
               if(jsonDocument == null)
               {
                   throw new SnannySostServerException(SnannySostServerMessages.ERROR_NO_UUID_RECORD_1of2+
                                              uuid+
                                              SnannySostServerMessages.ERROR_NO_UUID_RECORD_2of2,
                                              Status.NOT_FOUND); 
               }                              
               response.setContentType(SosFormat.getContentType(sosresponseformat));
               
               switch(sosresponseformat)
               {
                    case JSON :  if(snannySostServerConfig.isJsonObject())
                                 {
                                     out.append(new JsonTranscoder().jsonObjectToString(jsonDocument.content().getObject(SnannySostServerConfig.FilejsonField)));
                                 }
                                 else
                                 {
                                    out.append(new JsonArrayTranscoder().jsonArrayToString(jsonDocument.content().getArray(SnannySostServerConfig.FilejsonField)));
                                 }                                  
                                 break;

                    case XML :  if(snannySostServerConfig.isJsonObject())
                                {
                                    JSONObject jSONObject = new JSONObject(StringEscapeUtils.unescapeXml(new JsonTranscoder().jsonObjectToString(jsonDocument.content().getObject(SnannySostServerConfig.FilejsonField))));
                                    out.append(JSONML.toString(jSONObject));
                                }
                                else
                                {
                                    JSONArray jsonArray = new JSONArray(StringEscapeUtils.unescapeXml(new JsonArrayTranscoder().jsonArrayToString(jsonDocument.content().getArray(SnannySostServerConfig.FilejsonField))));
                                    out.append(JSONML.toString(jsonArray));
                                }                                
                                break;                    
               } 
               
            }
            catch(Exception ex)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_COUCHBASE_ERROR+ex.getMessage(),Status.SERVICE_UNAVAILABLE);
            }
        }
        else
        {
            File uuidFile = snannySostServerConfig.getSensorMlFile(uuid);                
            try
            {
               
               byte[] encoded = Files.readAllBytes(Paths.get(uuidFile.toURI()));
               String uuidFileContent = new String(encoded, snannySostServerConfig.getCharset());
               response.setContentType(SosFormat.getContentType(sosresponseformat)); 
               switch(sosresponseformat)
               {
                    case JSON :  if(snannySostServerConfig.isJsonObject())
                                 {
                                    JSONObject jSONObject = JSONML.toJSONObject(uuidFileContent);
                                    out.append(jSONObject.toString());
                                 }
                                 else
                                 {                                   
                                    JSONArray jsonArray = JSONML.toJSONArray(uuidFileContent);
                                    out.append(jsonArray.toString());
                                 }
                                 break;

                    case XML :  out.append(uuidFileContent);
                                break;                    
               }           
            }
            catch(IOException io)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_describeSensor,Status.SERVICE_UNAVAILABLE);
            }     
            catch(JSONException je)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_XML_JSON_CONVERSION+je.getMessage(),Status.SERVICE_UNAVAILABLE);
            }
        }
    }            
    
    /**allows to query a specific observation using an identifier returned by the service as response to an InsertObservation operation. 
     * 
     * @param request Http Servlet Request
     * @param response Http Servlet Response
     * @param out PrintWriter for Http Servlet Response
     * @param snannySostServerConfig the sos server configuration
     * @throws SnannySostServerException 
     */
    private void  getObservationById(HttpServletRequest request,HttpServletResponse response,PrintWriter out,SnannySostServerConfig snannySostServerConfig) throws SnannySostServerException
    {                        
        String uuid = getSosObservationUuid(request);
        SOSFORMAT sosresponseformat = getResponseFormatParameter(request);
        if(snannySostServerConfig.isCouchbase())
        {
            try
            {                                
               JsonDocument jsonDocument = CouchbaseManager.getObservationBucket().get(uuid);
                              
               if(jsonDocument == null)
               {
                   throw new SnannySostServerException(SnannySostServerMessages.ERROR_NO_UUID_RECORD_1of2+
                                              uuid+
                                              SnannySostServerMessages.ERROR_NO_UUID_RECORD_2of2,
                                              Status.NOT_FOUND); 
               }                              
               response.setContentType(SosFormat.getContentType(sosresponseformat));
               switch(sosresponseformat)
               {
                    case JSON :  if(snannySostServerConfig.isJsonObject())
                                 {
                                     out.append(new JsonTranscoder().jsonObjectToString(jsonDocument.content().getObject(SnannySostServerConfig.FilejsonField)));
                                 }                                   
                                 else   
                                 {
                                     out.append(new JsonArrayTranscoder().jsonArrayToString(jsonDocument.content().getArray(SnannySostServerConfig.FilejsonField)));
                                 }                                  
                                 break;

                    case XML :  if(snannySostServerConfig.isJsonObject())
                                {
                                    JSONObject jSONObject = new JSONObject(StringEscapeUtils.unescapeXml(new JsonTranscoder().jsonObjectToString(jsonDocument.content().getObject(SnannySostServerConfig.FilejsonField))));
                                    out.append(JSONML.toString(jSONObject));
                                }
                                else
                                {
                                    JSONArray jsonArray = new JSONArray(StringEscapeUtils.unescapeXml(new JsonArrayTranscoder().jsonArrayToString(jsonDocument.content().getArray(SnannySostServerConfig.FilejsonField))));                                
                                    out.append(JSONML.toString(jsonArray));
                                }                                
                                break;                    
               } 
                
               
            }
            catch(Exception ex)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_COUCHBASE_ERROR+ex.getMessage(),Status.SERVICE_UNAVAILABLE);
            }
        }
        else
        {
            File uuidFile = SnannySostServerConfig.singleton(request.getServletContext()).getOemFile(uuid);        
            try
            {
               byte[] encoded = Files.readAllBytes(Paths.get(uuidFile.toURI()));
               String uuidFileContent = new String(encoded, snannySostServerConfig.getCharset());
               response.setContentType(SosFormat.getContentType(sosresponseformat));                        
               switch(sosresponseformat)
               {
                    case JSON : if(snannySostServerConfig.isJsonObject())
                                {
                                    JSONObject jsonObject = JSONML.toJSONObject(uuidFileContent);
                                    out.append(jsonObject.toString());
                                }
                                else
                                {
                                    JSONArray jsonArray = JSONML.toJSONArray(uuidFileContent);
                                    out.append(jsonArray.toString());
                                }
                                
                                break;

                    case XML :  out.append(uuidFileContent);
                                break;                    
               }                     
            }
            catch(Exception ex)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_describeSensor,Status.SERVICE_UNAVAILABLE);
            }
        }
    }
    
    
    /** allows to delete a sensor in an deployed SOS. 
     * 
     * @param request Http Servlet Request
     * @param response Http Servlet Response
     * @param out PrintWriter for Http Servlet Response
     * @param snannySostServerConfig the sos server configuration
     * @param uuid the uuid of the deployed sensor
     * @throws SnannySostServerException (I/O problem)
     */
    private void  deleteSensor(HttpServletRequest request,HttpServletResponse response,PrintWriter out,SnannySostServerConfig snannySostServerConfig,String uuid) throws SnannySostServerException
    {        
        if(snannySostServerConfig.isCouchbase())
        {
            try                
            {
                if(CouchbaseManager.getSystemBucket().remove(uuid,
                                                             PersistTo.MASTER,
                                                             ReplicateTo.ONE,
                                                             snannySostServerConfig.getCouchbaseTimeOutMS(),
                                                             TimeUnit.MILLISECONDS) == null)
                {
                    System.out.println("*************************************** deleteSensor null");
                }
            }
            catch(Exception ex)                
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_COUCHBASE_deleteSensor+ex.getMessage(),Status.SERVICE_UNAVAILABLE);
            }
        }
        else
        {
            File uuidFile = snannySostServerConfig.getSensorMlFile(uuid);         
            Path fp = uuidFile.toPath();
            try
            {
                Files.delete(fp);
            }
            catch(IOException io)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_IO_deleteSensor+io.getMessage(),Status.SERVICE_UNAVAILABLE);
            }    
        }
        Success.submit(SnannySostServerMessages.DELETE_SENSOR_OK_1of2+uuid+SnannySostServerMessages.DELETE_SENSOR_OK_2of2,response,out,snannySostServerConfig);
    }
    
    
    
    /** can be used to delete data for already registered sensors in the SOS. 
     * 
     * @param request Http Servlet Request
     * @param response Http Servlet Response
     * @param out PrintWriter for Http Servlet Response
     * @param snannySostServerConfig
     * @param uuid the uuid of the registered data
     * @throws SnannySostServerException (I/O problem)
     */
    private void  deleteObservation(HttpServletRequest request,HttpServletResponse response,PrintWriter out,SnannySostServerConfig snannySostServerConfig,String uuid) throws SnannySostServerException
    {
        if(snannySostServerConfig.isCouchbase())
        {
            try                
            {
                if(CouchbaseManager.getSystemBucket().remove(uuid,
                                                             PersistTo.MASTER,
                                                             ReplicateTo.ONE,
                                                             snannySostServerConfig.getCouchbaseTimeOutMS(),
                                                             TimeUnit.MILLISECONDS) == null)
                {
                    System.out.println("*************************************** deleteObservation null");
                }    
            }
            catch(Exception ex)                
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_COUCHBASE_deleteObservation+ex.getMessage(),Status.SERVICE_UNAVAILABLE);
            }
        }
        else
        {
            File uuidFile = snannySostServerConfig.getOemFile(uuid);
            Path fp = uuidFile.toPath();
            try
            {
                Files.delete(fp);
            }
            catch(IOException io)
            {
                throw new SnannySostServerException(SnannySostServerMessages.ERROR_IO_deleteObservation+io.getMessage(),Status.SERVICE_UNAVAILABLE);
            }
        }
        Success.submit(SnannySostServerMessages.DELETE_OBSERVATION_OK_1of2+uuid+SnannySostServerMessages.DELETE_OBSERVATION_OK_2of2,response,out,snannySostServerConfig);
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response,false);               
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response,true);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "SOS SERVER";
    }// </editor-fold>

    }
