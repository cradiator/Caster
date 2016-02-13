package com.sysdbg.caster.router;

import android.os.Handler;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by crady on 1/19/2016.
 */
public class CmdReceiver extends NanoHTTPD {
    private static final String TAG = "Caster.CmdReceiver";
    private static final short DEFAULT_PORT = 2278;

    private Callback callback;
    private Handler handler;

    public interface Callback {
        void requestPlay(String url);
    }

    public CmdReceiver(Handler handler) {
        this(DEFAULT_PORT, handler);
    }

    public CmdReceiver(short port, Handler handler) {
        super(port);
        this.handler = handler;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        if (method != Method.GET) {
            return null;
        }

        String uri = session.getUri().toString();
        if (uri.startsWith("/play")) {
            return onPlay(session);
        }
        else if (uri.startsWith("/ping")) {
            return onPing(session);
        }

        return createErrorResponse("unsupport action");
    }

    private Response onPlay(IHTTPSession session) {
        String queryString = session.getQueryParameterString();
        if (!queryString.startsWith("url=")) {
            return createErrorResponse("url parameter doesn't exist");
        }

        fireOnPlay(queryString.substring(4));

        return createSuccessResponse("playing");
    }

    private Response onPing(IHTTPSession session) {
        Map<String, String> params = session.getParms();
        Response response;
        if (params == null || !params.containsKey("msg")) {
            response = createSuccessResponse("");
        }
        else {
            response = createSuccessResponse(params.get("msg"));
        }

        return response;
    }

    private Response createErrorResponse(String msg) {
        StringBuffer sb = new StringBuffer();
        sb.append("{status: \"error\", message:\"");
        sb.append(msg);
        sb.append("\"}");

        return newFixedLengthResponse(sb.toString());
    }

    private Response createSuccessResponse(String msg) {
        StringBuffer sb = new StringBuffer();
        sb.append("{status: \"success\", message:\"");
        sb.append(msg);
        sb.append("\"}");

        return newFixedLengthResponse(sb.toString());
    }

    private void fireOnPlay(final String url) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.requestPlay(url);
                }
            }
        });
    }
}
