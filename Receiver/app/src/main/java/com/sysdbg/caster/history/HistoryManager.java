package com.sysdbg.caster.history;

import android.content.Context;
import android.util.Log;

import com.sysdbg.caster.history.persistant.JosnFilePersistant;
import com.sysdbg.caster.history.persistant.Persistant;
import com.sysdbg.caster.history.persistant.SqlitePersistant;
import com.sysdbg.caster.resolver.MediaInfo;
import com.sysdbg.caster.utils.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by crady on 2/13/2016.
 */
public class HistoryManager {
    private static final String TAG = HistoryManager.class.getSimpleName();

    private static HistoryManager instance;

    private Context context;
    private Persistant persistant;
    private Map<String, List<HistoryItem>> domainHistoryItemMap;

    public static HistoryManager getInstance(Context context) {
        if (instance != null) {
            return instance;
        }

        instance = new HistoryManager(context, new SqlitePersistant(context));
        return instance;
    }

    public static void saveMediaInfo(Context context, MediaInfo info, long offset) {
        if (info == null) {
            return;
        }

        HistoryItem item = new HistoryItem();
        item.setTitle(info.getTitle());
        item.setDescription(info.getDescription());
        item.setOffset(offset);
        if (info.getWebPageUrl() != null) {
            item.setWebUrl(info.getWebPageUrl().toString());
        }
        if (info.getImageUrl() != null) {
            item.setImgUrl(info.getImageUrl().toString());
        }

        getInstance(context).saveItem(item);
    }

    public HistoryManager(Context context, Persistant persistant) {
        this.context = context;
        this.persistant = persistant;
        domainHistoryItemMap = new HashMap<>();
        loadFromPersistant();
    }

    public List<String> getDomains() {
        List<String> domains = new ArrayList<>();
        domains.addAll(domainHistoryItemMap.keySet());

        return domains;
    }

    public List<HistoryItem> getItems(String domains) {
        List<HistoryItem> returnValue = new ArrayList<>();
        if (StringUtils.isEmpty(domains)) {
            return returnValue;
        }

        List<HistoryItem> items = domainHistoryItemMap.get(domains);
        if (items != null && items.size() > 0) {
            returnValue.addAll(items);
        }

        return returnValue;
    }

    public void saveItem(HistoryItem item) {
        String webUrl = item.getWebUrl();
        if (StringUtils.isEmpty(webUrl)) {
            Log.e(TAG, "save HistoryItem with empty webUrl");
            return;
        }

        String domain = null;
        try {
            URL url = new URL(webUrl);
            domain = url.getHost();
        } catch (MalformedURLException e) {
            Log.e(TAG, "invalid url " + webUrl, e);
            return;
        }

        List<HistoryItem> historyItems = domainHistoryItemMap.get(domain);
        if (historyItems == null) {
            historyItems = new ArrayList<>();
            domainHistoryItemMap.put(domain, historyItems);
        }

        saveOrUpdateItem(item, historyItems);
        saveToPersistant();
    }

    private void saveOrUpdateItem(HistoryItem newItem, List<HistoryItem> historyItemList) {
        String webUrl = newItem.getWebUrl();

        Iterator<HistoryItem> historyItemIterator = historyItemList.iterator();
        while(historyItemIterator.hasNext()) {
            HistoryItem currentItem = historyItemIterator.next();
            if (webUrl.equals(currentItem.getWebUrl())) {
                historyItemIterator.remove();
            }
        }

        historyItemList.add(0, newItem);
    }

    private void loadFromPersistant() {
        clear();

        List<HistoryItem> items = persistant.load();
        for(HistoryItem item : items) {
            try {
                URL url = new URL(item.getWebUrl());
                List<HistoryItem> list = domainHistoryItemMap.get(url.getHost());
                if (list == null) {
                    list = new ArrayList<>();
                    domainHistoryItemMap.put(url.getHost(), list);
                }

                list.add(item);
            } catch (MalformedURLException e) {
                Log.w(TAG, "malformed web url", e);
            }
        }
    }

    private void saveToPersistant() {
        List<HistoryItem> items = new ArrayList<>();
        for(List<HistoryItem> current : domainHistoryItemMap.values()) {
            items.addAll(current);
        }

        persistant.save(items);
    }

    private void clear() {
        domainHistoryItemMap.clear();
    }
}
