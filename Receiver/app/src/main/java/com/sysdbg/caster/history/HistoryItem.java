package com.sysdbg.caster.history;

import android.util.Log;

import com.sysdbg.caster.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by crady on 2/13/2016.
 */

public class HistoryItem {
    private static final String TAG = HistoryItem.class.getSimpleName();
    private static final String WEB_URL = "WebUrl";
    private static final String IMG_URL = "ImgUrl";
    private static final String OFFSET = "Offset";
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";

    private String webUrl;
    private String imgUrl;
    private long offset;
    private String title;
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public JSONObject toJosn() {
        JSONObject result = null;

        try {
            JSONObject json = new JSONObject();
            if (StringUtils.isEmpty(getWebUrl())) {
                return null;
            }
            json.put(WEB_URL, getWebUrl());

            if (!StringUtils.isEmpty(getImgUrl())) {
                json.put(IMG_URL, getImgUrl());
            }

            if (!StringUtils.isEmpty(getTitle())) {
                json.put(TITLE, getTitle());
            }

            if (!StringUtils.isEmpty(getDescription())) {
                json.put(DESCRIPTION, getDescription());
            }

            json.put(OFFSET, getOffset());

            result = json;
        }
        catch (JSONException e) {
            Log.e(TAG, "toJson fail", e);
        }

        return result;
    }

    public static HistoryItem fromJosn(JSONObject json) {
        try {
            String webUrl = json.getString(WEB_URL);
            String imgUrl = json.optString(IMG_URL, null);
            String title = json.optString(TITLE, null);
            String description = json.optString(DESCRIPTION, null);
            int offset = json.optInt(OFFSET, 0);

            HistoryItem item = new HistoryItem();
            item.setWebUrl(webUrl);
            item.setImgUrl(imgUrl);
            item.setTitle(title);
            item.setDescription(description);
            item.setOffset(offset);

            return item;
        }
        catch (JSONException e) {
            Log.e(TAG, "fromJson fail", e);
        }

        return null;
    }
}

