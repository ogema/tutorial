package com.example.ogema.connector.util_wlan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by dnestle on 15.06.2015.
 */
public class WifiScanReceiver extends BroadcastReceiver {
    public String wifis[];
    public WifiManager wifi;
    protected WLANListener listener;

    public WifiScanReceiver(WifiManager wman, WLANListener listener) {
        Log.e("WifiScanR", "construct, startScan");
        wifi= wman;
        wifi.startScan();
        this.listener = listener;
    }

    public void onReceive(Context c, Intent intent) {

        //ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.e("WifiScanR", "onReceive");
        WifiInfo ni = wifi.getConnectionInfo();
        listener.networkAvailable(ni);

        List<ScanResult> wifiScanList = wifi.getScanResults();
        wifis = new String[wifiScanList.size()];

        for(int i = 0; i < wifiScanList.size(); i++){
            wifis[i] = ((wifiScanList.get(i)).toString());
        }
    }

    public interface WLANListener {
        public void networkAvailable(WifiInfo ni);
    }

}
