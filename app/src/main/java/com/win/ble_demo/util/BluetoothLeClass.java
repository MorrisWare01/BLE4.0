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

package com.win.ble_demo.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.win.ble_demo.bean.Frame;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeClass {
    private final static String TAG = BluetoothLeClass.class.getSimpleName();
    public static String Service_uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String Characteristic_uuid_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String Characteristic_uuid_FUNCTION = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    public interface OnConnectListener {
        void onConnect(BluetoothGatt gatt);
    }

    public interface OnDisconnectListener {
        void onDisconnect(BluetoothGatt gatt);
    }

    public interface OnServiceDiscoverListener {
        void onServiceDiscover(BluetoothGatt gatt);
    }

    public interface OnDataAvailableListener {
        void onCharacteristicRead(BluetoothGatt gatt,
                                  BluetoothGattCharacteristic characteristic,
                                  int status);

        void onCharacteristicWrite(BluetoothGatt gatt,
                                   BluetoothGattCharacteristic characteristic);
    }

    private OnConnectListener mOnConnectListener;
    private OnDisconnectListener mOnDisconnectListener;
    private OnServiceDiscoverListener mOnServiceDiscoverListener;
    private OnDataAvailableListener mOnDataAvailableListener;
    private Context mContext;

    public void setOnConnectListener(OnConnectListener l) {
        mOnConnectListener = l;
    }

    public void setOnDisconnectListener(OnDisconnectListener l) {
        mOnDisconnectListener = l;
    }

    public void setOnServiceDiscoverListener(OnServiceDiscoverListener l) {
        mOnServiceDiscoverListener = l;
    }

    public void setOnDataAvailableListener(OnDataAvailableListener l) {
        mOnDataAvailableListener = l;
    }

    public BluetoothLeClass(Context c) {
        mContext = c;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (mOnConnectListener != null)
                    mOnConnectListener.onConnect(gatt);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (mOnDisconnectListener != null)
                    mOnDisconnectListener.onDisconnect(gatt);
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS && mOnServiceDiscoverListener != null) {
                mOnServiceDiscoverListener.onServiceDiscover(gatt);
            } else {
                Log.e(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (mOnDataAvailableListener != null)
                mOnDataAvailableListener.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if (mOnDataAvailableListener != null)
                mOnDataAvailableListener.onCharacteristicWrite(gatt, characteristic);
        }
    };

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.e(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                return true;
//            } else {
//                return false;
//            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "Device not found.  Unable to connect.");
            return false;
        }
//        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//            try {
//                Log.e(TAG, "NOT BOND_BONDED");
//                BLEUtils.setPin(device.getClass(), device, "123456"); // 手机和蓝牙采集器配对
//                BLEUtils.createBond(device.getClass(), device);
//            } catch (Exception e) {
//                Log.e(TAG, "setPiN failed!");
//                e.printStackTrace();
//            }
//        } else {
//            Log.e(TAG, "HAS BOND_BONDED");
//            try {
//                BLEUtils.createBond(device.getClass(), device);
//                BLEUtils.setPin(device.getClass(), device, "123456"); // 手机和蓝牙采集器配对
//                BLEUtils.createBond(device.getClass(), device);
//            } catch (Exception e) {
//                Log.e(TAG, "setPiN failed!");
//                e.printStackTrace();
//            }
//        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        Log.e(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void writeCharacteristic(String server_uuid, String char_uuid, String data) {
        BluetoothGattCharacteristic gg = mBluetoothGatt.getService(UUID.fromString(server_uuid))
                .getCharacteristic(UUID.fromString(char_uuid));
        gg.setValue(data);
        mBluetoothGatt.writeCharacteristic(gg);
    }


    public BluetoothGattCharacteristic getTxGattChar() {
        return mBluetoothGatt.getService(UUID.fromString(Service_uuid))
                .getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
    }

    public BluetoothGattCharacteristic enable_JDY_ble(boolean p) {
        BluetoothGattCharacteristic ale = null;
        try {
            if (p) {
                BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(Service_uuid));
                switch (0) {
                    case 0://0xFFE1 //透传
                        ale = service.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
                        break;
                    case 1:// 0xFFE2 //iBeacon_UUID
                        ale = service.getCharacteristic(UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb"));
                        break;
                    case 2://0xFFE3 //iBeacon_Major
                        ale = service.getCharacteristic(UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb"));
                        break;
                    case 3://0xFFE4 //iBeacon_Minor
                        ale = service.getCharacteristic(UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb"));
                        break;
                    case 4://0xFFE5 //广播间隔
                        ale = service.getCharacteristic(UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb"));
                        break;
                    case 5://0xFFE6 //密码功能
                        ale = service.getCharacteristic(UUID.fromString("0000ffe6-0000-1000-8000-00805f9b34fb"));
                        break;
                    case 6:// 0xFFE7 //设备名功能
                        ale = service.getCharacteristic(UUID.fromString("0000ffe7-0000-1000-8000-00805f9b34fb"));
                        break;
                    case 7:// 0xFFE8 //IO输出功能功能
                        ale = service.getCharacteristic(UUID.fromString("0000ffe8-0000-1000-8000-00805f9b34fb"));
                        break;
                    case 8:// 0xFFE9 //PWM功能
                        ale = service.getCharacteristic(UUID.fromString("0000ffe9-0000-1000-8000-00805f9b34fb"));
                        break;
                    case 9:// 0xFFEA //复位模块
                        ale = service.getCharacteristic(UUID.fromString("0000ffea-0000-1000-8000-00805f9b34fb"));
                        break;
                    case 10:// 0xFFEB //发射功率
                        ale = service.getCharacteristic(UUID.fromString("0000ffeb-0000-1000-8000-00805f9b34fb"));
                        break;
                    case 11:// 0xFFEC //RTC功能
                        ale = service.getCharacteristic(UUID.fromString("0000ffec-0000-1000-8000-00805f9b34fb"));
                        break;
                    default:
                        ale = service.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
                        break;
                }
                boolean set = mBluetoothGatt.setCharacteristicNotification(ale, true);
                Log.e(TAG, " setnotification = " + set);
                BluetoothGattDescriptor dsc = ale.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                if (dsc != null) {
                    byte[] bytes = {0x01, 0x00};
                    dsc.setValue(bytes);
                    boolean success = mBluetoothGatt.writeDescriptor(dsc);
                    Log.e(TAG, "writing enabledescriptor:" + success);
                }
            } else {
                BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455"));
                ale = service.getCharacteristic(UUID.fromString(Service_uuid));
                boolean set = mBluetoothGatt.setCharacteristicNotification(ale, false);
                Log.e(TAG, " setnotification = " + set);
                BluetoothGattDescriptor dsc = ale.getDescriptor(UUID.fromString(Characteristic_uuid_TX));
                byte[] bytes = {0x00, 0x00};
                dsc.setValue(bytes);
                boolean success = mBluetoothGatt.writeDescriptor(dsc);
                Log.e(TAG, "writing enabledescriptor:" + success);
            }
//        	jdy=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//        	mBluetoothGatt.setCharacteristicNotification(jdy, p);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return ale;
    }

    public void write2Tx(byte[] value) {
        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(Service_uuid));
        if (service == null) {
            Log.e(TAG, "没有找到BluetoothGattService");
            return;
        }
        BluetoothGattCharacteristic gg = service.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
        if (gg == null) {
            Log.e(TAG, "没有找到BluetoothGattCharacteristic");
            return;
        }
        gg.setValue(value);
        mBluetoothGatt.writeCharacteristic(gg);
        SystemClock.sleep(100);
    }

    private void writeFrame(byte[] bytes) {
        int size = bytes.length;
        byte[] buffer = new byte[20];
        while (size >= 20) {
            System.arraycopy(bytes, 0, buffer, 0, 20);
            write2Tx(buffer);
            System.arraycopy(bytes, 20, bytes, 0, size - 20);
            size -= 20;
//            Log.e("TAG", size + ";");
        }
        if (size > 0) {
            byte[] new_buffer = new byte[size];
            System.arraycopy(bytes, 0, new_buffer, 0, size);
            write2Tx(new_buffer);
        }
    }

    public void shakeHand() {
        Frame frame = new Frame("00", "00", "01", "");
        byte[] bytes = Utils.getHexBytes(frame.toString());
        writeFrame(bytes);
    }

    public void requestSpecifiedDeviceStatus(String deviceId) {
        Frame frame = new Frame("00", "01", "01", deviceId);
        byte[] bytes = Utils.getHexBytes(frame.toString());
        writeFrame(bytes);
    }

    public void requestSpecifiedDeviceData(String deviceId) {
        Frame frame = new Frame("00", "02", "01", deviceId);
        byte[] bytes = Utils.getHexBytes(frame.toString());
        writeFrame(bytes);
    }


    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
