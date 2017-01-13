package com.example.ogema.connector.util_rest;

/**
 * Created by dnestle on 12.06.2015.
 */

        import java.security.cert.CertificateException;
        import java.security.cert.X509Certificate;
        import javax.net.ssl.X509TrustManager;

public class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
}
