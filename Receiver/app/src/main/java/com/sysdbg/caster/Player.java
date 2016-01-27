package com.sysdbg.caster;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by crady on 1/19/2016.
 */
public class Player implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnErrorListener {
    private MediaPlayer mMediaPlayer;
    private MainActivity mMainActivity;
    private SurfaceHolder mSurfaceHolder;

    public Player(MainActivity mainActivity, SurfaceHolder holder) {
        mMainActivity = mainActivity;
        mSurfaceHolder = holder;
    }

    public void play(String url) {
        release();

        MediaPlayer player = new MediaPlayer();
        player.setDisplay(mSurfaceHolder);

        try {
            player.setDataSource(url);
        }
        catch (IOException e) {
            mMainActivity.requestPlayError(e.getMessage());
            return;
        }

        mMediaPlayer = player;
        player.setOnPreparedListener(this);
        player.setOnVideoSizeChangedListener(this);

        player.prepareAsync();
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    public void resume() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mp != mMediaPlayer)
            return;

        mMediaPlayer.start();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (mp != mMediaPlayer)
            return;

        mMainActivity.requestMovieSizeChange(width, height);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mMainActivity.requestPlayError(String.format("media player error %d", what));
        return false;
    }
}
