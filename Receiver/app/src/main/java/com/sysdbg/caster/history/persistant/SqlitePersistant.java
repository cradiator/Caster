package com.sysdbg.caster.history.persistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sysdbg.caster.history.HistoryItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by crady on 3/5/2016.
 */
public class SqlitePersistant implements Persistant {
    private static final String DBNAME = "History";
    private Context context;
    private SqliteHelper sqliteHelper;

    public SqlitePersistant(Context context) {
        this.context = context;
        sqliteHelper = new SqliteHelper(context);
    }

    @Override
    public void save(Collection<HistoryItem> items) {
        SQLiteDatabase db = sqliteHelper.getWritableDatabase();
        for(HistoryItem item : items) {
            ContentValues values = new ContentValues();
            values.put("weburl", item.getWebUrl());
            values.put("content", item.toJosn().toString());

            long result = db.insert("HistoryItems", null, values);
            if (result == -1) {
                db.update("HistoryItems", values, "weburl = ?", new String[] {item.getWebUrl()});
            }
        }
    }

    @Override
    public List<HistoryItem> load() {
        List<HistoryItem> items = new ArrayList<>();

        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        Cursor cursor = db.query("HistoryItems",
                new String[] {"weburl", "content"},
                null,
                null,
                null,
                null,
                null,
                null);
        if (cursor == null) {
            return items;
        }

        if (cursor.moveToFirst()) {
            do {
                String content = cursor.getString(1);
                try {
                    HistoryItem item = HistoryItem.fromJosn(new JSONObject(content));
                    items.add(item);
                } catch (JSONException e) {
                }
            } while(cursor.moveToNext());
        }

        return items;
    }

    private static class SqliteHelper extends SQLiteOpenHelper {
        public SqliteHelper(Context context) {
            super(context, SqlitePersistant.DBNAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_BOOK_TABLE = "CREATE TABLE HistoryItems ( " +
                    "weburl TEXT PRIMARY KEY, "+
                    "content TEXT )";

            db.execSQL(CREATE_BOOK_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS HistoryItems");
            onCreate(db);
        }
    }
}
