package com.example.ogema.connector.util_rest;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

/**
 * Helper for connecting to an OGEMA system via the OGEMA REST interface
 */
public class RESTClient {
    public interface ResultListener {
        void getRESTResult(String getResult, String postResult);
    }
    ResultListener listener = null;
    String getResult = null;

    public void setResultListener(ResultListener listener) {
        this.listener = listener;
    }

    private static final String DEBUG_TAG = "RESTClient";

    /**Write value via the OGEMA REST interface
     *
     * @param address IP or domain to write to
     * @param resPath resource location to write to with delimiter '/'
     * @param value value to write
     * @param act
     * @param restUser OGEMA REST user to use
     * @param restPw password for OGEMA REST user
     */
    public void setStringValueViaREST(InetAddress address, String resPath, String value, ContextWrapper act,
                                      String restUser, String restPw) {
        setStringValueViaREST("https://"+address.getHostAddress()+":8443/rest/resources/"+resPath+"?user="+restUser+"&pw="+restPw, value, act);
    }

    /**Write value via the OGEMA REST interface
     *
     * @param address IP or domain to write to
     * @param resPath resource location to write to with delimiter '/'
     * @param value value to write
     * @param act
     * @param listener if not null the listener will be called when the response from the OGEMA system is
     *                 available
     * @param restUser OGEMA REST user to use
     * @param restPw password for OGEMA REST user
     */
    public void setStringValueViaREST(InetAddress address, String resPath, String value, ContextWrapper act, ResultListener listener,
                                      String restUser, String restPw) {
        setResultListener(listener);
        setStringValueViaREST(address, resPath, value, act, restUser, restPw);
    }

    /**Read data from an OGEMA system via REST
     *
     * @param address IP or domain to read from
     * @param resPath resource location to read from with delimiter '/'
     * @param act
     * @param listener listener will be called when the result is available
     * @param restUser OGEMA REST user to use
     * @param restPw password for OGEMA REST user
     */
    public void getStringValueViaREST(InetAddress address, String resPath, ContextWrapper act, ResultListener listener,
                                      String restUser, String restPw) {
        getStringValueViaREST("https://" + address.getHostAddress() + ":8443/rest/resources/" + resPath+"?user="+restUser+"&pw="+restPw, act, listener);
    }


    private void getStringValueViaREST(String url, ContextWrapper act, ResultListener listener) {
        this.listener = listener;
        ConnectivityManager connMgr = (ConnectivityManager)
                act.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(url, null);
        } else {
            listener.getRESTResult(null, null);
        }
    }

    private void setStringValueViaREST(String url, String value, ContextWrapper act) {
        ConnectivityManager connMgr = (ConnectivityManager)
                act.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(url, value);
        } else {
            if(listener != null) {
                listener.getRESTResult(null, null);
            }
        }

    }
    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        String url = null;
        String value;
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                url = urls[0];
                value = urls[1];
                Log.e("DEBUG_TAG", "Connecting to:" + url);
                return downloadUrl(urls[0]);
            } catch (Exception e) {
                return "!Unable to retrieve web page. URL may be invalid.\n"+e.toString();
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                Log.e(DEBUG_TAG, "Download-Result:"+result);
                if(result.startsWith("!")) {
                    if(listener != null) {
                        listener.getRESTResult(getResult, null);
                    }
                    return;
                }
                result = result.substring(0, result.indexOf("}")+1);
                Log.e(DEBUG_TAG, "From "+url+" JSON-In-Message:" + result);
                getResult = result;
                if(value == null) {
                    listener.getRESTResult(getResult, null);
                    return;
                }
                JSONObject jObject = new JSONObject(result);
                String currentValue = jObject.getString("value");
                jObject.put("value", value);
                String jsonString = jObject.toString(2);
                jsonString = jsonString.replaceAll("\\\\/", "/");
                Log.e(DEBUG_TAG, "From "+url+" JSON-Out-PUT:" + jsonString);
                new UploadWebpageTask().execute(url, jsonString);

            } catch(JSONException e) {
                Log.e(DEBUG_TAG, Log.getStackTraceString(e));
            }
        }
    }

    private class UploadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                Log.e(DEBUG_TAG, "JSON-Out-Message:" + urls[1]);
                uploadUrl(urls[0], urls[1]);
                return urls[0]+":"+urls[1];
            } catch (Exception e) {
                Log.e(DEBUG_TAG, Log.getStackTraceString(e));
                return "Unable to retrieve web page for upload. URL may be invalid.\n"+e.toString();
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.e(DEBUG_TAG, "Upload result:"+result);
            if(listener != null) {
                listener.getRESTResult(getResult, result);
            }
        }
    }

    private void uploadUrl(String myurl, String data) throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException, CertificateException {
        OutputStream theControl = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            //From http://stackoverflow.com/questions/2642777/trusting-all-certificates-using-httpclient-over-https/6378872#6378872
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            //sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            conn.setSSLSocketFactory(sf);

            //From http://stackoverflow.com/questions/1828775/how-to-handle-invalid-ssl-certificates-with-apache-httpclient
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
            SSLContext.setDefault(ctx);

            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });


            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(4000 /* milliseconds */);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            theControl = conn.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(theControl));
            out.write(data);
            out.close();

            // Starts the query
            Log.e(DEBUG_TAG, "Connecting to:");
            Log.e(DEBUG_TAG, "Connecting to:"+conn.getURL());
            Log.e(DEBUG_TAG, "Connecting to:"+conn.getURL() +" reqMethod:"+conn.getRequestMethod()+" enc:" + conn.getContentEncoding());
            conn.connect();

            int response = conn.getResponseCode();
            Log.e(DEBUG_TAG, "The response is: " + response+ "full:"+conn.getResponseMessage());
            //is = conn.getInputStream();

            // Convert the InputStream into a string
            //String contentAsString = readIt(is, len);
            //return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (theControl != null) {
                theControl.close();
            }
        }
    }


    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    private String downloadUrl(String myurl) throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException, CertificateException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            //From http://stackoverflow.com/questions/2642777/trusting-all-certificates-using-httpclient-over-https/6378872#6378872
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            //sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            conn.setSSLSocketFactory(sf);

            //From http://stackoverflow.com/questions/1828775/how-to-handle-invalid-ssl-certificates-with-apache-httpclient
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
            SSLContext.setDefault(ctx);

            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });


            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(4000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The GET response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    private String readIt(InputStream stream, int len) throws IOException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
}
