package com.sysdbg.caster.resolver;

import android.util.Base64;
import android.util.Log;

import com.sysdbg.caster.utils.HttpUtils;
import com.sysdbg.caster.utils.MD5;
import com.sysdbg.caster.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by crady on 2/21/2016.
 */
public class YoukuResolver extends Resolver {
    private static final String TAG = YoukuResolver.class.getSimpleName();
    private static final String COOKIES_URL = "http://p.l.youku.com/ypvlog";
    private static final String VIDEO_INFO_URL = "http://play.youku.com/play/get.json?ct=12&vid=";
    private static final String M3U_URL = "http://pl.youku.com/playlist/m3u8?ctype=12&ev=1&keyframe=1";
    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("id_(.*?==)");
    private static final Pattern SET_COOKIE_PATTERN = Pattern.compile("(.*?;)\\s*?domain");

    @Override
    protected MediaInfo doParse(String url) throws Throwable {
        String videoId = getVideoId(url);
        if (videoId == null) {
            Log.e(TAG, "no video id in " + url);
            return null;
        }

        String cookie = getCookies();
        JSONObject videoInfo = getVideoInfo(videoId, cookie);
        if (videoInfo == null) {
            return null;
        }

        MediaInfo mediaInfo = new MediaInfo();
        if (!parseMediaData(mediaInfo, videoInfo, cookie)) {
            return null;
        }

        parseBasicInfo(mediaInfo, videoInfo, url);

        return mediaInfo;
    }

    private void parseBasicInfo(MediaInfo mediaInfo, JSONObject videoInfo, String url) throws MalformedURLException, JSONException {
        mediaInfo.setWebPageUrl(new URL(url));

        JSONObject data = videoInfo.getJSONObject("data");
        if (data == null) {
            return;
        }

        JSONObject video = data.getJSONObject("video");
        if (video == null) {
            return;
        }

        String title = video.getString("title");
        String img = video.getString("logo");

        mediaInfo.setTitle(title);
        try {
            mediaInfo.setImageUrl(new URL(img));
        } catch (MalformedURLException e) {

        }
    }

    private boolean parseMediaData(MediaInfo mediaInfo, JSONObject videoInfo, String cookie) {
        String m3uFileBase = getM3uFileUrl(videoInfo);
        MediaInfo.MediaData mediaData = genMediaData(m3uFileBase + "flv", cookie);
        if (mediaData != null) {
            mediaData.setDefinition("Normal");
            mediaInfo.addData(mediaData);
        }

        mediaData = genMediaData(m3uFileBase + "mp4", cookie);
        if (mediaData != null) {
            mediaData.setDefinition("High");
            mediaInfo.addData(mediaData);
        }

        mediaData = genMediaData(m3uFileBase + "hd2", cookie);
        if (mediaData != null) {
            mediaData.setDefinition("SuperHigh");
            mediaInfo.addData(mediaData);
        }

        if (mediaInfo.getDefinitions().size() == 0) {
            return false;
        }

        return true;
    }

    private String getVideoId(String url) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private MediaInfo.MediaData genMediaData(String url, String cookie) {
        Map<String, String> requestHeader = new HashMap<>();
        // requestHeader.put("Cookie", cookie);

        String content = null;
        try {
            content = HttpUtils.readPage(new URL(url), requestHeader, null);
        } catch (MalformedURLException e) {
            Log.e(TAG, "read url fail", e);
            return null;
        }

        if (StringUtils.isEmpty(content)) {
            return null;
        }

        final String contentFinal = content;
        MediaInfo.MediaData data = new MediaInfo.MediaData() {
            @Override
            public void generateM3uWithLocations(OutputStream os) {
                try {
                    os.write(contentFinal.getBytes("UTF-8"));
                } catch (IOException e) {

                }
            }
        };

        return data;
    }

    private String getM3uFileUrl(JSONObject videoInfo) {
        try {
            JSONObject jsonData = videoInfo.getJSONObject("data");
            String ep = null;
            if (jsonData.has("security")) {
                ep = jsonData.getJSONObject("security").getString("encrypt_string");
            }

            if (StringUtils.isEmpty(ep)) {
                return null;
            }

            String oip = jsonData.getJSONObject("security").getString("ip");
            String vid = jsonData.getJSONObject("video").getString("encodeid");
            String temp = new String(OvO("becaf9be", Base64.decode(ep, Base64.DEFAULT)));
            String sid = temp.split("_")[0];
            String token = temp.split("_")[1];

            String s = sid + "_" + vid + "_" + token;
            byte[] bb = OvO("bf7e5f01", s.getBytes());
            try {
                s = new String(Base64.encode(bb, Base64.DEFAULT), "US-ASCII");
                s = URLEncoder.encode(s, "UTF-8");
                ep = s;
                ep = ep.substring(0, ep.length() - 3);

                return M3U_URL + "&ep=" + ep + "&oip=" + oip + "&sid=" + sid +"&token=" + token + "&vid=" + vid + "&type=";
            } catch (UnsupportedEncodingException e) {
            }

        } catch (JSONException e) {
            Log.e(TAG, "parse youku json fail", e);
        }

        return null;
    }

    private byte[] OvO(String a, byte[] c) {
        int f = 0;
        int i = 0;
        int h = 0;
        Map<Integer, Integer> b = new HashMap<>();
        String e = "";

        for(h = 0; h < 256; ++h) {
            b.put(h, h);
        }

        for(h = 0; h < 256; ++h) {
            f = ((f + b.get(h)) + charCodeAt(a, h % a.length())) % 256;
            i = b.get(h);
            b.put(h, b.get(f));
            b.put(f, i);
        }

        f = h = 0;
        byte[] result = new byte[c.length];
        for(int q = 0; q < c.length; q++) {
            h = (h + 1) % 256;
            f = (f + b.get(h)) % 256;
            i = b.get(h);
            b.put(h, b.get(f));
            b.put(f, i);
            result[q] = (byte)((c[q] & 0xff) ^ b.get((b.get(h) + b.get(f)) % 256));
        }

        return result;
    }

    private int charCodeAt(String data, int index) {
        return data.getBytes()[index];
    }

    private JSONObject getVideoInfo(String videoId, String cookie) {
        Map<String, String> requestHeader = new HashMap<>();
        requestHeader.put("Referer", "http://c-h5.youku.com/");
        if (!StringUtils.isEmpty(cookie)) {
            requestHeader.put("Cookie", cookie);
        }

        String content = null;
        try {
            content = HttpUtils.readPage(new URL(VIDEO_INFO_URL + videoId), requestHeader, null);
        } catch (MalformedURLException e) {
            Log.e(TAG, "invalid url " + VIDEO_INFO_URL + videoId);
            return null;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(content);
        } catch (JSONException e) {
            Log.e(TAG, "parse video info fail", e);
            return null;
        }

        return jsonObject;
    }

    private String getCookies() {
        Map<String, List<String>> responseHeaders = new HashMap<>();
        try {
            HttpUtils.readPage(new URL(COOKIES_URL), null, responseHeaders);
        } catch (MalformedURLException e) {
        }

        List<String> setCookies = responseHeaders.get("Set-Cookie");
        if (setCookies == null || setCookies.size() == 0) {
            Log.w(TAG, "Get youku cookit fail");
            return null;
        }

        StringBuffer sb = new StringBuffer();
        for(String setCookie : setCookies) {
            Matcher matcher = SET_COOKIE_PATTERN.matcher(setCookie);
            if (matcher.find()) {
                sb.append(matcher.group(1).trim());
                sb.append(" ");
            }
        }

        return sb.toString();
    }
}
