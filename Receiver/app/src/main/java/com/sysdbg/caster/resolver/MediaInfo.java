package com.sysdbg.caster.resolver;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by crady on 2/3/2016.
 */
public class MediaInfo {
    private static final String TAG = "MediaInfo";
    public static int MAX_VIDEO_RESOLUTION = 0x7fffffff;

    private List<List<MediaSection>> mediaSections;
    private String webPageUrl;
    private String imageUrl;
    private String title;
    private String description;

    public MediaInfo() {
        mediaSections = new ArrayList<>();
        webPageUrl = null;
    }

    @Override
    public String toString() {
        return "MediaInfo{" +
                "mediaSections=" + mediaSections +
                ", webPageUrl='" + webPageUrl + '\'' +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addMediaSection(List<MediaSection> sections) {
        mediaSections.add(sections);
    }

    public String getWebPageUrl() {
        return webPageUrl;
    }

    public void setWebPageUrl(String webPageUrl) {
        this.webPageUrl = webPageUrl;
    }

    public int getMediaSectionCount() {
        return mediaSections.size();
    }

    public int[] getResolutions(int sectionNumber) {
        Set<Integer> resolutions = new HashSet<>();

        for(MediaSection section : mediaSections.get(sectionNumber)) {
            resolutions.add(section.getVideoResolution());
        }

        int[] array = new int[resolutions.size()];
        int index = 0;
        Iterator<Integer> it = resolutions.iterator();
        while(it.hasNext()) {
            Integer i = it.next();
            array[index] = i;
            index++;
        }

        Arrays.sort(array);

        return array;
    }

    public MediaSection getMediaPart(int sectionNumber, int videoResolution) {
        if (sectionNumber >= mediaSections.size()) {
            Log.w(TAG, String.format("getMediaPart exceed limit, webUrl %s", webPageUrl));
            return null;
        }

        MediaSection selectedSection = null;
        for(MediaSection section : mediaSections.get(sectionNumber)) {
            if (!section.isContainVideo() || !section.isContainAudio()) {
                continue;
            }

            if (section.getVideoResolution() > videoResolution) {
                continue;
            }

            if (selectedSection == null || section.getVideoResolution() > selectedSection.getVideoResolution()) {
                selectedSection = section;
            }
        }

        if (selectedSection == null) {
            return null;
        }

        return selectedSection.clone();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public static class MediaSection implements Cloneable {
        private String mediaUrl;

        private boolean containVideo;
        private int videoResolution; // such as 720 for 720p

        private boolean containAudio;
        private int audioBitRate;    // such 128 for 128 kbps

        public MediaSection(String mediaUrl, boolean containVideo, int videoResolution, boolean containAudio, int audioBitRate) {
            this.mediaUrl = mediaUrl;
            this.containVideo = containVideo;
            this.videoResolution = videoResolution;
            this.containAudio = containAudio;
            this.audioBitRate = audioBitRate;
        }

        public MediaSection(String mediaUrl, boolean containVideo, boolean containAudio) {
            this(mediaUrl, containVideo, 0, containAudio, 0);
        }

        public MediaSection(String mediaUrl) {
            this(mediaUrl, true, true);
        }

        @Override
        public MediaSection clone() {
            return new MediaSection(mediaUrl, containVideo, videoResolution, containAudio, audioBitRate);
        }

        @Override
        public String toString() {
            return "MediaSection{" +
                    "mediaUrl='" + mediaUrl + '\'' +
                    ", containVideo=" + containVideo +
                    ", videoResolution=" + videoResolution +
                    ", containAudio=" + containAudio +
                    ", audioBitRate=" + audioBitRate +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MediaSection)) return false;

            MediaSection that = (MediaSection) o;

            if (containVideo != that.containVideo) return false;
            if (videoResolution != that.videoResolution) return false;
            if (containAudio != that.containAudio) return false;
            if (audioBitRate != that.audioBitRate) return false;
            return mediaUrl.equals(that.mediaUrl);

        }

        @Override
        public int hashCode() {
            int result = mediaUrl.hashCode();
            result = 31 * result + (containVideo ? 1 : 0);
            result = 31 * result + videoResolution;
            result = 31 * result + (containAudio ? 1 : 0);
            result = 31 * result + audioBitRate;
            return result;
        }

        public String getMediaUrl() {
            return mediaUrl;
        }

        public void setMediaUrl(String mediaUrl) {
            this.mediaUrl = mediaUrl;
        }

        public boolean isContainVideo() {
            return containVideo;
        }

        public void setContainVideo(boolean containVideo) {
            this.containVideo = containVideo;
        }

        public int getVideoResolution() {
            return videoResolution;
        }

        public void setVideoResolution(int videoResolution) {
            this.videoResolution = videoResolution;
        }

        public boolean isContainAudio() {
            return containAudio;
        }

        public void setContainAudio(boolean containAudio) {
            this.containAudio = containAudio;
        }

        public int getAudioBitRate() {
            return audioBitRate;
        }

        public void setAudioBitRate(int audioBitRate) {
            this.audioBitRate = audioBitRate;
        }
    }
}
