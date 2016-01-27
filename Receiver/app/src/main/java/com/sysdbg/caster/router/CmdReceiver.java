package com.sysdbg.caster.router;

import android.os.Handler;
import android.support.annotation.Keep;
import android.util.Log;

import com.sysdbg.caster.MainActivity;
import com.sysdbg.caster.analyzer.AnalyzerBase;
import com.sysdbg.caster.analyzer.KeepvidAnalyzer;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by crady on 1/19/2016.
 */
public class CmdReceiver extends NanoHTTPD implements AnalyzerBase.Callback {
    private static final String TAG = "Caster.CmdReceiver";
    private static final short DEFAULT_PORT = 2278;

    private MainActivity mMainActivity;
    private KeepvidAnalyzer mKeepvidAnalyzer;

    public CmdReceiver(MainActivity mainActivity) {
        this(DEFAULT_PORT, mainActivity);
    }

    public CmdReceiver(short port, MainActivity mainActivity) {
        super(port);

        mMainActivity = mainActivity;
        mKeepvidAnalyzer = new KeepvidAnalyzer();
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        if (method != Method.GET) {
            return null;
        }

        String uri = session.getUri().toString();
        if (uri.startsWith("/playmp4")) {
            return onPlayMp4(session);
        }
        else if (uri.startsWith("/ping")) {
            return onPing(session);
        }
        else if (uri.startsWith("/youtube")) {
            return onYoutube(session);
        }

        return createErrorResponse("unsupport action");
    }

    private Response onPlayMp4(IHTTPSession session) {
        String queryString = session.getQueryParameterString();
        if (!queryString.startsWith("url=")) {
            return createErrorResponse("url parameter doesn't exist");
        }

        mMainActivity.requestPlayMp4(queryString.substring(4));

        return createSuccessResponse("playing");
    }

    private Response onYoutube(IHTTPSession session) {
        String queryString = session.getQueryParameterString();
        if (!queryString.startsWith("url=")) {
            return createErrorResponse("url parameter doesn't exist");
        }

        mKeepvidAnalyzer.parse(queryString.substring(4), this);
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

    @Override
    public void onResult(String resultUrl) {
        mMainActivity.requestPlayMp4(resultUrl);
    }
}
