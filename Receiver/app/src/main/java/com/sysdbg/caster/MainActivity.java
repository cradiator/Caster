package com.sysdbg.caster;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sysdbg.caster.router.BroadCastReceiver;
import com.sysdbg.caster.router.CmdReceiver;

import java.io.IOException;

public class MainActivity extends Activity
        implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
    private static final String TAG = "Caster.MainActivity";
    private static final int REQUEST_MP4_ID = 10000;
    private static final int MOVIE_SIZE_CHANGE = 10001;
    private static final int MOVIE_ERROR = 10002;
    private static final int MOVIE_START = 10003;

    private PlayerView mPlayerView;
    private TextView mMessageTextView;
    private FrameLayout mMainLayout;

    private CmdReceiver mCmdReceiver;
    private BroadCastReceiver mBroadCastReceiver;
    private Handler mCmdHandler;

    private PowerManager.WakeLock mWakeLock;

    int mMovieWidth;
    int mMovieHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainLayout = (FrameLayout)findViewById(R.id.mainLayout);
        mPlayerView = (PlayerView)findViewById(R.id.playerView);
        mMessageTextView = (TextView)findViewById(R.id.messageTextView);

        showMessageView();
        mMessageTextView.setText("Cast Receiver");
        mPlayerView.setOnErrorListener(this);
        mPlayerView.setOnPreparedListener(this);

        mCmdHandler = new CmdHandler();
        mCmdReceiver = new CmdReceiver(this);
        mBroadCastReceiver = new BroadCastReceiver();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPlayerView.setSystemUiVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mPlayerView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mPlayerView.restoreState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            mCmdReceiver.start();
            mBroadCastReceiver.start();
        } catch (IOException e) {
            Log.e(TAG, "start CmdReceiver failed", e);
        }

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "CasterWakeLock");
        mWakeLock.acquire();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide navigation bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mCmdReceiver != null) {
            mCmdReceiver.stop();
        }

        if (mBroadCastReceiver != null) {
            mBroadCastReceiver.stop();
        }

        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    public void requestPlayMp4(String url) {
        Message message = Message.obtain();
        message.what = REQUEST_MP4_ID;
        message.obj = url;

        mCmdHandler.sendMessage(message);
    }

    private void onPlayMap4(String url) {
        showMessageView();
        // showMovieView();
        mMessageTextView.setText("loading " + url);
        mPlayerView.play(url);
    }

    public void requestMovieSizeChange(int width, int height) {
        int[] size = new int[2];
        size[0] = width;
        size[1] = height;

        Message message = Message.obtain();
        message.what = MOVIE_SIZE_CHANGE;
        message.obj = size;

        mCmdHandler.sendMessage(message);
    }

    private void onMovieSizeChange(int width, int height) {
        mMovieWidth = width;
        mMovieHeight = height;
        layoutPlayerSurfaceView();
    }

    public void requestMovieStart() {
        Message message = Message.obtain();
        message.what = MOVIE_START;

        mCmdHandler.sendMessage(message);
    }

    private void onMovieStart() {
        showMovieView();
    }

    private void layoutPlayerSurfaceView() {
        double windowWidth = (double)mMainLayout.getWidth();
        double windowHeight = (double)mMainLayout.getHeight();

        double movieRatio = (double)mMovieWidth / (double)mMovieHeight;
        double realWidth;
        double realHeight;
        if (movieRatio * windowHeight <= windowWidth) {
            realWidth = movieRatio * windowHeight;
            realHeight = windowHeight;
        }
        else {
            realWidth = windowWidth;
            realHeight = windowWidth / movieRatio;
        }

        double left = (windowWidth - realWidth) / 2;
        double top = (windowHeight - realHeight) / 2;
        mPlayerView.setLeft((int)left);
        mPlayerView.setTop((int)top);
        mPlayerView.setRight((int) (left + realWidth));
        mPlayerView.setBottom((int) (top + realHeight));
    }

    public void requestPlayError(String msg) {
        Message message = Message.obtain();
        message.what = MOVIE_ERROR;
        message.obj = msg;

        mCmdHandler.sendMessage(message);
    }

    private void onMovieError(String msg) {
        showMessageView();
        mMessageTextView.setText(msg);
    }

    private void showMovieView() {
        mPlayerView.bringToFront();
        mMessageTextView.setVisibility(View.INVISIBLE);
    }

    private void showMessageView() {
        mMessageTextView.bringToFront();
        mMessageTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        requestPlayError(String.format("media player error %d", what));
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        showMovieView();
    }

    private class CmdHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == REQUEST_MP4_ID) {
                onPlayMap4((String)msg.obj);
            }
            else if (what == MOVIE_SIZE_CHANGE) {
                int[] size = (int[])msg.obj;
                onMovieSizeChange(size[0], size[1]);
            }
            else if (what == MOVIE_ERROR) {
                onMovieError((String)msg.obj);
            }
            else if (what == MOVIE_START) {
                onMovieStart();
            }
        }
    }
}
