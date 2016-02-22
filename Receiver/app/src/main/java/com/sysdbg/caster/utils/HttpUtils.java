package com.sysdbg.caster.utils;

import android.util.Log;

import org.apache.http.protocol.HTTP;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by crady on 2/21/2016.
 */
public class HttpUtils {
    private static final String TAG = HttpUtils.class.getSimpleName();
    private static final String DEFAULT_UA =
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36";
    private static final String DEFAULT_CHARSET = "UTF-8";

    public static String readPage(URL url,
                                  Map<String, String> additionalRequestHeader,  // optional
                                  Map<String, List<String>> responseHeader) {   // optional
        if (url == null) {
            return null;
        }

        // construct request headers
        Map<String, String> requestHeader = new HashMap<>();
        if (additionalRequestHeader != null) {
            requestHeader.putAll(additionalRequestHeader);
        }

        if (!requestHeader.containsKey("User-Agent")) {
            requestHeader.put("User-Agent", DEFAULT_UA);
        }

        String content = null;
        HttpURLConnection httpConn = null;
        InputStream in = null;
        try {
            // open connection
            URLConnection conn = url.openConnection();
            if (!(conn instanceof HttpURLConnection)) {
                Log.e(TAG, url.toString() + " is not http");
                return null;
            }

            // add header
            httpConn = (HttpURLConnection)conn;
            httpConn.setInstanceFollowRedirects(false);
            for(Map.Entry<String, String> entry : requestHeader.entrySet()) {
                httpConn.addRequestProperty(entry.getKey(), entry.getValue());
            }

            // read response
            in = new BufferedInputStream(httpConn.getInputStream());
            ByteArrayOutputStream abos = new ByteArrayOutputStream();
            byte[] buffer = new byte[128];
            while(true) {
                int n = in.read(buffer);
                if (n == -1) {
                    break;
                }

                abos.write(buffer, 0, n);
            }
            byte[] contentBytes = abos.toByteArray();
            abos.close();

            // translate bytes into array
            Map<String, List<String>> headers = httpConn.getHeaderFields();
            content = convertResponseToString(contentBytes, httpConn.getHeaderFields());

            // write response header
            if (responseHeader != null) {
                responseHeader.clear();
                responseHeader.putAll(headers);
            }
        } catch (IOException e) {
            Log.e(TAG, "open url " + url.toString() + " fail", e);
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        return content;
    }

    private static String convertResponseToString(byte[] response, Map<String, List<String>> responseHeaders) {
        String charset = DEFAULT_CHARSET;
        if (responseHeaders.containsKey("Content-Type")) {
            for(String contentType : responseHeaders.get("Content-Type")) {
                int pos = contentType.indexOf("charset=");
                if (pos < 0) {
                    continue;
                }

                charset = contentType.substring(pos + 8);
                break;
            }
        }

        String content = null;
        try {
            content = new String(response, charset);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "can't find charset " + charset);
            return null;
        }

        return content;
    }
}
