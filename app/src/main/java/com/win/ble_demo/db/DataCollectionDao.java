package com.win.ble_demo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.win.ble_demo.bean.DataCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：Wilson on 2017-01-21 09:01
 * 邮箱：MorrisWare01@gmail.com
 */
public class DataCollectionDao {

    private DataCollectionHelper mHelper;

    public DataCollectionDao(Context context) {
        mHelper = new DataCollectionHelper(context);
    }

    public void put(String type, String data, long date) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.query(DataCollectionHelper.TABLE_NAME, null,
                "type is ?", new String[]{type}, null, null, "time asc");

        ContentValues values = new ContentValues();
        values.put(DataCollectionHelper.TABLE_TYPE, type);
        values.put(DataCollectionHelper.TABLE_DATA, data);
        values.put(DataCollectionHelper.TABLE_TIME, date);
        if (cursor.getCount() >= 5) {
            cursor.moveToFirst();
            long time = cursor.getLong(cursor.getColumnIndex(DataCollectionHelper.TABLE_TIME));
            db.update(DataCollectionHelper.TABLE_NAME, values, "type is ? and time is ?",
                    new String[]{type, String.valueOf(time)});
        } else {
            db.insert(DataCollectionHelper.TABLE_NAME, null, values);
        }
        cursor.close();
        db.close();
    }

    public List<DataCollection> get(String type) {
        List<DataCollection> mList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(DataCollectionHelper.TABLE_NAME, null,
                "type is ?", new String[]{type}, null, null, "time desc");
        DataCollection collection;
        while (cursor.moveToNext()) {
            collection = new DataCollection();
            collection.setType(type);
            collection.setData(cursor.getString(cursor.getColumnIndex(DataCollectionHelper.TABLE_DATA)));
            collection.setTime(cursor.getLong(cursor.getColumnIndex(DataCollectionHelper.TABLE_TIME)));
            mList.add(collection);
        }
        cursor.close();
        db.close();
        return mList;
    }

    public void insert(DataCollection collection) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataCollectionHelper.TABLE_TYPE, collection.getType());
        values.put(DataCollectionHelper.TABLE_DATA, collection.getData());
        values.put(DataCollectionHelper.TABLE_TIME, collection.getTime());
        db.insert(DataCollectionHelper.TABLE_NAME, null, values);
    }


}
