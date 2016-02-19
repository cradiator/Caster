package com.sysdbg.caster.resolver.simplehttp;

import android.util.Log;

import com.sysdbg.caster.resolver.MediaInfo;
import com.sysdbg.caster.utils.StringUtils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by crady on 2/17/2016.
 */
public class FlvcdResolver extends SimpleHttpResolver {
    private static final Pattern URL_REGEX = Pattern.compile("href=\"(.*?k.youku.com.*?)\"\\s+target=\"");
    private static final Pattern TITLE_REGEX = Pattern.compile("document.title\\s+=\\s+\"(.*?)\"");

    @Override
    protected String generateRequestUrl(String url) {
        url = StringUtils.removeQueryFromUrl(url);
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        return "http://www.flvcd.com/parse.php?kw=" + url + "&format=high";
    }

    @Override
    protected void beforeSend(HttpURLConnection httpConn) {

    }

    @Override
    protected MediaInfo parseContent(String content) {
        // parse URL
        List<String> urls = new ArrayList<>();

        Matcher matcher = URL_REGEX.matcher(content);
        while(matcher.find()  && matcher.groupCount() == 1) {
            Log.e("abc", new Integer(matcher.groupCount()).toString());
            String url = matcher.group(1);
            urls.add(url);
        }

        if (urls.size() == 0) {
            return null;
        }

        MediaInfo mi = new MediaInfo();
        for(String url : urls) {
            MediaInfo.MediaSection ms = new MediaInfo.MediaSection(url);
            List<MediaInfo.MediaSection> lms = new ArrayList<>();
            lms.add(ms);

            mi.addMediaSection(lms);
        }

        // parse title
        matcher = TITLE_REGEX.matcher(content);
        if (matcher.find() && matcher.groupCount() == 1) {
            String title = matcher.group(1);
            mi.setTitle(title);
        }

        return mi;
    }
}
