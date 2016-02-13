package com.sysdbg.caster.resolver.simplehttp;

import android.util.Log;

import com.sysdbg.caster.resolver.MediaInfo;
import com.sysdbg.caster.resolver.simplehttp.SimpleHttpResolver;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by crady on 1/24/2016.
 */
public class KeepvidResolver extends SimpleHttpResolver {
    private static final String TAG = "Caster.KVA";
    private static Pattern QUALITY_PATTERN = Pattern.compile("(\\d*)[pP]");

    public KeepvidResolver() {

    }

    @Override
    protected String generateRequestUrl(String url) {
        return "http://srv1.keepvid.com/api/v2.php?url=" + URLEncoder.encode(url);
    }

    @Override
    protected void beforeSend(HttpURLConnection httpConn) {
        httpConn.setRequestProperty("Referer", "http://keepvid.com/");
        httpConn.setRequestProperty("Host", "srv1.keepvid.com");
    }

    @Override
    protected MediaInfo parseContent(String content) {
        if (content == null || content.length() <= 0) {
            return null;
        }

        MediaInfo info = parseJson(content);
        return info;
    }

    private MediaInfo parseJson(String content) {
        JSONObject json = null;

        try {
            json = new JSONObject(content);
            String err = json.getString("error");
            if (err != null && err.length() > 0) {
                Log.e(TAG, "Keepvid return error code " + err);
                return null;
            }

            JSONObject links = json.getJSONObject("download_links");
            if (links == null) {
                Log.e(TAG, "Keepvid return no download_links");
                return null;
            }

            return findSuitableUrl(links);
        } catch (JSONException e) {
            Log.e(TAG, "json invalid", e);
        }

        return null;
    }

    private MediaInfo findSuitableUrl(JSONObject json) {
        MediaInfo mediaInfo = new MediaInfo();
        List<MediaInfo.MediaSection> sectionList = new ArrayList<>();

        Iterator<String> it = json.keys();
        while(it.hasNext()) {
            String key = it.next();

            try {
                JSONObject current = json.getJSONObject(key);
                String type = current.getString("type");
                if (type == null || type.compareToIgnoreCase("mp4") != 0) {
                    continue;
                }

                String url = current.getString("url");
                if (url == null) {
                    continue;
                }

                String quality = current.getString("quality");
                if (quality == null || quality.contains("Only") || quality.contains("only")) {
                    continue;
                }

                int resolution = extractQuality(quality);
                MediaInfo.MediaSection section = new MediaInfo.MediaSection(url, true, resolution, true, 0);
                sectionList.add(section);
            } catch (JSONException e) {
            }
        }

        mediaInfo.addMediaSection(sectionList);
        return mediaInfo;
    }

    private int compareQuality(String quality1, String quality2) {
        return extractQuality(quality1) - extractQuality(quality2);
    }

    private int extractQuality(String quality) {
        Matcher matcher = QUALITY_PATTERN.matcher(quality);
        if (matcher.find()) {
            String q = matcher.group(1);
            return Integer.parseInt(q);
        }

        return 0;
    }
}