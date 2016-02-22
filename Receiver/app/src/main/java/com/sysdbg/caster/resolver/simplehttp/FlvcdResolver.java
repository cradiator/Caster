package com.sysdbg.caster.resolver.simplehttp;

import android.util.Log;

import com.sysdbg.caster.resolver.MediaInfo;
import com.sysdbg.caster.utils.StringUtils;

import java.net.HttpURLConnection;
import java.net.URL;
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
    protected URL generateRequestUrl(URL webPageUrl) throws Throwable {
        String url = StringUtils.removeQueryFromUrl(webPageUrl.toString());
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        return new URL("http://www.flvcd.com/parse.php?kw=" + url + "&format=high");
    }

    @Override
    protected void beforeSend(HttpURLConnection httpConn) {

    }

    @Override
    protected MediaInfo parseContent(String content, URL webPageUrl, URL requestUrl) throws Throwable {
        // parse URL
        List<String> urls = new ArrayList<>();

        Matcher matcher = URL_REGEX.matcher(content);
        while(matcher.find()  && matcher.groupCount() == 1) {
            String url = matcher.group(1);
            urls.add(url);
        }
        String[] urlsArray = new String[urls.size()];
        urls.toArray(urlsArray);

        if (urls.size() == 0) {
            return null;
        }

        // parse title
        String title = null;
        matcher = TITLE_REGEX.matcher(content);
        if (matcher.find() && matcher.groupCount() == 1) {
            title = matcher.group(1);
        }

        return MediaInfo.builder()
                .withTitle(title)
                .withWebPageUrl(StringUtils.removeQueryFromUrl(webPageUrl.toString()))
                .withData("High", null, urlsArray)
                .build();
    }
}
