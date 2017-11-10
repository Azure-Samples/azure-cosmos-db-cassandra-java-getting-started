package com.azure.cosmosdb.cassandra.util;

import com.datastax.driver.core.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Cassandra utility class to handle the Cassandra Sessions
 */
public class CassandraUtils {

    private Cluster cluster;
    private Configurations config = new Configurations();

    public Session getSession() throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException {
        String host = config.getProperty("cassandra_host");
        int port = Integer.parseInt(config.getProperty("cassandra_port"));
        String username = config.getProperty("cassandra_username");
        String password = config.getProperty("cassandra_password");

        final SSLContext sc = SSLContext.getInstance("TLSv1.2");

        sc.init(null, new TrustManager[]{new X509TrustManager() {
            private X509Certificate[] accepted;

            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                accepted = xcs;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return accepted;
            }
        }}, null);

        JdkSSLOptions sslOptions = RemoteEndpointAwareJdkSSLOptions.builder()
                .withSSLContext(sc)
                .build();

        this.cluster = Cluster.builder()
                .addContactPoint(host)
                .withPort(port)
                .withCredentials(username, password)
                .withSSL(sslOptions)
                .build();

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
