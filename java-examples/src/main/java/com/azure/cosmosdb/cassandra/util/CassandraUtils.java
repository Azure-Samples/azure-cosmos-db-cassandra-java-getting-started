package com.azure.cosmosdb.cassandra.util;

import com.datastax.driver.core.*;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.*;

/**
 * Cassandra utility class to handle the Cassandra Sessions
 */
public class CassandraUtils {

    private Cluster cluster;
    private Configurations config = new Configurations();
    private String cassandraHost = "127.0.0.1";
    private int cassandraPort = 10350;
    private String cassandraUsername = "localhost";
    private String cassandraPassword = "defaultpassword";
    private File sslKeyStoreFile = null;
    private String sslKeyStorePassword = "changeit";


    /**
     * This method creates a Cassandra Session based on the the end-point details given in config.properties.
     * This method validates the SSL certificate based on ssl_keystore_file_path & ssl_keystore_password properties.
     * If ssl_keystore_file_path & ssl_keystore_password are not given then it uses 'cacerts' from JDK.
     * @return Session Cassandra Session
     */
    public Session getSession() {

        try {
            //Load cassandra endpoint details from config.properties
            loadCassandraConnectionDetails();

            final KeyStore keyStore = KeyStore.getInstance("JKS");
            try (final InputStream is = new FileInputStream(sslKeyStoreFile)) {
                keyStore.load(is, sslKeyStorePassword.toCharArray());
            }

            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                    .getDefaultAlgorithm());
            kmf.init(keyStore, sslKeyStorePassword.toCharArray());
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory
                    .getDefaultAlgorithm());
            tmf.init(keyStore);

            // Creates a socket factory for HttpsURLConnection using JKS contents.
            final SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new java.security.SecureRandom());

            JdkSSLOptions sslOptions = RemoteEndpointAwareJdkSSLOptions.builder()
                    .withSSLContext(sc)
                    .build();
            cluster = Cluster.builder()
                    .addContactPoint(cassandraHost)
                    .withPort(cassandraPort)
                    .withCredentials(cassandraUsername, cassandraPassword)
                    .withSSL(sslOptions)
                    .build();

            return cluster.connect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
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

    /**
     * Loads Cassandra end-point details from config.properties.
     * @throws Exception
     */
    private void loadCassandraConnectionDetails() throws Exception {
        cassandraHost = config.getProperty("cassandra_host");
        cassandraPort = Integer.parseInt(config.getProperty("cassandra_port"));
        cassandraUsername = config.getProperty("cassandra_username");
        cassandraPassword = config.getProperty("cassandra_password");
        String ssl_keystore_file_path = config.getProperty("ssl_keystore_file_path");
        String ssl_keystore_password = config.getProperty("ssl_keystore_password");

        // If ssl_keystore_file_path, build the path using JAVA_HOME directory.
        if (ssl_keystore_file_path == null || ssl_keystore_file_path.isEmpty()) {
            String javaHomeDirectory = System.getenv("JAVA_HOME");
            if (javaHomeDirectory == null || javaHomeDirectory.isEmpty()) {
                throw new Exception("JAVA_HOME not set");
            }
            ssl_keystore_file_path = new StringBuilder(javaHomeDirectory).append("/jre/lib/security/cacerts").toString();
        }

        sslKeyStorePassword = (ssl_keystore_password != null && !ssl_keystore_password.isEmpty()) ?
                ssl_keystore_password : sslKeyStorePassword;

        sslKeyStoreFile = new File(ssl_keystore_file_path);

        if (!sslKeyStoreFile.exists() || !sslKeyStoreFile.canRead()) {
            throw new Exception(String.format("Unable to access the SSL Key Store file from %s", ssl_keystore_file_path));
        }
    }
}
