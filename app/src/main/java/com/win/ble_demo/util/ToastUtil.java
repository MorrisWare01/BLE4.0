package com.win.ble_demo.util;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.win.ble_demo.App;

/**
 * 作者：Wilson on 2017-01-20 12:14
 * 邮箱：MorrisWare01@gmail.com
 */
public class ToastUtil {
    private static volatile ToastUtil sToastUtil = null;

    private Toast mToast = null;

    private Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 获取实例
     *
     * @return
     */
    public static ToastUtil getInstance() {
        if (sToastUtil == null) {
            synchronized (ToastUtil.class) {
                if (sToastUtil == null) {
                    sToastUtil = new ToastUtil();
                }
            }
        }
        return sToastUtil;
    }


    /**
     * 显示Toast，多次调用此函数时，Toast显示的时间不会累计，并且显示内容为最后一次调用时传入的内容
     * 持续时间默认为short
     *
     * @param tips 要显示的内容
     *             {@link Toast#LENGTH_LONG}
     */
    public void showToast(final String tips) {
        showToast(tips, Toast.LENGTH_SHORT);
    }

    public void showToast(final int tips) {
        showToast(tips, Toast.LENGTH_SHORT);
    }

    /**
     * 显示Toast，多次调用此函数时，Toast显示的时间不会累计，并且显示内容为最后一次调用时传入的内容
     *
     * @param tips     要显示的内容
     * @param duration 持续时间，参见{@link Toast#LENGTH_SHORT}和
     *                 {@link Toast#LENGTH_LONG}
     */
    public void showToast(final String tips, final int duration) {
        if (android.text.TextUtils.isEmpty(tips)) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(App.getAppContext(), tips, duration);
                    mToast.show();
                } else {
                    //mToast.cancel();
                    //mToast.setView(mToast.getView());
                    mToast.setText(tips);
                    mToast.setDuration(duration);
                    mToast.show();
                }
            }
        });
    }

    public void showToast(final int tips, final int duration) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(App.getAppContext(), tips, duration);
                    mToast.show();
                } else {
                    //mToast.cancel();
                    //mToast.setView(mToast.getView());
                    mToast.setText(tips);
                    mToast.setDuration(duration);
                    mToast.show();
                }
            }
        });
    }
}
