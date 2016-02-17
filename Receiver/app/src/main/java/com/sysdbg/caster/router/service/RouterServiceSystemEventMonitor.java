package com.sysdbg.caster.router.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class RouterServiceSystemEventMonitor extends BroadcastReceiver {
    private static final String TAG = RouterServiceSystemEventMonitor.class.getSimpleName();

    private static Set<String> wifiChangeEvent;
    private static Set<String> bootonEvent;

    static {
        wifiChangeEvent = new HashSet<>();
        wifiChangeEvent.add("android.net.conn.CONNECTIVITY_CHANGE");
        wifiChangeEvent.add("android.net.wifi.WIFI_STATE_CHANGED");
        wifiChangeEvent.add("android.net.wifi.STATE_CHANGE");

        bootonEvent = new HashSet<>();
        bootonEvent.add("android.intent.action.BOOT_COMPLETED");
        bootonEvent.add("android.intent.action.QUICKBOOT_POWERON");
    }

    public RouterServiceSystemEventMonitor() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, intent.getAction());

        if (wifiChangeEvent.contains(intent.getAction())) {
            onNetworkChange(context, intent);
        }
        else if (bootonEvent.contains(intent.getAction())) {
            onBooton(context, intent);
        }
    }

    private void onNetworkChange(Context context, Intent intent) {
        RouterService.requestNetworkChange(context);
    }

    private void onBooton(Context context, Intent intent) {
        RouterService.requestOnboot(context);
    }
}
