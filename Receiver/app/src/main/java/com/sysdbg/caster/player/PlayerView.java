package com.sysdbg.caster.player;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.sysdbg.caster.resolver.MediaInfo;
import com.sysdbg.caster.utils.StringUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by crady on 2/7/2016.
 */
public class PlayerView extends VideoView {
    private static final String TAG = PlayerView.class.getSimpleName();
    private static final String M3U_CACHE_DIR = "M3U-Cache";

    private boolean firstPlay = true;

    public PlayerView(Context context) {
        super(context);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void play(MediaInfo mediaInfo) {
        play(mediaInfo, 0);
    }

    public void play(MediaInfo mediaInfo, int offset) {
        play(mediaInfo, offset, MediaInfo.MAX_VIDEO_RESOLUTION);
    }

    public void play(MediaInfo mediaInfo, long offset, String definition) {
        if (mediaInfo == null) {
            Log.e(TAG, "play with null MediaInfo");
            return;
        }

        // get media data with definition
        MediaInfo.MediaData mediaData = mediaInfo.getData(definition);
        if (mediaData == null) {
            Log.e(TAG, "play with empty MediaData");
            return;
        }

        // get media path
        String path = null;
        if (mediaData.getDataLocations().size() == 1) {
            path = mediaData.getDataLocations().get(0).toString();
        }
        else if (mediaData.getM3uLocation() != null){
            path = mediaData.getM3uLocation().toString();
        }
        else {
            String prefix = null;
            if (mediaInfo.getWebPageUrl() != null) {
                prefix = mediaInfo.getWebPageUrl().getHost();
            }

            path = generateLocalM4u(mediaData, prefix);
        }

        // start play
        setVideoPath(path);
        if (offset > 0) {
            seekTo(offset);
        }
        start();
    }

    private String generateLocalM4u(MediaInfo.MediaData mediaData, String prefix) {
        File cacheDir = getContext().getCacheDir();
        File m3uDir = new File(cacheDir, M3U_CACHE_DIR);
        m3uDir.mkdir();

        if (StringUtils.isEmpty(prefix)) {
            prefix = "";
        }

        File m3uFile = new File(m3uDir, prefix + "-" + UUID.randomUUID().toString());
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(m3uFile));
            mediaData.generateM3uWithLocations(os);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Can create m3u file", e);
            return null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }

        return m3uFile.toURI().toString();
    }
}
