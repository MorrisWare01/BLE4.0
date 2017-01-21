package com.win.ble_demo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.win.ble_demo.db.DataCollectionDao;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        DataCollectionDao dao = new DataCollectionDao(appContext);
        dao.put("卡尺", "1.1", System.currentTimeMillis());
        dao.put("卡尺", "1.2", System.currentTimeMillis());
        dao.put("卡尺", "1.3", System.currentTimeMillis());
        dao.put("卡尺", "1.4", System.currentTimeMillis());
        dao.put("卡尺", "1.5", System.currentTimeMillis());
        dao.put("卡尺", "1.7", System.currentTimeMillis());
        dao.put("百分表", "1.8", System.currentTimeMillis());
        dao.put("百分表", "1.9", System.currentTimeMillis());
        dao.put("百分表", "2.0", System.currentTimeMillis());
        dao.get("百分表");
        dao.get("卡尺");
//        Log.e("TAG", collections.toString());
    }
}
