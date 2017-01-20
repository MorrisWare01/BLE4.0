package com.win.ble_demo;

import android.app.Application;
import android.content.Context;

/**
 * 作者：Wilson on 2017-01-20 12:16
 * 邮箱：MorrisWare01@gmail.com
 */
public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getAppContext() {
        return mContext;
    }
}
