package com.sysdbg.caster.resolver;

import android.util.Log;

import com.sysdbg.caster.utils.MD5;
import com.sysdbg.caster.utils.StringUtils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by crady on 2/8/2016.
 */
public class BilibiliResolver extends Resolver {
    private static final String TAG = BilibiliResolver.class.getSimpleName();
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.99 Safari/537.36";
    private static final String APPKEY = "8e9fc618fbd41e28";
    private static final String APPSEC = "";

    private static final String API_BASE = "http://api.bilibili.com/view?";
    private static final String INTERFACE_BASE = "http://interface.bilibili.com/playurl?";
    private static Pattern URL_MATCHER = Pattern.compile("http:/*[^/]+/video/av(\\d+)(/|/index.html|/index_(\\d+).html)?(\\?|#|$)");

    private static Random RANDOM = new Random();

    @Override
    protected MediaInfo doParse(String url) throws Throwable {
        Matcher matcher = URL_MATCHER.matcher(url);
        if (!matcher.matches()) {
            return null;
        }

        String aid = matcher.group(1);
        String pid = matcher.group(3);
        if (pid == null) {
            pid = "1";
        }

        Map<String, String> cid_args = new HashMap<>();
        cid_args.put("type", "json");
        cid_args.put("id", aid);
        cid_args.put("page", pid);
        String apiUrl = API_BASE + getSign(cid_args);
        String message0 = fetchUrl(apiUrl);

        JSONObject json = new JSONObject(message0);
        String title = json.getString("title");
        String description = json.getString("description");

        String cid = json.getString("cid");
        Map<String, String> params = new HashMap<>();
        params.put("otype", "json");
        params.put("cid", cid);
        params.put("type", "mp4");
        params.put("quality", "4");
        params.put("appkey", APPKEY);
        String interfaceUrl = INTERFACE_BASE + generateQueryParams(params);
        String message1 = fetchUrl(interfaceUrl);

        JSONObject json1 = new JSONObject(message1);
        String result = json1.getString("result");
        if (result == null || result.equals("error")) {
            Log.e(TAG, "error when parsing bilibili");
            return null;
        }

        String mediaAddress = json1.getJSONArray("durl").getJSONObject(0).getString("url");
        return generateMediaInfo(mediaAddress, url, json.getString("pic"), title, description);
    }

    private MediaInfo generateMediaInfo(String mediaAddress, String webPageUrl, String imageUrl, String title, String description) throws MalformedURLException {
        MediaInfo info = MediaInfo.builder()
                .withData("Max", null, mediaAddress)
                .withWebPageUrl(webPageUrl)
                .withImageUrl(imageUrl)
                .withDescription(description)
                .withTitle(title)
                .build();

        return info;
    }

    private String generateQueryParams(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        for(Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }

            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
        }

        return sb.toString();
    }

    private String getSign(Map<String, String> params) {
        params.put("appkey", APPKEY);

        Set<String> keySet = params.keySet();
        String[] keys = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keys);

        StringBuilder sb = new StringBuilder();
        for(String key : keys) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key);
            sb.append("=");
            sb.append(params.get(key));
        }

        if (!StringUtils.isEmpty(APPSEC)) {
            String sign = MD5.md5(sb.toString() + APPSEC);
            sb.append("&sign=");
            sb.append(sign);
        }

        return sb.toString();
    }

    private String fetchUrl(String url) throws MalformedURLException, IOException {
        URL u = new URL(url);
        HttpURLConnection conn = null;
        InputStream in = null;
        String content = null;
        try {
            conn = (HttpURLConnection)u.openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            String ip = randomIp();
            conn.setRequestProperty("Client-IP", ip);
            conn.setRequestProperty("X-Forwarded-For", ip);

            in = new BufferedInputStream(conn.getInputStream());
            ByteArrayOutputStream abos = new ByteArrayOutputStream();
            byte[] buffer = new byte[128];
            while(true) {
                int n = in.read(buffer);
                if (n == -1) {
                    break;
                }

                abos.write(buffer, 0, n);
            }

            content = abos.toString("utf-8");
        }
        catch (Throwable e) {
            throw e;
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }

            if (in != null) {
                in.close();
            }
        }

        return content;
    }

    private String randomIp() {
        int random = RANDOM.nextInt(255);
        if (random % 2 == 0) {
            return "220.181.111." + Integer.toString(random);
        }
        else {
            return "59.152.193." + Integer.toString(random);
        }
    }
}
