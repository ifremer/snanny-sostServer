<%-- 
    Document   : index
    Created on : Jun 6, 2014, 9:34:16 AM
    Author     : mlarour
--%>

<%@page import="sos.Sos"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="stylesheet.css" title="Style">
        <title>snanny-sostServer</title>
    </head>
    <body>           
        <%
            String usageUrl;
            String sampleUrl;            
            String sampleProcedure = request.getRequestURL()+"record/"+Sos.SAMPLE_SYSTEM_UUID;
            String sampleObservation = request.getRequestURL()+"record/"+Sos.SAMPLE_OBSERVATION_UUID;                                            
        %>        
        <!-- sos servlet -->
        <div class="topNav">
            <ul class="navList">
                <li><%out.print(sos.Sos.getInfo());%></li>
            </ul>        
        </div>        
        <div class="contentContainer">
            <div>
                
                <%                    
                   if(!sos.Sos.preloaded())
                   {
                      out.print("<h3>Preload xsd from the web</h3><a href=\""+request.getRequestURL()+"sos?preload\"><img src=\"resources/reload.png\" title=\"preload (get xsd from the web)\"></a>"); 
                   }
                %>                
            </div>                            
            <div class="summary">                       
                <h3>Request Summary</h3>            
                <table class="overviewSummary" cellspacing="0" cellpadding="3" border="0" summary="available requests">
                    <caption>
                       <span>Requests</span>
                       <span class="tabEnd"></span>
                    </caption>
                    <tbody>
                        <tr>
                            <th class="colFirst" scope="col">Name</th>
                            <th class="colLast" scope="col">Description</th>
                        </tr>
                        <tr class="altColor">
                            <td class="colFirst"><code>getCapabilities</code></td>
                            <td class="colLast">
                                <div class="block">returns an XML service description with information about the interface (offered operations and endpoints) as well as the available sensor data, such as the period for which sensor data is available, sensors that produce the measured values, or phenomena that are observed (for example air temperature).<br/>
                                    <hr />
                                    <b>Usage : </b>
                                    <br />
                                    <%
                                        usageUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=getCapabilities";
                                        out.print(usageUrl);
                                    %>
                                    <hr />
                                    <b>Sample : </b><br />
                                    <br />
                                    <%
                                        sampleUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=getCapabilities";
                                        out.print("<a href=\""+sampleUrl+"\">"+sampleUrl+"</a>");
                                    %>
                                </div>
                            </td>
                        </tr>
                        <tr class="rowColor">
                            <td class="colFirst"><code>describeSensor</code></td>
                            <td class="colLast">
                                <div class="block">provides sensor metadata in SensorML. The sensor description can contain information about the sensor in general, the identifier and classification, position and observed phenomena, but also details such as calibration data.
                                    <hr />
                                    <b>Usages : </b>
                                    <br />                                    
                                    <%      
                                        
                                        usageUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=describeSensor&procedure=&lt;procedure id&gt;&responseFormat=&lt;format&gt;";
                                        out.print(usageUrl);
                                    %>
                                    <ul>
                                        <li>&lt;procedure id&gt; is the persistent URL of the procedure
                                        <%
                                            out.print(" (e.g."+sampleProcedure+")");
                                        %>
                                        </li>
                                        <li>&lt;format&gt; application/json;subtype="http://www.opengis.net/om/2.0" or text/xml;subtype="http://www.opengis.net/om/2.0"</li>
                                    </ul>
                                    <hr />
                                    <b>Samples : </b>
                                    <br />
                                    <%
                                        sampleUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=describeSensor&procedure="+sampleProcedure+"&responseFormat=application/json;subtype=&quot;http://www.opengis.net/om/2.0&quot";
                                        out.print("<a href=\""+sampleUrl+"\">"+sampleUrl+"</a>");
                                        out.print("<br />");
                                        sampleUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=describeSensor&procedure="+sampleProcedure+"&responseFormat=text/xml;subtype=&quot;http://www.opengis.net/om/2.0&quot;";
                                        out.print("<a href=\""+sampleUrl+"\">"+sampleUrl+"</a>");
                                        out.print("<br />");
                                    %>                                    
                                </div>
                            </td>
                        </tr>
                        <tr class="altColor">
                            <td class="colFirst"><code>getObservation</code></td>
                            <td class="colLast">
                                <div class="block">allows pull-based querying of observed values, including their metadata. The measured values and their metadata is returned in the Observations and Measurements format (O & M).
                                <hr />
                                <b>Not implemented For a version 0</b>
                                </div>
                            </td>
                        </tr>
                        <tr class="rowColor">
                            <td class="colFirst"><code>getObservationById</code></td>
                            <td class="colLast">
                                <div class="block">allows to query a specific observation using an identifier returned by the service as response to an InsertObservation operation.
                                <hr />
                                    <b>Usages : </b>
                                    <br />
                                    <%                    
                                        usageUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=getObservationById&observation=&lt;observation id&gt;&responseFormat=&lt;format&gt;";
                                        out.print(usageUrl);
                                    %>
                                    <ul>
                                        <li>&lt;observation&gt; id is the persistent URL of the observation
                                        <%
                                            out.print(" (e.g."+sampleObservation+")");
                                        %>
                                        </li>
                                        <li>&lt;format&gt; application/json;subtype="http://www.opengis.net/om/2.0" or text/xml;subtype="http://www.opengis.net/om/2.0"</li>
                                    </ul>
                                    <hr />
                                    <b>Samples : </b>
                                    <br />
                                    <%
                                        sampleUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=getObservationById&observation="+sampleObservation+"&responseFormat=application/json;subtype=&quot;http://www.opengis.net/om/2.0&quot";
                                        out.print("<a href=\""+sampleUrl+"\">"+sampleUrl+"</a>");
                                        out.print("<br />");
                                        sampleUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=getObservationById&observation="+sampleObservation+"&responseFormat=text/xml;subtype=&quot;http://www.opengis.net/om/2.0&quot;";
                                        out.print("<a href=\""+sampleUrl+"\">"+sampleUrl+"</a>");
                                        out.print("<br />");
                                    %>                                    
                                </div>
                            </td>
                        </tr>
                        <tr class="altColor">
                            <td class="colFirst"><code>getResult</code></td>
                            <td class="colLast">
                                <div class="block">provides the ability to query for sensor readings without the metadata given consistent metadata (e.g. sensor, observed object).
                                <hr />
                                <b>Not implemented For a version 0</b>
                                </div>
                            </td>
                        </tr>
                        <tr class="rowColor">
                            <td class="colFirst"><code>insertSensor (RegisterSensor in official SOS protocol ?)</code></td>
                            <td class="colLast">
                                <div class="block">allows to register a new sensor in an deployed SOS.
                                <hr />
                                    <b>Usage : </b>
                                    <br />
                                    <%
                                        usageUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=insertSensor";
                                        out.print(usageUrl);
                                    %>
                                    with a POST query string
                                    <hr />
                                    <b>Sample : </b><br />
                                    <br />
                                    <b>curl -X POST -d @xmlToPost.xml '<%out.print(usageUrl);%>' --header "Content-Type:text/xml"</b><br/>
                                    <br/>
                                    <b>posted query string :</b><br/>
                                    <i>&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;swes:InsertSensor service="SOS" version="2.0.0"
    xmlns:swes="http://www.opengis.net/swes/2.0"
    xmlns:sos="http://www.opengis.net/sos/2.0"
    xmlns:swe="http://www.opengis.net/swe/2.0"
    xmlns:sml="http://www.opengis.net/sensorml/2.0"
    xmlns:gml="http://www.opengis.net/gml/3.2"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:gmd="http://www.isotc211.org/2005/gmd" 
    xmlns:gco="http://www.isotc211.org/2005/gco" 
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://www.opengis.net/sos/2.0 http://schemas.opengis.net/sos/2.0/sosInsertSensor.xsd   http://www.opengis.net/swes/2.0 http://schemas.opengis.net/swes/2.0/swes.xsd"&gt;
 &lt;swes:procedureDescriptionFormat&gt;<b>[format]</b>&lt;/swes:procedureDescriptionFormat&gt;
