package com.sysdbg.caster.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by crady on 2/13/2016.
 */
public class StringUtils {
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }
    public static String removeQueryFromUrl(String url) {
        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(u.getProtocol());
        sb.append("://");
        sb.append(u.getHost());
        if (u.getPort() > 0) {
            sb.append(":");
            sb.append(u.getPort());
        }
        sb.append(u.getPath());

        return sb.toString();
    }
}
