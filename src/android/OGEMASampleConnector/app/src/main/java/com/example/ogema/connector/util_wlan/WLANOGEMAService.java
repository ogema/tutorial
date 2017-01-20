package com.example.ogema.connector.util_wlan;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.text.format.Formatter;
import android.util.Log;

import com.example.ogema.connector.util_file.FileUtil;
import com.example.ogema.connector.util_other.AudioHelper;
import com.example.ogema.connector.util_rest.RESTClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WLANOGEMAService extends Service implements WifiScanReceiver.WLANListener {
    public final static String PAR_KEY = "de.iwes.test.key";

    /**
     * Use this static member to configure the service. This is not very nice, but maybe a simple way
     * that works
     */
    private WLANScanConfig wlanScanConfig = null;
    private ResultReceiver resultReceiver = null;
    private String scanState = "no network";
    private String lastFound = "found nothing";
    private String maxConnect = "";
    private String completationStatus = "";

    //public static Context context = null;

    private static final String DEBUG_TAG = "WLANServiceExample";
    //private String writePath = "MobileDeviceCommunication/dataInflow";
    //private String readPath = "MobileDeviceConfig";
    private String writePath = "sampleAndroiddriverConfig/dataInflow";
    private String readPath = "sampleAndroiddriverConfig/mobileDeviceConfig";
    private WifiScanReceiver wifiSR;
    private String latestSSId = "";
    WriteRESTListener latestWriteRESTListener = null;


    private List<HostData> connectionsToHold = new ArrayList<>();
    Timer connectionTimer = null;
    private String serialIdHelper = "";

    public WLANOGEMAService() {
        this.wlanScanConfig = null;
    }
    //public WLANOGEMAService(WLANScanConfig wlanScanConfig) {
    //    this.wlanScanConfig = wlanScanConfig;
    //}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        wifiSR = new WifiScanReceiver( (WifiManager)getSystemService(Context.WIFI_SERVICE), this );
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(wifiSR);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /** we do not really need this here, just for demonstratrion*/
        WLANInput indata = (WLANInput)intent.getParcelableExtra(PAR_KEY);
        resultReceiver = intent.getParcelableExtra("receiver");
        if(indata != null) {
            completationStatus += "1";
            Log.e(DEBUG_TAG, "Received in-directory:" + indata.configFileDirectory);
        } else {
            if(resultReceiver == null) {
                completationStatus += "3";
                indata = new WLANInput();
                indata.configFileDirectory = getFilesDir().getAbsolutePath();
            } else {
                //command to start scan
                completationStatus += "2";
                if (intent.getBooleanExtra("scanNow", false)) {
                    if (latestWriteRESTListener.finished) {
                        latestWriteRESTListener.useKnownHosts = false;
                        latestWriteRESTListener.nscan = 1;
                        latestWriteRESTListener.checkNextConnection();
                        Log.e(DEBUG_TAG, "Started scan manually");
                    }
                }
                Log.e(DEBUG_TAG, "Tried to start scan manually");
                return START_STICKY;
            }
        }

        if(resultReceiver != null) {
            //send all
            if(resultReceiver != null) {
                 Bundle bundle = new Bundle();
                bundle.putString("message", scanState);
                resultReceiver.send(100, bundle);

                bundle = new Bundle();
                bundle.putString("message", lastFound);
                resultReceiver.send(200, bundle);

                bundle = new Bundle();
                bundle.putString("message", maxConnect);
                resultReceiver.send(300, bundle);

                bundle = new Bundle();
                bundle.putString("message", completationStatus);
                resultReceiver.send(400, bundle);

            }

        }

        String json = FileUtil.readPublicFile("wlanConfig.json");
        if(json == null) {
            wlanScanConfig = new WLANScanConfig();
            Log.e(DEBUG_TAG, "writeConfigJSON!");
            FileUtil.writeConfigJSON(wlanScanConfig);
        } else {
            Log.e(DEBUG_TAG, "Read:"+json);
            wlanScanConfig = FileUtil.readConfigJson("wlanConfig.json", WLANScanConfig.class);
            if(wlanScanConfig != null) {
                Log.e(DEBUG_TAG, "bool:"+wlanScanConfig.scanUnknownNetworks);
            } else {
                Log.e(DEBUG_TAG, "WLANOGEMAService.wlanScanConfig null!");
            }
        }

        Log.e(DEBUG_TAG, "Registering receiver");
        registerReceiver(wifiSR, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        return START_STICKY;
    }

    public void networkAvailable(WifiInfo ni) {
        Log.e(DEBUG_TAG, "Network available");
        //if(ni.getType() == ConnectivityManager.TYPE_WIFI){
        //    Log.i(DEBUG_TAG, "WLAN-Num:"+wifiSR.wifis.length+" First item:"+wifiSR.wifis[0]);
        //}
        if(ni == null) {
            //lost connection
            connectionTimer.cancel();
            connectionTimer.purge();
            connectionTimer = null;
            return;
        }

        Log.e(DEBUG_TAG, "Network available:" + ni.getSSID() + " IP:" + ni.getIpAddress());
        if(ni.getIpAddress() == 0) {
            return;
        }

        boolean wlanKnown = false;
        if(wlanScanConfig == null) {
            Log.e(DEBUG_TAG, "Not using wlanScanConfig");
            connectionsToHold.clear();
        } else {
            connectionsToHold = null;
            for(WLANData wd:wlanScanConfig.wlanData) {
                if(wd.ssId.equals(ni.getSSID())) {
                    connectionsToHold = wd.hostData;
                    wlanKnown = true;
                    break;
                }
            }
            if(connectionsToHold == null) {
                WLANData wd = new WLANData(ni.getSSID());
                connectionsToHold = wd.hostData;
                wlanScanConfig.wlanData.add(wd);
            }
        }

        Bundle bundle = new Bundle();
        if(wlanKnown) {
            bundle.putString("message", "Known " + ni.getSSID() + " from:" + Formatter.formatIpAddress(ni.getIpAddress()));
        } else {
            bundle.putString("message", "Scanning " + ni.getSSID() + " from:" + Formatter.formatIpAddress(ni.getIpAddress()));
        }
        scanState = bundle.getString("message");
        if(resultReceiver != null) {
            resultReceiver.send(100, bundle);
        }

        Log.e(DEBUG_TAG, "WLAN known:"+wlanKnown+" use config:"+(wlanScanConfig != null));
        latestSSId = ni.getSSID();
        WriteRESTListener wl = new WriteRESTListener(wlanKnown);
        latestWriteRESTListener = wl;
        wl.ssId = ni.getSSID();
        DeviceUuidFactory duf = new DeviceUuidFactory(this);
        wl.serialId = duf.getDeviceUuid().toString();
        serialIdHelper = wl.serialId;

        //find computer
        int ipadr = ni.getIpAddress();
        wl.bytes = int32toBytes(ipadr);


        try {
            InetAddress address = InetAddress.getByAddress(wl.bytes);
            Log.e("WIFISCAN", "ssID:" + wl.ssId + " serialId:" + wl.serialId + " IP:" + address.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        wl.basicip = ipadr & 0xffffff00;
        wl.checkNextConnection();
    }

    public class WriteRESTListener implements RESTClient.ResultListener {
        public byte[] bytes;
        byte nscan;
        String ssId;
        String serialId;
        int basicip;
        boolean finished = false;

        //only relevant if the network does not need to be scanned, only known hosts to be contacted
        boolean useKnownHosts;

        //Data of current connection
        InetAddress address;
        String value;

        public WriteRESTListener(boolean useKnownHosts) {
            this.useKnownHosts = useKnownHosts;
            if(useKnownHosts) {
                nscan = 0;
            } else {
                nscan = 1;
            }
        }

        //HostData hd;
        @Override
        public void getRESTResult(String getResult, String postResult) {
            Log.e(DEBUG_TAG, "hd:" + address+" result:"+postResult);
            if(postResult != null) {
                Log.e(DEBUG_TAG, "connection to hold:" + address);
                if((connectionsToHold.size() == 0)||useKnownHosts) {
                    connectionTimer = new Timer();
                    connectionTimer.scheduleAtFixedRate(new ConnectTimer(), 20*1000, 20*1000);
//                    connectionTimer.scheduleAtFixedRate(new ConnectTimer(), 10000, 10000);
                }

                HostData hd;
                if(useKnownHosts) {
                    hd = connectionsToHold.get(nscan-1);
                } else {
                    hd = new HostData(address, value, true, readPath + "/" + validJavaOGEMAName(serialId) + "/trackPresence",
                            readPath + "/" + validJavaOGEMAName(serialId) + "/messageToDevice");
                    connectionsToHold.add(hd);
                    FileUtil.writeConfigJSON(wlanScanConfig);
                }
                if(wlanScanConfig != null && wlanScanConfig.listener != null) {
                    wlanScanConfig.listener.foundOGEMA(hd, ssId, !useKnownHosts);
                }
                lastFound = "Found OGEMA on " + address;
                if(resultReceiver != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("message", lastFound);
                    resultReceiver.send(200, bundle);
                }
                finish();
                return; //do not check for further gateways
            }
            checkNextConnection();
        }

        private void finish() {
            finished = true;
            completationStatus += "finished";
            if(resultReceiver != null) {
                Bundle bundle = new Bundle();
                bundle.putString("message", completationStatus);
                resultReceiver.send(400, bundle);
            }
        }
        public void checkNextConnection() {
            if(!latestSSId.equals(ssId)) {
                //we are now connected with another network
                finish();
                return;
            }
            try {
                if(useKnownHosts) {
                    if(nscan >= connectionsToHold.size()) {
                        finish();
                        return;
                    }
                    address = connectionsToHold.get(nscan).address;
                } else {
                    if ((nscan == 0)||(nscan > 255)) {
                        finish();
                        return;
                    }
                    bytes[3] = nscan; //BigInteger.valueOf(scanadr).toByteArray();
                    address = InetAddress.getByAddress(bytes);
                }
                maxConnect = "Max:" + address;
                if(resultReceiver != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("message", maxConnect);
                    resultReceiver.send(300, bundle);
                }

                value = "?mobilid="+serialId+"&ssid="+ssId;
                new RESTClient().setStringValueViaREST(address, writePath, value, WLANOGEMAService.this, this,
                        wlanScanConfig.restUser, wlanScanConfig.restPassword);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            nscan++;
        }
    }


    public static String validJavaOGEMAName(String s) {
        StringBuilder sb = new StringBuilder();
        if(!Character.isJavaIdentifierStart(s.charAt(0))) {
            sb.append("_");
        }
        for (char c : s.toCharArray()) {
            if(!Character.isJavaIdentifierPart(c)) {
                sb.append("_");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static byte[] int32toBytes(int hex) {
        byte[] b = new byte[4];
        b[3] = (byte) ((hex & 0xFF000000) >> 24);
        b[2] = (byte) ((hex & 0x00FF0000) >> 16);
        b[1] = (byte) ((hex & 0x0000FF00) >> 8);
        b[0] = (byte) (hex & 0x000000FF);
        return b;
    }

    boolean connectionTimerActive = false;
    public class ConnectTimer extends TimerTask {
        @Override
        public void run() {
            if(connectionTimerActive) return;
            connectionTimerActive = true;
            for(HostData hd: connectionsToHold) {
                Log.e("WIFISCAN", "Starting Connect Task to:" + hd.address.getHostAddress());
                if ((wlanScanConfig.restUser != null) && (!wlanScanConfig.restUser.equals(""))) {
                    new RESTClient().setStringValueViaREST(hd.address, writePath, hd.value, WLANOGEMAService.this,
                            wlanScanConfig.restUser, wlanScanConfig.restPassword);
                } else {
                    new RESTClient().setStringValueViaREST(hd.address, writePath, hd.value, WLANOGEMAService.this,
                            wlanScanConfig.restUser, wlanScanConfig.restPassword);
                }
                Log.e("WIFISCAN", "First steop of Connect Task to:" + hd.address.getHostAddress());
                //Bundle bundle = new Bundle();
                //bundle.putString("message", "nop");
                //resultReceiver.send(500, bundle);
                if(hd.trackDevice) {
                    Log.e("WIFISCAN", "track device to:" + hd.address.getHostAddress());
                    new RESTClient().getStringValueViaREST(hd.address, hd.readTrackDevicePath, WLANOGEMAService.this,
                            new ReadRESTListener(hd), wlanScanConfig.restUser, wlanScanConfig.restPassword);
                }
            }
            connectionTimerActive = false;
        }
    }

    public class ReadRESTListener implements RESTClient.ResultListener {
        public ReadRESTListener(HostData hd) {
            this.hd = hd;
        }

        HostData hd;
        @Override
        public void getRESTResult(String getResult, String postResult) {
            Log.e("WIFISCAN", "getResult:" + getResult+" post:"+postResult);
            if(getResult != null) {
                String res = getValueFromJsonReply(getResult);
                if((res != null) && res.equals("false")) {
                    hd.trackDevice = false;
                }
                //check for messages
                Log.e("WIFISCAN", "track device to:" + hd.address.getHostAddress());
                new RESTClient().getStringValueViaREST(hd.address, readPath + "/" + validJavaOGEMAName(serialIdHelper) + "/messageToDevice", WLANOGEMAService.this,
                        new ReadRESTMessageListener(hd), wlanScanConfig.restUser, wlanScanConfig.restPassword);
            }
        }
    }

    private boolean activatedRing = false;
    public class ReadRESTMessageListener implements RESTClient.ResultListener {
        public ReadRESTMessageListener(HostData hd) {
            this.hd = hd;
        }

        HostData hd;
        @Override
        public void getRESTResult(String getResult, String postResult) {
            Log.e("WIFISCAN", "RESTMessage: getResult:" + getResult+" post:"+postResult);
            if(getResult != null) {
                String res = getValueFromJsonReply(getResult);
                if((res != null) && res.equals("\"ring\"")) {
                    AudioHelper.setRingtone(true, WLANOGEMAService.this);
                    activatedRing = true;
                } else if(activatedRing) {
                    AudioHelper.setRingtone(false, WLANOGEMAService.this);
                    activatedRing = false;
                }
            }
        }
    }

    public static String getValueFromJsonReply(String json) {
        int i = json.indexOf("\"value\" : ");
        if(i<0) return null;
        int endidx = json.indexOf(",", i+10);
        return json.substring(i+10, endidx);
    }
}
