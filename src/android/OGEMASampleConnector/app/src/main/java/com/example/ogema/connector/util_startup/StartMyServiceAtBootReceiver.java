package com.example.ogema.connector.util_startup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.ogema.connector.util_wlan.WLANOGEMAService;

/**
 * Created by dnestle on 02.01.2016.
 */
public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, WLANOGEMAService.class);
            context.startService(serviceIntent);
        }
    }
}
