package com.sysdbg.caster;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.FrameLayout;

import com.sysdbg.caster.player.PlayerView;
import com.sysdbg.caster.router.BroadCastReceiver;
import com.sysdbg.caster.router.CmdReceiver;

import java.io.IOException;

public class MainActivity extends Activity {
    private PlayerFragment playerFragement;
    PowerManager.WakeLock wakeLock = null;
    private Handler handler = new Handler();
    private BroadCastReceiver broadCastReceiver = new BroadCastReceiver();
    private CmdReceiver cmdReceiver = new CmdReceiver(handler);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerFragement = new PlayerFragment();
        cmdReceiver.setCallback(cmdReceiverCallback);

        switchToFragment(playerFragement);
    }

    private void switchToFragment(Fragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transation = manager.beginTransaction();
        transation.add(R.id.mainFrame, fragment);
        transation.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startServer();
        acquireWakeLock();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopServer();
        releaseWakeLock();
    }

    private CmdReceiver.Callback cmdReceiverCallback = new CmdReceiver.Callback() {

        @Override
        public void requestPlay(String url) {
        play(url, 0,0 );
        }
    };

    private void play(String url, int sectionNumber, int offset) {
        if (playerFragement != null) {
            playerFragement.play(url, sectionNumber, offset);
        }
    }

    private void startServer() {
        broadCastReceiver.start();
        try {
            cmdReceiver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopServer() {
        broadCastReceiver.stop();
        cmdReceiver.stop();
    }

    private void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "Caster");
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock()
    {
        if (null != wakeLock)
        {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
