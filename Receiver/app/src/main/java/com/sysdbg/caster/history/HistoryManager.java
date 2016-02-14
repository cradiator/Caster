package com.sysdbg.caster.history;

import android.content.Context;
import android.util.Log;

import com.sysdbg.caster.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
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
    private static final String HISTORY_FILE_NAME = "History.dat";
    private static final String HISTORY_FILE_ENCODING = "UTF-8";
    private static final String HISTORY = "History";

    private static HistoryManager instance;

    private Context context;
    private Map<String, List<HistoryItem>> domainHistoryItemMap;

    public static HistoryManager getInstance(Context context) {
        if (instance != null) {
            return instance;
        }

        instance = new HistoryManager(context);
        return instance;
    }

    public HistoryManager(Context context) {
        this.context = context;
        domainHistoryItemMap = new HashMap<>();

        load();
    }

    public List<String> getDomains() {
        List<String> domains = new ArrayList<String>();
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
        save();
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

    private void load() {
        clear();

        String content = readHistoryFileAsString();
        if (StringUtils.isEmpty(content)) {
            return;
        }

        try {
            JSONObject json = new JSONObject(content);
            JSONObject history = json.getJSONObject(HISTORY);

            Iterator<String> domainIt = history.keys();
            while (domainIt.hasNext()) {
                String domain = domainIt.next();
                JSONArray historyItems = history.getJSONArray(domain);

                List<HistoryItem> items = new ArrayList<>();
                for(int i = 0; i < historyItems.length(); i++) {
                    JSONObject historyItem = historyItems.getJSONObject(i);
                    HistoryItem item = HistoryItem.fromJosn(historyItem);

                    items.add(item);
                }

                if (items.size() > 0) {
                    domainHistoryItemMap.put(domain, items);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "parse history data fail", e);
            clear();
        }
    }

    private void save() {
        String content = null;
        try {
            JSONObject base = new JSONObject();
            JSONObject history = new JSONObject();
            base.put(HISTORY, history);

            for(String domain : domainHistoryItemMap.keySet()) {
                JSONArray items = new JSONArray();

                for(HistoryItem item : domainHistoryItemMap.get(domain)) {
                    JSONObject itemJson = item.toJosn();
                    if (itemJson == null) {
                        continue;
                    }

                    items.put(itemJson);
                }

                if (items.length() > 0) {
                    history.put(domain, items);
                }
            }

            content = base.toString();
            saveHistoryFile(content);
        }
        catch (JSONException e) {
            Log.e(TAG, "save history data fail", e);
        }
    }

    private void clear() {
        domainHistoryItemMap.clear();
    }

    private String readHistoryFileAsString() {
        try(Reader reader  = new InputStreamReader(
                context.openFileInput(HISTORY_FILE_NAME),
                HISTORY_FILE_ENCODING)) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            for(;;) {
                int size = reader.read(buffer);
                if  (size < 0) {
                    break;
                }

                sb.append(buffer, 0, size);
            }

            return sb.toString();
        } catch (FileNotFoundException e) {
            Log.i(TAG, "history file not found, use empty");
        } catch (IOException e) {
            Log.e(TAG, "open history file fail", e);
        }

        return null;
    }

    private void saveHistoryFile(String content) {
        if (content == null) {
            content = "";
        }

        try(Writer writer = new OutputStreamWriter(
                context.openFileOutput(HISTORY_FILE_NAME, Context.MODE_PRIVATE),
                HISTORY_FILE_ENCODING)) {
            writer.write(content);
        } catch (Exception e) {
            Log.e(TAG, "save history.dat fail", e);
        }
    }
}
