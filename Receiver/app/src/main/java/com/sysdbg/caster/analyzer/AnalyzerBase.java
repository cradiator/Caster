package com.sysdbg.caster.analyzer;

import android.util.Log;

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
abstract public class AnalyzerBase {
    private static final String TAG = "Caster.AB";

    public interface Callback {
        void onResult(String resultUrl);
    }

    public AnalyzerBase() {

    }

    public void parse(String url, Callback callback) {
        WorkerThread thread = new WorkerThread(url, callback);
        thread.start();
    }

    abstract protected String generateRequestUrl(String url);
    abstract protected void beforeSend(HttpURLConnection httpConn);
    abstract protected String parseContent(String content);

    private class WorkerThread extends Thread {
        private Callback mCallback;
        private String mUrl;

        public WorkerThread(String url, Callback callback) {
            super();

            mUrl = url;
            mCallback = callback;
        }

        @Override
        public void run() {
            URL url = null;
            HttpURLConnection httpConn = null;
            try {
                url = new URL(generateRequestUrl(mUrl));
                URLConnection conn = url.openConnection();
                if (!(conn instanceof HttpURLConnection)) {
                    Log.e(TAG, "url " + mUrl + " is not http");
                    return;
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
                String result = parseContent(content);
                if (result == null || result.length() == 0) {
                    return;
                }

                if (mCallback != null) {
                    mCallback.onResult(result);
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Malformed url " + mUrl, e);
            } catch (IOException e) {
                Log.e(TAG, "Open url " + mUrl + " fail.", e);
            } finally {
                if (httpConn != null) {
                    httpConn.disconnect();
                }
            }
        }
    }
}
