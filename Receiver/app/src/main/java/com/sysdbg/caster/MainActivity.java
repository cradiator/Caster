package com.sysdbg.caster;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.sysdbg.caster.history.HistoryItem;
import com.sysdbg.caster.router.BroadCastReceiver;
import com.sysdbg.caster.router.CmdReceiver;
import com.sysdbg.caster.router.service.RouterService;
import com.sysdbg.caster.utils.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import io.vov.vitamio.Vitamio;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String INTENT_EXTRA_URL = "URL";

    private PlayerFragment playerFragement;
    private HistroyFragment historyFragment;
    PowerManager.WakeLock wakeLock;

    public static void requestPlay(Context context, String url) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(INTENT_EXTRA_URL, url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(getApplication());

        setContentView(R.layout.activity_main);

        playerFragement = new PlayerFragment();
        historyFragment = new HistroyFragment();
        historyFragment.setCallback(historyFragmentCallback);

        initFragment(getIntent());
        RouterService.requestOnboot(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "OnNewIntent");
        initFragment(intent);
    }

    private void initFragment(Intent intent) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transation = manager.beginTransaction();
        transation.replace(R.id.mainFrame, historyFragment);
        transation.commit();

        if (intent == null) {
            return;
        }

        String url = intent.getStringExtra(INTENT_EXTRA_URL);
        if (StringUtils.isEmpty(url)) {
            return;
        }

        manager.executePendingTransactions();
        play(url, 0);
    }

    private void switchToFragment(Fragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transation = manager.beginTransaction();
        transation.addToBackStack(null);
        transation.replace(R.id.mainFrame, fragment);
        transation.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        acquireWakeLock();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseWakeLock();
    }

    private CmdReceiver.Callback cmdReceiverCallback = new CmdReceiver.Callback() {
        @Override
        public void requestPlay(String url) {
            play(url, 0);
        }
    };

    private HistroyFragment.Callback historyFragmentCallback = new HistroyFragment.Callback() {
        @Override
        public void onPlay(HistoryItem item) {
            if (item.getWebUrl() != null) {
                play(item.getWebUrl().toString(), item.getOffset());
            }
        }
    };

    private void play(String url, long offset) {
        if (playerFragement != null) {
            switchToFragment(playerFragement);
            playerFragement.play(url, offset);
        }
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
