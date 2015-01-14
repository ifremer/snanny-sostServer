/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sos.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import config.SnannySostServerConfig;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.Response.Status;
import messages.SnannySostServerException;
import messages.SnannySostServerMessages;

/**
 *
 * @author mlarour
 */
public class CouchbaseManager implements ServletContextListener
{
    private static Cluster cluster = null;
    private static Bucket systemBucket = null;    
    private static Bucket observationBucket = null;    

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        
        try
        {
            SnannySostServerConfig snannySostServerConfig = SnannySostServerConfig.singleton(sce.getServletContext());
            cluster = CouchbaseCluster.create(snannySostServerConfig.getCouchbaseHosts());
            systemBucket      = cluster.openBucket(snannySostServerConfig.getCouchbaseSystemBucket(),
                                                   snannySostServerConfig.getCouchbasePasswd(),
                                                   snannySostServerConfig.getCouchbaseTimeOutMS(),
                                                   TimeUnit.MILLISECONDS);            
            
            observationBucket = cluster.openBucket(snannySostServerConfig.getCouchbaseObservationBucket(),
                                                   snannySostServerConfig.getCouchbasePasswd(),
                                                   snannySostServerConfig.getCouchbaseTimeOutMS(),
                                                   TimeUnit.MILLISECONDS);
            
            
        }
        catch(Exception ex)
        {
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_COUCHBASE_ERROR+ex.getMessage(),Status.SERVICE_UNAVAILABLE);            
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
        if(cluster != null && !cluster.disconnect())
        {                
            throw new SnannySostServerException(SnannySostServerMessages.ERROR_COUCHBASE_DISCONNECT,Status.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * @return the System bucket
     */
    public static Bucket getSystemBucket() throws SnannySostServerException
    {                
        return systemBucket;
    }
    
    /**
     * @return the Observation bucket
     */
    public static Bucket getObservationBucket() throws SnannySostServerException
    {                
        return observationBucket;
    }
    
}
