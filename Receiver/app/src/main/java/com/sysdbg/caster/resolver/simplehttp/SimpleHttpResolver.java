package com.sysdbg.caster.resolver.simplehttp;

import android.os.Handler;
import android.util.Log;

import com.sysdbg.caster.resolver.MediaInfo;
import com.sysdbg.caster.resolver.Resolver;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by crady on 1/24/2016.
 */
abstract public class SimpleHttpResolver extends Resolver {
    private static final String TAG = "Caster.AB";

    public SimpleHttpResolver() {

    }

    @Override
    protected MediaInfo doParse(String mUrl) {
        URL url = null;
        HttpURLConnection httpConn = null;
        try {
            url = new URL(generateRequestUrl(mUrl));
            URLConnection conn = url.openConnection();
            if (!(conn instanceof HttpURLConnection)) {
                Log.e(TAG, "url " + mUrl + " is not http");
                return null;
            }

            httpConn = (HttpURLConnection)conn;
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36");
            beforeSend(httpConn);   // modify header here

            // read response
            InputStream in = new BufferedInputStream(httpConn.getInputStream());
            ByteArrayOutputStream abos = new ByteArrayOutputStream();
            byte[] buffer = new byte[128];
            while(true) {
                int n = in.read(buffer);
                if (n == -1) {
                    break;
                }

                abos.write(buffer, 0, n);
            }

            // parse content
            String content = abos.toString();
            final MediaInfo result = parseContent(content);
            result.setWebPageUrl(mUrl);
            if (result == null) {
                return null;
            }

            return result;
        } catch (MalformedURLException e) {
            Log.e(TAG, "Malformed url " + mUrl, e);
        } catch (IOException e) {
            Log.e(TAG, "Open url " + mUrl + " fail.", e);
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }

        return null;
    }

    abstract protected String generateRequestUrl(String url);
    abstract protected void beforeSend(HttpURLConnection httpConn);
    abstract protected MediaInfo parseContent(String content);
}
