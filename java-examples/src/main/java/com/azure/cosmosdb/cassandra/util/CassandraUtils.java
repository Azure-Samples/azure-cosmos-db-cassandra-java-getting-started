package com.azure.cosmosdb.cassandra.util;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;

import java.io.IOException;

import static java.lang.System.out;

/**
 * Cassandra utility class to handle the Cassandra Sessions
 */
public class CassandraUtils {

    private Cluster cluster;
    private Configurations config = new Configurations();

    public Session getSession() throws IOException {
        String host = config.getProperty("cassandra_host");
        int port = Integer.parseInt(config.getProperty("cassandra_port"));
        String username = config.getProperty("cassandra_username");
        String password = config.getProperty("cassandra_password");

        this.cluster = Cluster.builder().addContactPoint(host).withPort(port).withCredentials(username, password).build();        
        return cluster.connect();
    }

    public Cluster getCluster() {
        return cluster;
    }

    /**
     * Closes the cluster and Cassandra session
     */
    public void close() {
        cluster.close();
    }
}