&lt;swes:procedureDescription&gt;
<b>[sensorML record]</b>
&lt;/swes:procedureDescription&gt;
&lt;swes:observableProperty&gt;&lt;/swes:observableProperty&gt;   
&lt;/swes:InsertSensor&gt; 
                                    </i>
                                    <ul>
                                        <li><b>[format]</b> application/json;subtype="http://www.opengis.net/om/2.0" or text/xml;subtype="http://www.opengis.net/om/2.0"</li>
                                        <li><b>[sensorML record]</b> is the sensor description in the format above (xml or json)</li>                                        
                                        </li>
                                        </ul>
                                </div>
                            </td>
                        </tr>
                        <tr class="altColor">
                            <td class="colFirst"><code>deleteSensor</code></td>
                            <td class="colLast">
                                <div class="block">allows to delete a sensor in an deployed SOS.
                                <hr/>
                                <b>Usages : </b>
                                    <br />
                                    <%                    
                                        usageUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=deleteSensor&procedure=&lt;procedure id&gt;";
                                        out.print(usageUrl);
                                    %>
                                    <ul>
                                        <li>&lt;procedure&gt; id is the persistent URL of the procedure
                                        <%
                                            out.print(" (e.g."+sampleProcedure+")");
                                        %>
                                        </li>
                                    </ul>
                                    <hr />
                                    <b>Samples : </b>
                                    <br />
                                    <%
                                        sampleUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=deleteSensor&procedure="+sampleProcedure;
                                        out.print("<a href=\""+sampleUrl+"\">"+sampleUrl+"</a>");
                                        out.print("<br />");                                        
                                    %>
                                </div>
                            </td>
                        </tr>
                        <tr class="rowColor">
                            <td class="colFirst"><code>insertObservation</code></td>
                            <td class="colLast">
                                <div class="block">can be used to insert data for already registered sensors in the SOS.
                                <hr />
                                    <b>Usage : </b>
                                    <br />
                                    <%
                                        usageUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=insertObservation";
                                        out.print(usageUrl);
                                    %>
                                    with a POST query string
                                    <hr />
                                    <b>Sample : </b><br />
                                    <br />
                                    <b>curl -X POST -d @xmlToPost.xml '<%out.print(usageUrl);%>' --header "Content-Type:text/xml"</b><br/>
                                    <br/>
                                    <b>posted query string :</b><br/>
                                    <i>&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;sos:InsertObservation service="SOS" version="2.0.0"
    xmlns:sos="http://www.opengis.net/sos/2.0"
    xmlns:swes="http://www.opengis.net/swes/2.0"
    xmlns:swe="http://www.opengis.net/swe/2.0"
    xmlns:sml="http://www.opengis.net/sensorML/1.0.1"
    xmlns:gml="http://www.opengis.net/gml/3.2"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:om="http://www.opengis.net/om/2.0"
    xmlns:sams="http://www.opengis.net/samplingSpatial/2.0"
    xmlns:sf="http://www.opengis.net/sampling/2.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/sos/2.0 http://schemas.opengis.net/sos/2.0/sos.xsd          http://www.opengis.net/samplingSpatial/2.0 http://schemas.opengis.net/samplingSpatial/2.0/spatialSamplingFeature.xsd"&gt;
    &lt;!-- multiple offerings are possible --&gt;
    &lt;sos:offering&gt;[Offering]&lt;/sos:offering&gt;

    &lt;sos:observation&gt;
       [O&amp;M record] 
    &lt;/sos:observation&gt;
