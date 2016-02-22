package com.sysdbg.caster.resolver;

import android.util.Log;

import com.sysdbg.caster.utils.StringUtils;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.vov.vitamio.provider.MediaStore;

/**
 * Created by crady on 2/3/2016.
 */
public class MediaInfo {
    private static final String TAG = "MediaInfo";
    public static final String MAX_VIDEO_RESOLUTION = TAG + "_MAX_VIDEO_RESOLUTION";

    private List<String> definitions;   // from low to high
    private Map<String, MediaData> definitionMediaDataMap;

    private URL webPageUrl;
    private URL imageUrl;

    private String title;
    private String description;

    public MediaInfo() {
        definitions = new ArrayList<>();
        definitionMediaDataMap = new HashMap<>();
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

    public URL getWebPageUrl() {
        return webPageUrl;
    }

    public void setWebPageUrl(URL webPageUrl) {
        this.webPageUrl = webPageUrl;
    }

    public URL getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void addData(MediaData data) {
        definitions.add(data.getDefinition());
        definitionMediaDataMap.put(data.getDefinition(), data);
    }

    public MediaData getData(String definition) {
        if (definitions.size() == 0) {
            return null;
        }

        if (MAX_VIDEO_RESOLUTION.equals(definition)) {
            definition = definitions.get(definitions.size() - 1);
        }

        return definitionMediaDataMap.get(definition);
    }

    public List<String> getDefinitions() {
        return new ArrayList<String>(definitions);
    }

    public static class MediaData {
        private static final String TAG = MediaData.class.getSimpleName();

        private List<URL> dataLocations;
        private String definition;

        private URL m3uLocation;    // optional

        public MediaData() {
            dataLocations = new ArrayList<>();
        }

        public void addData(URL url) {
            if (url == null) {
                Log.e(TAG, "addData with null url");
                return;
            }

            dataLocations.add(url);
        }

        public void clearData() {
            dataLocations.clear();
        }

        public List<URL> getDataLocations() {
            return Collections.unmodifiableList(dataLocations);
        }

        public URL getM3uLocation() {
            return m3uLocation;
        }

        public void setM3uLocation(URL m3uLocation) {
            this.m3uLocation = m3uLocation;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        public void generateM3uWithLocations(OutputStream os) {
            // TODO: Implement it!
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private static final String TAG = Builder.class.getSimpleName();
        private MediaInfo mediaInfo;

        public Builder() {
            mediaInfo = new MediaInfo();
        }

        public Builder withWebPageUrl(URL url) {
            mediaInfo.setWebPageUrl(url);
            return this;
        }

        public Builder withWebPageUrl(String url) throws MalformedURLException {
            mediaInfo.setWebPageUrl(new URL(url));
            return this;
        }

        public Builder withImageUrl(URL url) {
            mediaInfo.setImageUrl(url);
            return this;
        }

        public Builder withImageUrl(String url) throws MalformedURLException {
            mediaInfo.setImageUrl(new URL(url));
            return this;
        }

        public Builder withTitle(String title) {
            mediaInfo.setTitle(title);
            return this;
        }

        public Builder withDescription(String description) {
            mediaInfo.setDescription(description);
            return this;
        }

        public Builder withData(String definition, URL m3uLocation, URL... locations) {
            if (locations == null || locations.length == 0) {
                Log.e(TAG, "Add data with empty locations, skip");
                return this;
            }
            if (StringUtils.isEmpty(definition)) {
                Log.e(TAG, "Add data with empty definition, skip");
                return this;
            }

            MediaData mediaData = new MediaData();
            mediaData.setDefinition(definition);
            mediaData.setM3uLocation(m3uLocation);

            for(URL location : locations) {
                mediaData.addData(location);
            }

            mediaInfo.addData(mediaData);
            return this;
        }

        public Builder withData(String definition, String m3uLocation, String... locations) throws MalformedURLException {
            if (locations == null || locations.length == 0) {
                Log.e(TAG, "Add data with empty locations, skip");
                return this;
            }
            if (StringUtils.isEmpty(definition)) {
                Log.e(TAG, "Add data with empty definition, skip");
                return this;
            }

            MediaData mediaData = new MediaData();
            mediaData.setDefinition(definition);
            if (!StringUtils.isEmpty(m3uLocation)) {
                mediaData.setM3uLocation(new URL(m3uLocation));
            }

            for(String location : locations) {
                mediaData.addData(new URL(location));
            }

            mediaInfo.addData(mediaData);
            return this;
        }

        public MediaInfo build() {
            MediaInfo returnValue = mediaInfo;
            mediaInfo = new MediaInfo();
            return returnValue;
        }
    }
}
