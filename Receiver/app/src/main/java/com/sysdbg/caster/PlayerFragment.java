package com.sysdbg.caster;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sysdbg.caster.history.HistoryItem;
import com.sysdbg.caster.history.HistoryManager;
import com.sysdbg.caster.player.PlayerView;
import com.sysdbg.caster.resolver.MediaInfo;
import com.sysdbg.caster.resolver.Resolver;
import com.sysdbg.caster.resolver.simplehttp.SimpleHttpResolver;

import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;

public class PlayerFragment extends Fragment {
    private static final String TAG = PlayerFragment.class.getSimpleName();

    private Handler handler;
    private PlayerView playerView;
    private TextView playerInformationTextView;
    private MediaController playerController;
    private MediaInfo mediaInfo;
    private int currentResolution;

    private List<String> textInfos;

    private String pendingUrl;
    private int pendingSectionNumber;
    private int pendingOffset;
    private int pendingResolution;

    public PlayerFragment() {
        textInfos = new ArrayList<>();
        handler = new Handler();
    }

    public void play(String url) {
        play(url, 0, 0);
    }

    public void play(String url, int sectionNumber, int offset) {
        play(url, sectionNumber, offset, MediaInfo.MAX_VIDEO_RESOLUTION);
    }

    public void play(final String url, final int sectionNumber, final int offset, final int resolution) {
        stop();

        if (playerView == null) {
            pendingUrl = url;
            pendingSectionNumber = sectionNumber;
            pendingOffset = offset;
            pendingResolution = resolution;
            return;
        }

        pendingUrl = null;
        pendingSectionNumber = pendingOffset = pendingResolution = 0;

        clearText();
        addText("Resolving " + url);
        Resolver.parse(url, new SimpleHttpResolver.Callback() {
                    @Override
                    public void onResult(MediaInfo info) {
                        if (info == null) {
                            Log.e(TAG, "Resolve url " + url + " failed.");
                            addText("Resolving failed");
                            return;
                        }

                        addText("Loading...");
                        mediaInfo = info;

                        int sectionCount = info.getMediaSectionCount();
                        String urls[] = new String[sectionCount];
                        for (int i = 0; i < urls.length; i++) {
                            MediaInfo.MediaSection section = info.getMediaPart(i, resolution);
                            if (section == null) {
                                continue;
                            }
                            urls[i] = section.getMediaUrl();
                        }

                        currentResolution = info.getMediaPart(sectionNumber, resolution).getVideoResolution();

                        updateHistory(info, sectionNumber, offset);
                        playerView.play(sectionNumber, offset, urls);
                    }
                },
                handler);
    }

    public void stop() {
        if (playerView == null) {
            return;
        }

        playerView.stopPlayback();
        mediaInfo = null;
    }

    public int getCurrentResolution() {
        return currentResolution;
    }

    public int getCurrentSection() {
        return playerView.getCurrentMediaIndex();
    }

    public int getSectionCount() {
        if (mediaInfo == null) {
            return 0;
        }

        return mediaInfo.getMediaSectionCount();
    }

    public String getUrl() {
        if (mediaInfo == null) {
            return null;
        }

        return mediaInfo.getWebPageUrl();
    }

    public int getCurrentSectionOffset() {
        return (int)playerView.getCurrentPosition();
    }

    public int[] getResolutions(int sectionNumber) {
        if (mediaInfo == null) {
            return new int[0];
        }

        return mediaInfo.getResolutions(getCurrentSection());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        playerView = (PlayerView)view.findViewById(R.id.playerView);
        playerInformationTextView = (TextView)view.findViewById(R.id.playerInformationTextView);

        playerController = new MediaController(getActivity());
        // playerController.setCallback(playerContrallerCallback);
        playerView.setMediaController(playerController);
        playerView.setOnPreparedListener(onMediaPlayerPrepared);
        playerView.setOnCompletionListener(onMediaPlayerComplete);

        addText("Caster");

        if (pendingUrl != null) {
            play(pendingUrl, pendingSectionNumber, pendingOffset, pendingResolution);
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        updateHistory(mediaInfo, getCurrentSection(), getCurrentSectionOffset());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        playerView = null;
        playerController = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void updateHistory(MediaInfo medioInfo, int currentSection, int currentOffset) {
        HistoryItem historyItem = new HistoryItem();
        historyItem.setWebUrl(medioInfo.getWebPageUrl());
        historyItem.setImgUrl(medioInfo.getImageUrl());
        historyItem.setTitle(medioInfo.getTitle());
        historyItem.setDescription(medioInfo.getDescription());
        historyItem.setTotalSection(medioInfo.getMediaSectionCount());
        historyItem.setCurrentSection(currentSection);
        historyItem.setCurrentOffset(currentOffset);

        HistoryManager.getInstance(getActivity()).saveItem(historyItem);
    }

    private void clearText() {
        textInfos.clear();
        refrashTextView();
    }

    private void addText(String text) {
        textInfos.add(text);
        refrashTextView();
    }

    private void refrashTextView() {
        if (playerInformationTextView == null) {
            return;
        }

        StringBuffer sb = new StringBuffer();
        for(String s : textInfos) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(s);
        }

        playerInformationTextView.setText(sb.toString());
    }

    private MediaPlayer.OnPreparedListener onMediaPlayerPrepared = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            clearText();
        }
    };

    private MediaPlayer.OnCompletionListener onMediaPlayerComplete = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            addText("Play Completion");
            updateHistory(mediaInfo, 0, 0);
        }
    };
/*
    private PlayerController.Callback playerContrallerCallback = new PlayerController.Callback() {
        @Override
        public int getSectionCount() {
            return PlayerFragment.this.getSectionCount();
        }

        @Override
        public int getCurrentSection() {
            return PlayerFragment.this.getCurrentSection();
        }

        @Override
        public int[] getResolutions() {
            return PlayerFragment.this.getResolutions(
                    PlayerFragment.this.getCurrentSection()
            );
        }

        @Override
        public int getCurrentResolution() {
            return PlayerFragment.this.getCurrentResolution();
        }

        @Override
        public void requestChangeSection(int sectionNumber) {
            PlayerFragment.this.play(
                    PlayerFragment.this.getUrl(),
                    sectionNumber,
                    0
            );
        }

        @Override
        public void reqeustChangeResolution(int resolution) {
            PlayerFragment.this.play(
                    PlayerFragment.this.getUrl(),
                    PlayerFragment.this.getCurrentSection(),
                    PlayerFragment.this.getCurrentSectionOffset(),
                    resolution);
        }
    };
    */
}


