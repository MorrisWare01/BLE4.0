package com.win.ble_demo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 作者：Wilson on 2017-01-21 08:52
 * 邮箱：MorrisWare01@gmail.com
 */
public class DataCollectionHelper extends SQLiteOpenHelper {

    public final static String TABLE_NAME = "data";
    public final static String TABLE_ID = "_id";
    public final static String TABLE_TIME = "time";
    public final static String TABLE_DATA = "data";
    public final static String TABLE_TYPE = "type";

    public DataCollectionHelper(Context context) {
        super(context, "data.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_NAME + "(" +
                TABLE_ID + " integer primary key autoincrement," +
                TABLE_TYPE + " text," +
                TABLE_DATA + " text," +
                TABLE_TIME + " long)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
