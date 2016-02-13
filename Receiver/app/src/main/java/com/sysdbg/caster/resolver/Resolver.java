package com.sysdbg.caster.resolver;

import android.os.Handler;
import android.util.Log;

import com.sysdbg.caster.resolver.simplehttp.KeepvidResolver;

/**
 * Created by crady on 2/8/2016.
 */
abstract public class Resolver {
    private static final String TAG = Resolver.class.getSimpleName();

    public interface Callback {
        void onResult(MediaInfo mediaInfo);
    }

    public static void parse(String url, Callback callback, Handler handler) {
        Resolver resolver = null;
        if (url.contains("youtube.com")) {
            resolver = new KeepvidResolver();
        }
        else if (url.contains("bilibili.com")) {
            resolver = new BilibiliResolver();
        }
        else {
            resolver = new PlainUrlResolver();
        }

        resolver.parseInternal(url, callback, handler);
    }

    private void parseInternal(String url, Callback callback, Handler handler) {
        WorkerThread thread = new WorkerThread(url, callback, handler);
        thread.start();
    }

    abstract protected MediaInfo doParse(String url) throws Throwable;

    private class WorkerThread extends Thread {
        private String url;
        private Callback callback;
        private Handler handler;

        public WorkerThread(String url, Callback callback, Handler handler){
            this.url = url;
            this.callback = callback;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                MediaInfo info = null;
                try {
                    info = doParse(url);
                }
                catch (Throwable e) {
                    Log.d(TAG, "parse fail", e);
                }

                if (callback != null) {
                    final MediaInfo mi = info;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResult(mi);
                        }
                    });
                }
            }
            catch (Throwable e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }
}
