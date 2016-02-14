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
    private static final String TOTAL_SECTION = "TotalSection";
    private static final String CURRENT_SECTION = "CurrentSection";
    private static final String CURRENT_OFFSET = "CurrentOffset";
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";

    private String webUrl;
    private String imgUrl;
    private int totalSection;
    private int currentSection;
    private int currentOffset;
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

    public int getTotalSection() {
        return totalSection;
    }

    public void setTotalSection(int totalSection) {
        this.totalSection = totalSection;
    }

    public int getCurrentSection() {
        return currentSection;
    }

    public void setCurrentSection(int currentSectioin) {
        this.currentSection = currentSectioin;
    }

    public int getCurrentOffset() {
        return currentOffset;
    }

    public void setCurrentOffset(int currentOffset) {
        this.currentOffset = currentOffset;
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

            json.put(TOTAL_SECTION, getTotalSection());
            json.put(CURRENT_SECTION, getCurrentSection());
            json.put(CURRENT_OFFSET, getCurrentOffset());

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
            int totalSection = json.optInt(TOTAL_SECTION);
            int currentSection = json.optInt(CURRENT_SECTION);
            int currentOffset = json.optInt(CURRENT_OFFSET);

            HistoryItem item = new HistoryItem();
            item.setWebUrl(webUrl);
            item.setImgUrl(imgUrl);
            item.setTitle(title);
            item.setDescription(description);
            item.setTotalSection(totalSection);
            item.setCurrentSection(currentSection);
            item.setCurrentOffset(currentOffset);

            return item;
        }
        catch (JSONException e) {
            Log.e(TAG, "fromJson fail", e);
        }

        return null;
    }
}

