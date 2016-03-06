package com.sysdbg.caster.history.persistant;

import android.content.Context;
import android.util.Log;

import com.sysdbg.caster.history.HistoryItem;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by crady on 3/5/2016.
 */
public class JosnFilePersistant implements Persistant {
    private static final String TAG  = JosnFilePersistant.class.getSimpleName();
    private static final String HISTORY_FILE_NAME = "History.dat";
    private static final String HISTORY_FILE_ENCODING = "UTF-8";
    private static final String HISTORY = "History";

    private Context context;

    public JosnFilePersistant(Context context) {
        this.context = context;
    }

    @Override
    public void save(Collection<HistoryItem> items) {
        String content = null;
        try {
            JSONObject base = new JSONObject();
            JSONArray history = new JSONArray();
            base.put(HISTORY, history);

            for(HistoryItem item : items) {
                JSONObject itemJson = item.toJosn();
                if (itemJson == null) {
                    continue;
                }

                history.put(itemJson);
            }

            content = base.toString();
            saveHistoryFile(content);
        }
        catch (JSONException e) {
            Log.e(TAG, "save history data fail", e);
        }
    }

    @Override
    public List<HistoryItem> load() {
        List<HistoryItem> items = new ArrayList<>();
        String content = readHistoryFileAsString();
        if (StringUtils.isEmpty(content)) {
            return items;
        }

        try {
            JSONObject json = new JSONObject(content);
            JSONArray history = json.getJSONArray(HISTORY);

            for(int i = 0; i < history.length(); i++) {
                JSONObject historyItem = history.getJSONObject(i);
                HistoryItem item = HistoryItem.fromJosn(historyItem);
                items.add(item);
            }
        } catch (JSONException e) {
            Log.e(TAG, "parse history data fail", e);
        }

        return items;
    }

    private String readHistoryFileAsString() {
        Reader reader = null;
        try {
            reader  = new InputStreamReader(
                    context.openFileInput(HISTORY_FILE_NAME),
                    HISTORY_FILE_ENCODING);
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
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }

    private void saveHistoryFile(String content) {
        if (content == null) {
            content = "";
        }

        Writer writer = null;
        try {
            writer = new OutputStreamWriter(
                    context.openFileOutput(HISTORY_FILE_NAME, Context.MODE_PRIVATE),
                    HISTORY_FILE_ENCODING);
            writer.write(content);
        } catch (Exception e) {
            Log.e(TAG, "save history.dat fail", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
