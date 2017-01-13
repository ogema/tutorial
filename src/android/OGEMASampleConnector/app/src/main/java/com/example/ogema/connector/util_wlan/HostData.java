package com.example.ogema.connector.util_wlan;

import java.net.InetAddress;

/**
 * Created by dnestle on 24.08.2015.
 */
public class HostData {
    InetAddress address;
    String value;
    String readTrackDevicePath;
    String readMessagePath;
    boolean trackDevice;

    public HostData(InetAddress address, String value, boolean trackDevice, String readTrackDevicePath, String readMessagePath) {
        this.address = address;
        this.value = value;
        this.trackDevice = trackDevice;
        this.readTrackDevicePath = readTrackDevicePath;
        this.readMessagePath = readMessagePath;
    }
}
