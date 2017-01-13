package com.example.ogema.connector.util_wlan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dnestle on 24.08.2015.
 */
public class WLANScanConfig {
    public boolean scanUnknownNetworks = true;
    public List<WLANData> wlanData = new ArrayList<>();
    public String restUser;
    public String restPassword;

    public interface OGEMAConnectionListener {
        void foundOGEMA(HostData hostData, String ssId, boolean newConnection);
    }
    OGEMAConnectionListener listener = null;
}
