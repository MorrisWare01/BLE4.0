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
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.win.ble_demo.util.BluetoothLeClass;
import com.win.ble_demo.R;
import com.win.ble_demo.util.Utils;

import java.util.ArrayList;
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
public class DeviceControlActivity extends AppCompatActivity {

    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    private final static String UUID_SERVICE = "00001802-0000-1000-8000-00805f9b34fb";
    private final static String UUID_KEY_DATA = "afdf39cc-e249-4397-b342-295408ac33bc";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    @BindView(R.id.device_name)
    TextView tv_name;

    @BindView(R.id.device_address)
    TextView tv_address;

    @BindView(R.id.connection_state)
    TextView tv_state;

    //    @BindView(R.id.data_value)
    TextView tv_data;

    @BindView(R.id.et_text)
    EditText et_text;

    @BindView(R.id.list_data)
    ListView mListView;

    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;
    private BluetoothLeClass mBLE;
    private Handler mHandler = new Handler();

    private List<String> mList = new ArrayList<>(0);
    private MyAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        tv_name.setText(mDeviceName);
        tv_address.setText(mDeviceAddress);

        mBLE = new BluetoothLeClass(this);
        if (!mBLE.initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
        mBLE.setOnDataAvailableListener(mOnDataAvailable);
        mBLE.setOnConnectListener(new BluetoothLeClass.OnConnectListener() {
            @Override
            public void onConnect(BluetoothGatt gatt) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            }
        });
        mBLE.setOnDisconnectListener(new BluetoothLeClass.OnDisconnectListener() {
            @Override
            public void onDisconnect(BluetoothGatt gatt) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            }
        });

        mAdapter = new MyAdapter();
        mListView.setAdapter(mAdapter);

    }

    @OnClick(R.id.btn_send)
    void onSendClick() {
        if (!mConnected) {
            Toast.makeText(this, "设备没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
        mBLE.shakeHand();
//        final BluetoothGattCharacteristic mGattChar = mBLE.getTxGattChar();
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mBLE.readCharacteristic(mGattChar);
//            }
//        }, 500);
//        mGattChar.setValue(Utils.getHexBytes(et_text.getText().toString()));
//        mBLE.writeCharacteristic(mGattChar);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_state.setText(resourceId);
            }
        });
    }

    private void clearUI() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                tv_data.setText(R.string.no_data);
//            }
//        });
    }

    private void displayData(final byte[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (data != null) {
                    mList.add(0, Utils.bytes2HexString(data));
                    mAdapter.notifyDataSetChanged();
//                    tv_data.setText(Utils.bytes2HexString(data));
                }
            }
        });

    }

    private void displayLog(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (data != null) {
                    mList.add(0, data);
                    mAdapter.notifyDataSetChanged();
//                    tv_data.setText(Utils.bytes2HexString(data));
                }
            }
        });
    }

    /**
     * 搜索到BLE终端服务的事件
     */
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover =
            new BluetoothLeClass.OnServiceDiscoverListener() {

                @Override
                public void onServiceDiscover(BluetoothGatt gatt) {
                    displayGattServices(mBLE.getSupportedGattServices());
                }
            };

    /**
     * 收到BLE终端数据交互的事件
     */
    private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable =
            new BluetoothLeClass.OnDataAvailableListener() {

                /**
                 * BLE终端数据被读的事件 手机向外围设备写入数据
                 */
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic, int status) {
                    String value = new String(characteristic.getValue());
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.e(TAG, "onCharRead " + gatt.getDevice().getName()
                                + " read "
                                + characteristic.getUuid().toString()
                                + " -> "
                                + value);
                        displayData(characteristic.getValue());
                    }
                }

                /**
                 * 收到BLE终端写入数据回调 外围数据向手机写入数据
                 */
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic) {
//                    Log.e(TAG, "onCharWrite " + gatt.getDevice().getName()
//                            + " write "
//                            + characteristic.getUuid().toString()
//                            + " -> "
//                            + Utils.bytesToHexString(characteristic.getValue()));

                    byte[] bytes = characteristic.getValue();
                    if (bytes[0] == 0x7E || size == 0 || cap == 0) {
                        cap = bytes[1] * 256 + bytes[2];
                        frame = new byte[cap];
                        System.arraycopy(bytes, 0, frame, size, bytes.length);
                        size = bytes.length;
//                        displayLog(size + "-" + cap + ";" + Arrays.toString(frame));
//                        Log.e("TAG", size + ";" + cap + ";" + Arrays.toString(frame));
                    } else {
                        System.arraycopy(bytes, 0, frame, size, bytes.length);
                        size += bytes.length;
//                        Log.e("TAG", size + ";" + cap + ";" + Arrays.toString(frame));
//                        displayLog(size + "-" + cap + ";" + Arrays.toString(frame));
                        if (size >= cap) {
                            displayData(frame);
                            size = 0;
                            cap = 0;
                        }
                    }
                }
            };

    private int size;
    private byte[] frame;
    private int cap;

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

//        for (BluetoothGattService gattService : gattServices) {
//            //-----Service的字段信息-----//
//            int type = gattService.getType();
//            Log.e(TAG, "-->service type:" + Utils.getServiceType(type));
//            Log.e(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
//            Log.e(TAG, "-->service uuid:" + gattService.getUuid());
//
//            //-----Characteristics的字段信息-----//
//            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
//            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());
//
//                int permission = gattCharacteristic.getPermissions();
//                Log.e(TAG, "---->char permission:" + Utils.getCharPermission(permission));
//
//                int property = gattCharacteristic.getProperties();
//                Log.e(TAG, "---->char property:" + Utils.getCharPropertie(property));
//
//                byte[] data = gattCharacteristic.getValue();
//                if (data != null && data.length > 0) {
//                    Log.e(TAG, "---->char value:" + new String(data));
//                }
//                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)) {
//                    mGattChar = gattCharacteristic;
//                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
//                    mBLE.setCharacteristicNotification(mGattChar, true);
//                }
////                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
////                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)) {
////                    //测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
////                    mHandler.postDelayed(new Runnable() {
////                        @Override
////                        public void run() {
////                            mBLE.readCharacteristic(gattCharacteristic);
////                        }
////                    }, 500);
////
////                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
////                    mBLE.setCharacteristicNotification(gattCharacteristic, true);
////                    //设置数据内容
////                    gattCharacteristic.setValue("send data->");
////                    //往蓝牙模块写入数据
////                    mBLE.writeCharacteristic(gattCharacteristic);
////                }
//
//                //-----Descriptors的字段信息-----//
//                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
//                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
//                    Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
//                    int descPermission = gattDescriptor.getPermissions();
//                    Log.e(TAG, "-------->desc permission:" + Utils.getDescPermission(descPermission));
//
//                    byte[] desData = gattDescriptor.getValue();
//                    if (desData != null && desData.length > 0) {
//                        Log.e(TAG, "-------->desc value:" + new String(desData));
//                    }
//                }
//            }
//        }//

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


    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View view1 = LayoutInflater.from(DeviceControlActivity.this).
                    inflate(R.layout.list_text, viewGroup, false);
            TextView textView = (TextView) view1.findViewById(R.id.tv_data);
            textView.setText(mList.get(i));
            return view1;
        }
    }
}