&lt;/sos:InsertObservation&gt;</i>
                                    <ul>
                                        <li><b>[Offering]</b> is the group of observation (dataset) to which the current observation is submitted (for example frenchResearchVessels). <b>It is not managed here</b></li>
                                        <li><b>[O&amp;M record]</b> is the observation description in XML</li>                                        
                                        </li>
                                        </ul>
                                </div>
                            </td>
                        </tr>
                        <tr class="altColor">
                            <td class="colFirst"><code>deleteObservation (not in official SOS protocol ?)</code></td>
                            <td class="colLast">
                                <div class="block">can be used to delete data for already registered sensors in the SOS.
                                    <hr/>
                                <b>Usages : </b>
                                    <br />
                                    <%                    
                                        usageUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=deleteObservation&observation=&lt;observation id&gt;";
                                        out.print(usageUrl);
                                    %>
                                    <ul>
                                        <li>&lt;observation&gt; id is the persistent URL of the observation
                                        <%
                                            out.print(" (e.g."+sampleObservation+")");
                                        %>
                                        </li>
                                    </ul>
                                    <hr />
                                    <b>Samples : </b>
                                    <br />
                                    <%
                                       sampleUrl = request.getRequestURL()+"sos?service=SOS&version=2.0&request=deleteObservation&observation="+sampleObservation;
                                        out.print("<a href=\""+sampleUrl+"\">"+sampleUrl+"</a>");
                                        out.print("<br />");                                        
                                    %>
                                </div>
                            </td>
                        </tr>
                        <tr class="rowColor">
                            <td class="colFirst"><code>insertResult</code></td>
                            <td class="colLast">
                                <div class="block">To Be completed
                                <hr />
                                <b>Not implemented For a version 0</b>
                                </div>
                            </td>
                        </tr>
                        <tr class="altColor">
                            <td class="colFirst"><code>deleteResult</code></td>
                            <td class="colLast">
                                <div class="block">To Be completed
                                <hr />
                                <b>Not implemented For a version 0</b>
                                </div>
                            </td>
                        </tr>                            
                    </tbody>
                </table>                         
            </div>            
        </div>    
        
        <br/>
        
        <!-- result servlet -->
        <div class="topNav">
            <ul class="navList">
                <li>RECORD SERVER</li>
            </ul>            
        </div>
        <div class="header">
            <h2>Usage :</h2>
            <h4 class="title">
                <%
                    usageUrl = request.getRequestURL()+"record/&lt;uuid&gt;";
                    out.print(usageUrl);
                %>
            </h4>
            <br/>
            <hr />
            <h2>Sample :</h2>
            <h4 class="title">
                <i>procedure : </i>
                <%
                    sampleUrl = sampleProcedure;
                    out.print("<a href=\""+sampleUrl+"\">"+sampleUrl+"</a>");
                %>
                <br /><i>observation : </i>
                <%   
                    sampleUrl = sampleObservation;
                    out.print("<a href=\""+sampleUrl+"\">"+sampleUrl+"</a>");
                %>
            </h4>
            <br />
        </div>    
            
    </body>
</html>
