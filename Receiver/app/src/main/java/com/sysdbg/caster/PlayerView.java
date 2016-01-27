package com.sysdbg.caster;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by crady on 1/20/2016.
 */
public class PlayerView extends VideoView {
    private static final String IS_PLAYING = "PlayerView.IsPlaying";
    private static final String URL = "PlayerView.Url";
    private static final String CURRENT_POS = "PlayerView.CurrentPos";

    private MediaController mMediaController;
    private String mUrl;

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mMediaController = new MediaController(context);
        setMediaController(mMediaController);
    }

    public void play(String url) {
        if (isPlaying()) {
            stopPlayback();
        }

        mUrl = url;
        setVideoPath(url);
        start();
    }

    public void stop() {
        stopPlayback();
    }

    public void saveState(Bundle bundle) {
        if (bundle == null)
            return;

        bundle.putBoolean(IS_PLAYING, isPlaying());
        bundle.putString(URL, mUrl);
        bundle.putInt(CURRENT_POS, getCurrentPosition());
    }

    public void restoreState(Bundle bundle) {
        if (bundle == null)
            return;

        boolean isPlaying = bundle.getBoolean(IS_PLAYING);
        String url = bundle.getString(URL);
        int pos = bundle.getInt(CURRENT_POS);

        if (url == null) {
            return;
        }

        setVideoPath(url);
        if (pos <= 0) {
            return;
        }

        if (isPlaying) {
            start();
        }
    }
}
