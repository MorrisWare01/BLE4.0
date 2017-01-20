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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.win.ble_demo.R;
import com.win.ble_demo.bean.Frame;
import com.win.ble_demo.bean.ShakeHandFrame;
import com.win.ble_demo.bean.SpecifiedDeviceDataFrame;
import com.win.ble_demo.bean.SpecifiedDeviceStatusFrame;
import com.win.ble_demo.util.BluetoothLeClass;
import com.win.ble_demo.util.ToastUtil;

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

    @BindView(R.id.tv_data1)
    TextView tv_data1;
    @BindView(R.id.tv_data2)
    TextView tv_data2;
    @BindView(R.id.tv_data3)
    TextView tv_data3;
    @BindView(R.id.tv_data4)
    TextView tv_data4;

    @BindView(R.id.tv_status1)
    TextView tv_status1;
    @BindView(R.id.tv_status2)
    TextView tv_status2;
    @BindView(R.id.tv_status3)
    TextView tv_status3;
    @BindView(R.id.tv_status4)
    TextView tv_status4;

    @BindView(R.id.tv_debug)
    TextView tv_debug;

    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;
    private BluetoothLeClass mBLE;
    private int size;
    private byte[] frame;
    private int cap;
    private int mWaitingId = -1;
    private boolean isWaiting = false;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        mBLE = new BluetoothLeClass(this);
        if (!mBLE.initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }

        mBLE.setOnServiceDiscoverListener(new BluetoothLeClass.OnServiceDiscoverListener() {
            @Override
            public void onServiceDiscover(BluetoothGatt gatt) {
                displayGattServices(mBLE.getSupportedGattServices());
            }
        });
        mBLE.setOnDataAvailableListener(new BluetoothLeClass.OnDataAvailableListener() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//                String value = new String(characteristic.getValue());
                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    Log.e(TAG, "onCharRead " + gatt.getDevice().getName()
//                            + " read "
//                            + characteristic.getUuid().toString()
//                            + " -> "
//                            + value);
                    displayData(characteristic.getValue());
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//                Log.e(TAG, "onCharWrite " + gatt.getDevice().getName()
//                        + " write "
//                        + characteristic.getUuid().toString()
//                        + " -> "
//                        + Utils.bytes2HexString(characteristic.getValue()));
                if (isWaiting) {
                    byte[] bytes = characteristic.getValue();
                    if (bytes[0] == 0x7E && cap == 0) {
                        cap = bytes[1] * 256 + bytes[2];
                        frame = new byte[cap];
                        System.arraycopy(bytes, 0, frame, size, bytes.length);
                        size = bytes.length;
//                    Log.e("TAG", size + "/" + cap);
                    } else {
                        System.arraycopy(bytes, 0, frame, size, bytes.length);
                        size += bytes.length;
//                    Log.e("TAG", size + "/" + cap);
                    }
                    if (size >= cap) {
                        displayData(frame);
                        size = 0;
                        cap = 0;
                    }
                }
            }
        });
        mBLE.setOnConnectListener(new BluetoothLeClass.OnConnectListener() {
            @Override
            public void onConnect(BluetoothGatt gatt) {
                mConnected = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearUI();
                    }
                });
                invalidateOptionsMenu();
            }
        });
        mBLE.setOnDisconnectListener(new BluetoothLeClass.OnDisconnectListener() {
            @Override
            public void onDisconnect(BluetoothGatt gatt) {
                mConnected = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearUI();
                    }
                });
                invalidateOptionsMenu();
            }
        });
    }


    @OnClick({R.id.shake_hand, R.id.status_1, R.id.status_2, R.id.status_3, R.id.status_4,
            R.id.data_1, R.id.data_2, R.id.data_3, R.id.data_4})
    void onShakeHandClick(View view) {
        if (!mConnected) {
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
                mBLE.shakeHand();
                break;
            case R.id.status_1:
                mBLE.requestSpecifiedDeviceStatus("01000001");
                break;
            case R.id.status_2:
                mBLE.requestSpecifiedDeviceStatus("02000001");
                break;
            case R.id.status_3:
                mBLE.requestSpecifiedDeviceStatus("03000001");
                break;
            case R.id.status_4:
                mBLE.requestSpecifiedDeviceStatus("04000001");
                break;
            case R.id.data_1:
                mBLE.requestSpecifiedDeviceData("01000001");
                break;
            case R.id.data_2:
                mBLE.requestSpecifiedDeviceData("02000001");
                break;
            case R.id.data_3:
                mBLE.requestSpecifiedDeviceData("03000001");
                break;
            case R.id.data_4:
                mBLE.requestSpecifiedDeviceData("04000001");
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
                        case R.id.status_1:
                            showFailure(1);
                            break;
                        case R.id.status_2:
                            showFailure(2);
                            break;
                        case R.id.status_3:
                            showFailure(3);
                            break;
                        case R.id.status_4:
                            showFailure(4);
                            break;
                        case R.id.data_1:
                            showFailure(1);
                            break;
                        case R.id.data_2:
                            showFailure(2);
                            break;
                        case R.id.data_3:
                            showFailure(3);
                            break;
                        case R.id.data_4:
                            showFailure(4);
                            break;
                    }
                    ToastUtil.getInstance().showToast("操作超时");
                    isWaiting = false;
                }
            }
        }, 5000);
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        mBLE.enable_JDY_ble(true);
        try {
            Thread.currentThread();
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBLE.enable_JDY_ble(true);
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
                                tv_status1.setTextColor(Color.parseColor("#00ff00"));
                            } else {
                                tv_status1.setTextColor(Color.parseColor("#ff0000"));
                                tv_data1.setText("0");
                            }
                            tv_status1.setText(statusFrame.getDeviceStatus());
                            break;
                        case "数显千分尺":
                            if (statusFrame.getDeviceStatus().equals("在线")) {
                                tv_status2.setTextColor(Color.parseColor("#00ff00"));
                            } else {
                                tv_status2.setTextColor(Color.parseColor("#ff0000"));
                                tv_data2.setText("0");
                            }
                            tv_status2.setText(statusFrame.getDeviceStatus());
                            break;
                        case "数显百分表":
                            if (statusFrame.getDeviceStatus().equals("在线")) {
                                tv_status3.setTextColor(Color.parseColor("#00ff00"));
                            } else {
                                tv_status3.setTextColor(Color.parseColor("#ff0000"));
                                tv_data3.setText("0");
                            }
                            tv_status3.setText(statusFrame.getDeviceStatus());
                            break;
                        case "数显扭矩扳手":
                            if (statusFrame.getDeviceStatus().equals("在线")) {
                                tv_status4.setTextColor(Color.parseColor("#00ff00"));
                            } else {
                                tv_status4.setTextColor(Color.parseColor("#ff0000"));
                                tv_data4.setText("0");
                            }
                            tv_status4.setText(statusFrame.getDeviceStatus());
                            break;
                    }
                } else if (frame.getCmdType().equals(Frame.TYPE_REQUEST_SPECIFIED_DEVICE_DATA)) {
                    SpecifiedDeviceDataFrame dataFrame = SpecifiedDeviceDataFrame.parse(frame.getOptionData());
                    switch (dataFrame.getDeviceName()) {
                        case "数显卡尺":
                            if (TextUtils.isEmpty(dataFrame.getDeviceData())) {
                                tv_status1.setText("不在线");
                                tv_status1.setTextColor(Color.parseColor("#ff0000"));
                                tv_data1.setText("0");
                            } else {
                                tv_status1.setText("在线");
                                tv_status1.setTextColor(Color.parseColor("#00ff00"));
                                tv_data1.setText(dataFrame.getDeviceData());
                            }
                            break;
                        case "数显千分尺":
                            if (TextUtils.isEmpty(dataFrame.getDeviceData())) {
                                tv_status2.setText("不在线");
                                tv_status2.setTextColor(Color.parseColor("#ff0000"));
                                tv_data2.setText("0");
                            } else {
                                tv_status2.setText("在线");
                                tv_status2.setTextColor(Color.parseColor("#00ff00"));
                                tv_data2.setText(dataFrame.getDeviceData());
                            }
                            break;
                        case "数显百分表":
                            if (TextUtils.isEmpty(dataFrame.getDeviceData())) {
                                tv_status3.setText("不在线");
                                tv_status3.setTextColor(Color.parseColor("#ff0000"));
                                tv_data3.setText("0");
                            } else {
                                tv_status3.setText("在线");
                                tv_status3.setTextColor(Color.parseColor("#00ff00"));
                                tv_data3.setText(dataFrame.getDeviceData());
                            }
                            break;
                        case "数显扭矩扳手":
                            if (TextUtils.isEmpty(dataFrame.getDeviceData())) {
                                tv_status4.setText("不在线");
                                tv_status4.setTextColor(Color.parseColor("#ff0000"));
                                tv_data4.setText("0");
                            } else {
                                tv_status4.setText("在线");
                                tv_status4.setTextColor(Color.parseColor("#00ff00"));
                                tv_data4.setText(dataFrame.getDeviceData());
                            }
                            break;
                    }
                }
                tv_debug.setText(frame.toDebug());
                isWaiting = false;
                mHandler.removeCallbacksAndMessages(null);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBLE.connect(mDeviceAddress);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBLE.disconnect();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBLE.close();
        mBLE = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBLE.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBLE.disconnect();
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
                tv_data1.setText("0");
                tv_status1.setText("不在线");
                tv_status1.setTextColor(Color.parseColor("#ff0000"));
                break;
            case 2:
                tv_data2.setText("0");
                tv_status2.setText("不在线");
                tv_status2.setTextColor(Color.parseColor("#ff0000"));
                break;
            case 3:
                tv_data3.setText("0");
                tv_status3.setText("不在线");
                tv_status3.setTextColor(Color.parseColor("#ff0000"));
                break;
            case 4:
                tv_data4.setText("0");
                tv_status4.setText("不在线");
                tv_status4.setTextColor(Color.parseColor("#ff0000"));
                break;
        }
        tv_debug.setText("");
    }

}
