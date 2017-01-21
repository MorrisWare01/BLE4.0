package com.win.ble_demo;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;

/**
 * 作者：Wilson on 2017-01-20 12:16
 * 邮箱：MorrisWare01@gmail.com
 */
public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        mContext = this;
    }

    public static Context getAppContext() {
        return mContext;
    }
}
