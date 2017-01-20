package com.example.ogema.connector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ogema.connector.util_file.FileUtil;
import com.example.ogema.connector.util_other.AudioHelper;
import com.example.ogema.connector.util_wlan.WLANInput;
import com.example.ogema.connector.util_wlan.WLANOGEMAService;
import com.example.ogema.connector.util_wlan.WLANScanConfig;
import com.example.ogema.connector.util_wlan.WifiScanReceiver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

public class ServletWLANActivity extends Activity { //implements WifiScanReceiver.WLANListener { // implements NetworkStateReceiver.NetworkStateReceiverListener {
    private static final String DEBUG_TAG = "WALNServletExample";
    private EditText restUser;
    private EditText restPassword;
    private TextView textView;

    //private NetworkStateReceiver networkStateReceiver;
    private WifiScanReceiver wifiSR;
    private List<String> connectionsToHold = new ArrayList<>();
    Timer connectionTimer = null;
    private WLANScanConfig wlanScanConfigForRestData;

    String listenStatus = "Service off";
    String scanState = "no network";
    String lastFound = "found nothing";
    String maxConnect = "";
    String completationStatus = "";
    String lastHelloSent = "";
    public void setText(String text) {
        textView.setText(listenStatus+" "+scanState+" "+lastFound+" "+maxConnect+completationStatus+" "+lastHelloSent+text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servlet_wlan);
        //urlText = (EditText) findViewById(R.id.edit_urlServlet);
        restUser = (EditText) findViewById(R.id.edit_restUser);
        restPassword = (EditText) findViewById(R.id.edit_restPw);
        textView = (TextView) findViewById(R.id.text2_urlServlet);

        //reading access data from file
        String json = FileUtil.readPublicFile("wlanConfig.json");
        if(json == null) {
            Log.e(DEBUG_TAG, "Resetting rest access data");
            restUser.setText("rest");
            restPassword.setText("");
        } else {
            Log.e(DEBUG_TAG, "Read:"+json);
            //Gson gson = new Gson();
            //BufferedReader out = null;
            //out = FileUtil.getBufferedReader("wlanConfig.json");
            //wlanScanConfigForRestData = gson.fromJson(out, WLANScanConfig.class);
            wlanScanConfigForRestData = FileUtil.readConfigJson("wlanConfig.json", WLANScanConfig.class);
            //if (out != null) {
            // try {
            //        out.close();
            //    } catch (IOException e) {}
            //}
            if(wlanScanConfigForRestData != null) {
                Log.e(DEBUG_TAG, "rest:"+wlanScanConfigForRestData.restUser+" / "+wlanScanConfigForRestData.restPassword);
                restUser.setText(wlanScanConfigForRestData.restUser);
                restPassword.setText(wlanScanConfigForRestData.restPassword);
            } else {
                Log.e(DEBUG_TAG, "ServletWLANActivity:wlanScanConfigForRestData null!");
            }
        }

        Log.e(DEBUG_TAG, "starting Service!");

        class MyResultReceiver extends ResultReceiver
        {
            public MyResultReceiver(Handler handler) {
                super(handler);
            }

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {

                if (resultCode == 100) {
                    scanState = resultData.getString("message");
                    completationStatus = "";
                    setText("");
                } else if (resultCode == 200) {
                    lastFound = resultData.getString("message");
                   setText("");
                } else if (resultCode == 400) {
                    completationStatus = " "+resultData.getString("message");
                    setText("");
                } else if (resultCode == 500) {
                    long yourmilliseconds = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                    Date resultdate = new Date(yourmilliseconds);
                    lastHelloSent = sdf.format(resultdate);
                    setText("");
                } else {
                    maxConnect = resultData.getString("message");
                    setText("");
                }
            }
        }
        MyResultReceiver resultReceiver = new MyResultReceiver(null);

        WLANInput wlanin = new WLANInput();
        wlanin.configFileDirectory = getFilesDir().getAbsolutePath();
        Intent mIntent = new Intent(this, WLANOGEMAService.class);
        Bundle mBundle = new Bundle();
        mBundle.putParcelable(WLANOGEMAService.PAR_KEY, wlanin);
        mIntent.putExtras(mBundle);
        mIntent.putExtra("receiver", resultReceiver);
        startService(mIntent);
        listenStatus = "Service on";
        setText("");
    }

    protected void onPause() {
        //unregisterReceiver(wifiSR);
        super.onPause();
    }

    protected void onResume() {
        //registerReceiver(wifiSR, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_servlet_wlan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void myClickHandlerServlet(View view) {
        stopService(new Intent(this, WLANOGEMAService.class));
        listenStatus = "Service off";
        maxConnect = "";
        completationStatus = "";
        setText("");
        finish();
        return;
    }
    public void myClickHandlerScan(View view) {
        //set values
        wlanScanConfigForRestData.restUser = restUser.getText().toString();
        wlanScanConfigForRestData.restPassword = restPassword.getText().toString();
        FileUtil.writeConfigJSON(wlanScanConfigForRestData);

        completationStatus = "";
        setText("");
        Intent i = new Intent(this, WLANOGEMAService.class);
        i.putExtra("scanNow", true);
        startService(i);
        return;
    }
    public void myClickHandlerTestInternalFunction(View view) {
        AudioHelper.toggleRingTone(this);
    }
}