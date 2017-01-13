package com.example.ogema.connector.util_wlan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dnestle on 24.08.2015.
 */
public class WLANData {
    public WLANData(String ssId) {
        this.ssId = ssId;
    }

    public String ssId;
    public List<HostData> hostData = new ArrayList<>();
}
