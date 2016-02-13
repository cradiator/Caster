package com.sysdbg.caster.resolver;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by crady on 2/8/2016.
 */
public class PlainUrlResolver extends Resolver {
    @Override
    protected MediaInfo doParse(final String url) {
        final MediaInfo info = new MediaInfo();
        info.setWebPageUrl(url);

        MediaInfo.MediaSection section = new MediaInfo.MediaSection(url);
        List<MediaInfo.MediaSection> sections = new ArrayList<>();
        sections.add(section);

        info.addMediaSection(sections);
        return info;
    }
}
