/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.win.ble_demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.win.ble_demo.R;
import com.win.ble_demo.bean.DataCollection;
import com.win.ble_demo.bean.Frame;
import com.win.ble_demo.bean.ShakeHandFrame;
import com.win.ble_demo.bean.SpecifiedDeviceDataFrame;
import com.win.ble_demo.bean.SpecifiedDeviceStatusFrame;
import com.win.ble_demo.db.DataCollectionDao;
import com.win.ble_demo.server.BluetoothLeService;
import com.win.ble_demo.util.ToastUtil;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    @BindView(R.id.tv_device_name)
    TextView tv_device_name;
    @BindView(R.id.tv_device_address)
    TextView tv_device_address;

    @BindView(R.id.tv_data_kachi)
    TextView tv_data_kachi;
    @BindView(R.id.tv_data_qianfenchi)
    TextView tv_data_qianfenchi;
    @BindView(R.id.tv_data_baifenbiao)
    TextView tv_data_baifenbiao;
    @BindView(R.id.tv_data_niujubanshou)
    TextView tv_data_niujubanshou;
    @BindView(R.id.tv_data_shenduchi)
    TextView tv_data_shenduchi;
    @BindView(R.id.tv_data_lishiyingduji)
    TextView tv_data_lishiyingduji;

    @BindView(R.id.tv_status_kachi)
    TextView tv_status_kachi;
    @BindView(R.id.tv_status_qianfenchi)
    TextView tv_status_qianfenchi;
    @BindView(R.id.tv_status_baifenbiao)
    TextView tv_status_baifenbiao;
    @BindView(R.id.tv_status_niujubanshou)
    TextView tv_status_niujubanshou;
    @BindView(R.id.tv_status_shenduchi)
    TextView tv_status_shenduchi;
    @BindView(R.id.tv_status_lishiyingduji)
    TextView tv_status_lishiyingduji;

    @BindView(R.id.tv_debug)
    TextView tv_debug;

    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;
    private int mBleConnectStatus = BluetoothLeService.STATE_DISCONNECTED;
    private Handler mHandler = new WeakHandler(this);
    private DataCollectionDao mDao;

    private int cap;
    private int size;
    private byte[] frame;
    private int mWaitingId = -1;
    private boolean isWaiting = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mDao = new DataCollectionDao(this);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @OnClick({R.id.record_kachi, R.id.record_qianfengchi, R.id.record_baifenbiao, R.id.record_niujubanshou, R.id.record_shenduchi, R.id.record_lishiyingduji})
    public void onRecordClick(View view) {
        List<DataCollection> mList = null;
        switch (view.getId()) {
            case R.id.record_kachi:
                mList = mDao.get("数显卡尺");
                break;
            case R.id.record_qianfengchi:
                mList = mDao.get("数显千分尺");
                break;
            case R.id.record_baifenbiao:
                mList = mDao.get("数显百分表");
                break;
            case R.id.record_niujubanshou:
                mList = mDao.get("数显扭矩扳手");
                break;
            case R.id.record_shenduchi:
                mList = mDao.get("数显深度尺");
                break;
            case R.id.record_lishiyingduji:
                mList = mDao.get("里氏硬度计");
                break;
        }
        if (mList != null && mList.size() > 0) {
            showDialog(mList);
        } else {
            ToastUtil.getInstance().showToast("还没存入历史记录");
        }
    }

    private void showDialog(List<DataCollection> list) {
        StringBuilder sb = new StringBuilder();
        for (DataCollection collection : list) {
            sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(collection.getTime())))
                    .append("  ").append(collection.getData()).append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        new AlertDialog.Builder(this)
                .setTitle("历史记录--" + list.get(0).getType())
                .setMessage(sb.toString())
                .setCancelable(true)
                .setNeutralButton("返回", null)
                .show();
    }

    @OnClick({R.id.shake_hand, R.id.status_kachi, R.id.status_qianfengchi, R.id.status_baifenbiao, R.id.status_niujubanshou, R.id.status_shenduchi, R.id.status_lishiyingduji,
            R.id.data_kachi, R.id.data_qianfengchi, R.id.data_baifenbiao, R.id.data_niujubanshou, R.id.data_shenduchi, R.id.data_lishiyingduji})
    void onShakeHandClick(View view) {
        if (mBleConnectStatus != BluetoothLeService.STATE_DISCOVERED) {
            Log.e("TAG", "设备还没连接");
            return;
        }
        if (isWaiting) {
            ToastUtil.getInstance().showToast("请等待上一次操作完毕");
            return;
        }
        isWaiting = true;
        mWaitingId = view.getId();
        switch (view.getId()) {
            case R.id.shake_hand:
                mBluetoothLeService.shakeHand();
                break;
            case R.id.status_kachi:
                mBluetoothLeService.requestSpecifiedDeviceStatus("01000001");
                break;
            case R.id.status_qianfengchi:
                mBluetoothLeService.requestSpecifiedDeviceStatus("02000001");
                break;
            case R.id.status_baifenbiao:
                mBluetoothLeService.requestSpecifiedDeviceStatus("03000001");
                break;
            case R.id.status_niujubanshou:
                mBluetoothLeService.requestSpecifiedDeviceStatus("04000001");
                break;
            case R.id.status_shenduchi:
                mBluetoothLeService.requestSpecifiedDeviceStatus("05000001");
                break;
            case R.id.status_lishiyingduji:
                mBluetoothLeService.requestSpecifiedDeviceStatus("06000001");
                break;
            case R.id.data_kachi:
                mBluetoothLeService.requestSpecifiedDeviceData("01000001");
                break;
            case R.id.data_qianfengchi:
                mBluetoothLeService.requestSpecifiedDeviceData("02000001");
                break;
            case R.id.data_baifenbiao:
                mBluetoothLeService.requestSpecifiedDeviceData("03000001");
                break;
            case R.id.data_niujubanshou:
                mBluetoothLeService.requestSpecifiedDeviceData("04000001");
                break;
            case R.id.data_shenduchi:
                mBluetoothLeService.requestSpecifiedDeviceData("05000001");
                break;
            case R.id.data_lishiyingduji:
                mBluetoothLeService.requestSpecifiedDeviceData("06000001");
                break;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isWaiting) {
                    switch (mWaitingId) {
                        case R.id.shake_hand:
                            clearUI();
                            break;
                        case R.id.status_kachi:
                            showFailure(1);
                            break;
                        case R.id.status_qianfengchi:
                            showFailure(2);
                            break;
                        case R.id.status_baifenbiao:
                            showFailure(3);
                            break;
                        case R.id.status_shenduchi:
                            showFailure(4);
                            break;
                        case R.id.status_lishiyingduji:
                            showFailure(5);
                            break;
                        case R.id.status_niujubanshou:
                            showFailure(6);
                            break;
                        case R.id.data_kachi:
                            showFailure(1);
                            break;
                        case R.id.data_qianfengchi:
                            showFailure(2);
                            break;
                        case R.id.data_baifenbiao:
                            showFailure(3);
                            break;
                        case R.id.data_niujubanshou:
                            showFailure(4);
                            break;
                        case R.id.data_shenduchi:
                            showFailure(5);
                            break;
                        case R.id.data_lishiyingduji:
                            showFailure(6);
                            break;
                    }
                    ToastUtil.getInstance().showToast("操作超时");
                    isWaiting = false;
                }
            }
        }, 5000);
    }

    private void displayData(final byte[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (data == null) {
                    return;
                }
                final Frame frame = Frame.parseFrame(data);
                if (frame.getCmdType().equals(Frame.TYPE_SHAKE_HAND)) {
                    clearUI();
                    ShakeHandFrame shakeHandFrame = ShakeHandFrame.parse(frame.getOptionData());
                    tv_device_name.setText(shakeHandFrame.getSmdcName());
                    tv_device_address.setText(shakeHandFrame.getBleMac());
                } else if (frame.getCmdType().equals(Frame.TYPE_REQUEST_SPECIFIED_DEVICE_STATUS)) {
                    SpecifiedDeviceStatusFrame statusFrame = SpecifiedDeviceStatusFrame.parse(frame.getOptionData());
                    switch (statusFrame.getDeviceName()) {
                        case "数显卡尺":
                            if (statusFrame.getDeviceStatus().equals("在线")) {
                                tv_status_kachi.setTextColor(Color.parseColor("#00ff00"));
                            } else {
                                tv_status_kachi.setTextColor(Color.parseColor("#ff0000"));
                                tv_data_kachi.setText("0");
                            }
                            tv_status_kachi.setText(statusFrame.getDeviceStatus());
                            break;
                        case "数显千分尺":
                            if (statusFrame.getDeviceStatus().equals("在线")) {
                                tv_status_qianfenchi.setTextColor(Color.parseColor("#00ff00"));
                            } else {
                                tv_status_qianfenchi.setTextColor(Color.parseColor("#ff0000"));
                                tv_data_qianfenchi.setText("0");
                            }
                            tv_status_qianfenchi.setText(statusFrame.getDeviceStatus());
                            break;
                        case "数显百分表":
                            if (statusFrame.getDeviceStatus().equals("在线")) {
                                tv_status_baifenbiao.setTextColor(Color.parseColor("#00ff00"));
                            } else {
                                tv_status_baifenbiao.setTextColor(Color.parseColor("#ff0000"));
                                tv_data_baifenbiao.setText("0");
                            }
                            tv_status_baifenbiao.setText(statusFrame.getDeviceStatus());
                            break;
                        case "数显扭矩扳手":
                            if (statusFrame.getDeviceStatus().equals("在线")) {
                                tv_status_niujubanshou.setTextColor(Color.parseColor("#00ff00"));
                            } else {
                                tv_status_niujubanshou.setTextColor(Color.parseColor("#ff0000"));
                                tv_data_niujubanshou.setText("0");
                            }
                            tv_status_niujubanshou.setText(statusFrame.getDeviceStatus());
                            break;
                        case "数显深度尺":
                            if (statusFrame.getDeviceStatus().equals("在线")) {
                                tv_status_shenduchi.setTextColor(Color.parseColor("#00ff00"));
                            } else {
                                tv_status_shenduchi.setTextColor(Color.parseColor("#ff0000"));
                                tv_data_shenduchi.setText("0");
                            }
                            tv_status_shenduchi.setText(statusFrame.getDeviceStatus());
                            break;
                        case "里氏硬度计":
                            if (statusFrame.getDeviceStatus().equals("在线")) {
                                tv_status_lishiyingduji.setTextColor(Color.parseColor("#00ff00"));
                            } else {
                                tv_status_lishiyingduji.setTextColor(Color.parseColor("#ff0000"));
                                tv_data_lishiyingduji.setText("0");
                            }
                            tv_status_lishiyingduji.setText(statusFrame.getDeviceStatus());
                            break;
                    }
                } else if (frame.getCmdType().equals(Frame.TYPE_REQUEST_SPECIFIED_DEVICE_DATA)) {
                    SpecifiedDeviceDataFrame dataFrame = SpecifiedDeviceDataFrame.parse(frame.getOptionData());
                    switch (dataFrame.getDeviceName()) {
                        case "数显卡尺":
                            if (TextUtils.isEmpty(dataFrame.getDeviceData())) {
                                tv_status_kachi.setText("不在线");
                                tv_status_kachi.setTextColor(Color.parseColor("#ff0000"));
                                tv_data_kachi.setText("0");
                            } else {
                                tv_status_kachi.setText("在线");
                                tv_status_kachi.setTextColor(Color.parseColor("#00ff00"));
                                tv_data_kachi.setText(dataFrame.getDeviceData());
                                mDao.put("数显卡尺", dataFrame.getDeviceData(), System.currentTimeMillis());
                            }
                            break;
                        case "数显千分尺":
                            if (TextUtils.isEmpty(dataFrame.getDeviceData())) {
                                tv_status_qianfenchi.setText("不在线");
                                tv_status_qianfenchi.setTextColor(Color.parseColor("#ff0000"));
                                tv_data_qianfenchi.setText("0");
                            } else {
                                tv_status_qianfenchi.setText("在线");
                                tv_status_qianfenchi.setTextColor(Color.parseColor("#00ff00"));
                                tv_data_qianfenchi.setText(dataFrame.getDeviceData());
                                mDao.put("数显千分尺", dataFrame.getDeviceData(), System.currentTimeMillis());
                            }
                            break;
                        case "数显百分表":
                            if (TextUtils.isEmpty(dataFrame.getDeviceData())) {
                                tv_status_baifenbiao.setText("不在线");
                                tv_status_baifenbiao.setTextColor(Color.parseColor("#ff0000"));
                                tv_data_baifenbiao.setText("0");
                            } else {
                                tv_status_baifenbiao.setText("在线");
                                tv_status_baifenbiao.setTextColor(Color.parseColor("#00ff00"));
                                tv_data_baifenbiao.setText(dataFrame.getDeviceData());
                                mDao.put("数显百分表", dataFrame.getDeviceData(), System.currentTimeMillis());
                            }
                            break;
                        case "数显扭矩扳手":
                            if (TextUtils.isEmpty(dataFrame.getDeviceData())) {
                                tv_status_niujubanshou.setText("不在线");
                                tv_status_niujubanshou.setTextColor(Color.parseColor("#ff0000"));
                                tv_data_niujubanshou.setText("0");
                            } else {
                                tv_status_niujubanshou.setText("在线");
                                tv_status_niujubanshou.setTextColor(Color.parseColor("#00ff00"));
                                tv_data_niujubanshou.setText(dataFrame.getDeviceData());
                                mDao.put("数显扭矩扳手", dataFrame.getDeviceData(), System.currentTimeMillis());
                            }
                            break;
                        case "数显深度尺":
                            if (TextUtils.isEmpty(dataFrame.getDeviceData())) {
                                tv_status_shenduchi.setText("不在线");
                                tv_status_shenduchi.setTextColor(Color.parseColor("#ff0000"));
                                tv_data_shenduchi.setText("0");
                            } else {
                                tv_status_shenduchi.setText("在线");
                                tv_status_shenduchi.setTextColor(Color.parseColor("#00ff00"));
                                tv_data_shenduchi.setText(dataFrame.getDeviceData());
                                mDao.put("数显深度尺", dataFrame.getDeviceData(), System.currentTimeMillis());
                            }
                            break;
                        case "里氏硬度计":
                            if (TextUtils.isEmpty(dataFrame.getDeviceData())) {
                                tv_status_lishiyingduji.setText("不在线");
                                tv_status_lishiyingduji.setTextColor(Color.parseColor("#ff0000"));
                                tv_data_lishiyingduji.setText("0");
                            } else {
                                tv_status_lishiyingduji.setText("在线");
                                tv_status_lishiyingduji.setTextColor(Color.parseColor("#00ff00"));
                                tv_data_lishiyingduji.setText(dataFrame.getDeviceData());
                                mDao.put("里氏硬度计", dataFrame.getDeviceData(), System.currentTimeMillis());
                            }
                            break;
                    }
                }
                tv_debug.setText(frame.toDebug());
                mHandler.removeCallbacksAndMessages(null);
                isWaiting = false;
                ToastUtil.getInstance().showToast("操作成功");
            }
        });
    }

    private void clearUI() {
        tv_device_name.setText("");
        tv_device_address.setText("");
        showFailure(1);
        showFailure(2);
        showFailure(3);
        showFailure(4);
        showFailure(5);
        showFailure(6);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mBleConnectStatus == BluetoothLeService.STATE_DISCONNECTED) {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else if (mBleConnectStatus == BluetoothLeService.STATE_CONNECTING
                || mBleConnectStatus == BluetoothLeService.STATE_CONNECTED) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        } else if (mBleConnectStatus == BluetoothLeService.STATE_DISCOVERED) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBleConnectStatus = BluetoothLeService.STATE_CONNECTING;
                mBluetoothLeService.connect(mDeviceAddress);
                invalidateOptionsMenu();
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case R.id.menu_stop:
                mHandler.removeCallbacksAndMessages(null);
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFailure(int type) {
        switch (type) {
            case 1:
                tv_data_kachi.setText("0");
                tv_status_kachi.setText("不在线");
                tv_status_kachi.setTextColor(Color.parseColor("#ff0000"));
                break;
            case 2:
                tv_data_qianfenchi.setText("0");
                tv_status_qianfenchi.setText("不在线");
                tv_status_qianfenchi.setTextColor(Color.parseColor("#ff0000"));
                break;
            case 3:
                tv_data_baifenbiao.setText("0");
                tv_status_baifenbiao.setText("不在线");
                tv_status_baifenbiao.setTextColor(Color.parseColor("#ff0000"));
                break;
            case 4:
                tv_data_niujubanshou.setText("0");
                tv_status_niujubanshou.setText("不在线");
                tv_status_niujubanshou.setTextColor(Color.parseColor("#ff0000"));
                break;
            case 5:
                tv_data_shenduchi.setText("0");
                tv_status_shenduchi.setText("不在线");
                tv_status_shenduchi.setTextColor(Color.parseColor("#ff0000"));
                break;
            case 6:
                tv_data_lishiyingduji.setText("0");
                tv_status_lishiyingduji.setText("不在线");
                tv_status_lishiyingduji.setTextColor(Color.parseColor("#ff0000"));
                break;
        }
        tv_debug.setText("");
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mBleConnectStatus = BluetoothLeService.STATE_CONNECTED;
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mBleConnectStatus = BluetoothLeService.STATE_DISCONNECTED;
                clearUI();
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if (mBluetoothLeService.getSupportedGattServices() == null) return;
                mBluetoothLeService.enable_JDY_ble(true);
                SystemClock.sleep(100);
                mBleConnectStatus = BluetoothLeService.STATE_DISCOVERED;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (isWaiting) {
                    byte[] bytes = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    if (bytes[0] == 0x7E && cap == 0) {
                        cap = bytes[1] * 256 + bytes[2];
                        frame = new byte[cap];
                        System.arraycopy(bytes, 0, frame, size, bytes.length);
                        size = bytes.length;
                    } else {
                        System.arraycopy(bytes, 0, frame, size, bytes.length);
                        size += bytes.length;
                    }
                    if (size >= cap) {
                        displayData(frame);
                        size = 0;
                        cap = 0;
                    }
                }
            }
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private static class WeakHandler extends Handler {

        private WeakReference<Activity> reference;

        public WeakHandler(Activity activity) {
            reference = new WeakReference<Activity>(activity);
        }
    }

}
