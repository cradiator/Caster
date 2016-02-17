package com.sysdbg.caster.router.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.sysdbg.caster.MainActivity;
import com.sysdbg.caster.router.BroadCastReceiver;
import com.sysdbg.caster.router.CmdReceiver;

import java.io.IOException;

public class RouterService extends Service {
    private static final String TAG = RouterService.class.getSimpleName();
    private static final String CMD = "Cmd";
    private static final int CMD_UNKNOWN = 0;
    private static final int CMD_NETWORK_CHANGE = 1;
    private static final int CMD_START = 2;
    private static final int CMD_STOP = 3;
    private static final int CMD_ON_BOOT = 4;

    private boolean hasNetwork;
    private boolean shouldRunning;
    private boolean isRunning;
    private BroadCastReceiver broadCastReceiver;
    private CmdReceiver cmdReceiver;

    static public void requestNetworkChange(Context context) {
        requestCmd(context, CMD_NETWORK_CHANGE);
    }

    static public void requestStart(Context context) {
        requestCmd(context, CMD_START);
    }

    static public void requestStop(Context context) {
        requestCmd(context, CMD_STOP);
    }

    static public void requestOnboot(Context context) {
        requestCmd(context, CMD_ON_BOOT);
    }

    static private void requestCmd(Context context, int cmd) {
        Intent intent = new Intent(context, RouterService.class);
        intent.putExtra(CMD, cmd);
        context.startService(intent);
    }

    public RouterService() {
        hasNetwork = false;
        isRunning = false;
        shouldRunning = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadCastReceiver = new BroadCastReceiver();
        cmdReceiver = new CmdReceiver(this);
        cmdReceiver.setCallback(cmdReceiverCallback);

        onNetworkStatusChange();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        stop();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            ensureServerStatus();
            return START_STICKY;
        }

        int cmd = intent.getIntExtra(CMD, CMD_UNKNOWN);

        if (cmd == CMD_NETWORK_CHANGE) {
            onNetworkStatusChange();
        }
        else if (cmd == CMD_START) {
            start();
        }
        else if (cmd == CMD_STOP) {
            stop();
        }
        else {
            ensureServerStatus();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RouterServiceBinder();
    }

    synchronized void start() {
        shouldRunning = true;
        ensureServerStatus();
    }

    public void stop() {
        shouldRunning = false;
        ensureServerStatus();
    }

    public class RouterServiceBinder extends Binder {
        public RouterService getRouterSercie() {
            return RouterService.this;
        }
    }

    private void onNetworkStatusChange() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo == null) {
            hasNetwork = false;
        }
        else {
            int networkType = activeNetInfo.getType();
            if (networkType == ConnectivityManager.TYPE_WIFI ||
                    networkType == ConnectivityManager.TYPE_ETHERNET) {
                hasNetwork = true;
            }
            else {
                hasNetwork = false;
            }
        }

        ensureServerStatus();
    }

    private void ensureServerStatus() {
        if (!hasNetwork) {
            if (isRunning) {
                Log.i(TAG, "Stop service because of network lost");
                cmdReceiver.stop();
                broadCastReceiver.stop();
            }

            isRunning = false;
            return;
        }

        if (shouldRunning && !isRunning) {
            Log.i(TAG, "start service");
            try {
                cmdReceiver.start();
            } catch (IOException e) {
                Log.e(TAG, "start cmd receiver fail", e);
            }
            broadCastReceiver.start();
            isRunning = true;
            return;
        }

        if (!shouldRunning && isRunning) {
            Log.i(TAG, "stop service");
            cmdReceiver.stop();
            broadCastReceiver.stop();
            isRunning = false;
        }
    }

    private CmdReceiver.Callback cmdReceiverCallback = new CmdReceiver.Callback() {
        @Override
        public void requestPlay(String url) {
            MainActivity.requestPlay(RouterService.this, url);
        }
    };
}
