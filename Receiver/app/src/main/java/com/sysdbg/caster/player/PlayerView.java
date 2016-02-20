package com.sysdbg.caster.player;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by crady on 2/7/2016.
 */
public class PlayerView extends VideoView {
    private static final String TAG = PlayerView.class.getSimpleName();

    private String[] mediaUrls;
    private int currentMediaIndex;

    private MediaPlayer.OnCompletionListener onCompletionListener;
    private MediaPlayer.OnPreparedListener onPreparedListener;
    private boolean firstPlay = true;

    public PlayerView(Context context) {
        super(context);
        init();
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public int getCurrentMediaIndex() {
        return currentMediaIndex;
    }

    public void play(int currentMediaIndex, int offset, String... urls) {
        mediaUrls = urls;
        play(currentMediaIndex, offset);
    }

    public void play(int currentMediaIndex, int offset) {
        if (mediaUrls == null) {
            Log.e(TAG, "play with empty urls");
            return;
        }

        this.currentMediaIndex = currentMediaIndex;
        firstPlay = true;
        playSection(currentMediaIndex, offset);
    }

    @Override
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        onCompletionListener = l;
    }

    @Override
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        onPreparedListener = l;
    }

    private void init() {
        super.setOnCompletionListener(sectionCompletionListener);
        super.setOnPreparedListener(sectionOnPreparedListener);
    }

    private MediaPlayer.OnCompletionListener sectionCompletionListener
            = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (currentMediaIndex >= mediaUrls.length - 1) {
                currentMediaIndex = 0;
                seekTo(0);

                fireOnCompletion(mp);
            }
            else {
                currentMediaIndex++;
                playSection(currentMediaIndex, 0);
            }
        }
    };

    private MediaPlayer.OnPreparedListener sectionOnPreparedListener
            = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (firstPlay) {
                firstPlay = false;
                fireOnPrepared(mp);
            }
        }
    };

    private void playSection(int sectionNumber, int offset) {
        stopPlayback();

        setVideoPath(mediaUrls[sectionNumber]);
        if (offset > 0) {
            seekTo(offset);
        }
        start();
    }

    private void fireOnCompletion(MediaPlayer mp) {
        if (onCompletionListener != null) {
            onCompletionListener.onCompletion(mp);
        }
    }

    private void fireOnPrepared(MediaPlayer mp) {
        if (onPreparedListener != null) {
            onPreparedListener.onPrepared(mp);
        }
    }
}
