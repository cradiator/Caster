package com.sysdbg.caster.resolver.simplehttp;

import android.util.Log;

import com.sysdbg.caster.resolver.MediaInfo;
import com.sysdbg.caster.resolver.simplehttp.SimpleHttpResolver;
import com.sysdbg.caster.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
    protected URL generateRequestUrl(URL webPageUrl) throws Throwable {
        return new URL("http://srv1.keepvid.com/api/v2.php?url=" + webPageUrl.toString());
    }

    @Override
    protected void beforeSend(HttpURLConnection httpConn) {
        httpConn.setRequestProperty("Referer", "http://keepvid.com/");
        httpConn.setRequestProperty("Host", "srv1.keepvid.com");
    }

    @Override
    protected MediaInfo parseContent(String content, URL webPageUrl, URL requestUrl) throws Throwable {
        if (content == null || content.length() <= 0) {
            return null;
        }

        MediaInfo info = parseJson(content);
        return info;
    }

    private MediaInfo parseJson(String content) throws MalformedURLException {
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

            MediaInfo mediaInfo = findSuitableUrl(links);
            parseInformation(json.getJSONObject("info"), mediaInfo);

            return mediaInfo;
        } catch (JSONException e) {
            Log.e(TAG, "json invalid", e);
        }

        return null;
    }

    private void parseInformation(JSONObject info, MediaInfo mediaInfo) {
        String title = info.optString("title");
        String image = info.optString("image");

        mediaInfo.setTitle(title);
        try {
            mediaInfo.setImageUrl(new URL(image));
        } catch (MalformedURLException e) {
            Log.e(TAG, "image url invalid", e);
        }
    }

    private MediaInfo findSuitableUrl(JSONObject json) throws MalformedURLException {
        Iterator<String> it = json.keys();

        String quality = null, url = null;
        while(it.hasNext()) {
            String key = it.next();

            try {
                JSONObject current = json.getJSONObject(key);
                String type = current.getString("type");
                if (type == null || type.compareToIgnoreCase("mp4") != 0) {
                    continue;
                }

                url = current.getString("url");
                if (url == null) {
                    continue;
                }

                quality = current.getString("quality");
                if (quality == null || quality.contains("Only") || quality.contains("only")) {
                    continue;
                }
            } catch (JSONException e) {
            }
        }

        if (StringUtils.isEmpty(quality)) {
            quality = "Unknown";
        }

        if (StringUtils.isEmpty(url)) {
            Log.e(TAG, "Can find suitable url");
            return null;
        }

        return MediaInfo.builder()
                .withData(quality, null, url)
                .build();
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
