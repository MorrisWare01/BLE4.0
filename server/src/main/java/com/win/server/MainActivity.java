package com.win.server;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final boolean DEBUG = true;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SERVICE_UUID_1 = "";
    private static final String CHAR_UUID_1 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothGattServer mGattServer = mBluetoothManager.openGattServer(this, mCallbacks);
        BluetoothGattService mService = createService();
        mGattServer.addService(mService);
    }

    private BluetoothGattService createService() {
        BluetoothGattService firstService = new BluetoothGattService(
                UUID.fromString(SERVICE_UUID_1), BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic firstServiceChar = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_UUID_1),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY
        );
        return firstService;
    }

    private BluetoothDevice mDevice;
    private String mCharacteristicString;
    private final BluetoothGattServerCallback mCallbacks = new BluetoothGattServerCallback() {
        @Override
        //获取连接状态方法，BLE设备连接上或断开时，会调用到此方
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (DEBUG) Log.d(TAG, "onConnectionStateChange: newState=" + newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mDevice = device;
                    String devicename = device.getName();
                    String address = device.getAddress();
//                    notifyConnected(devicename);
//                    beginNotification();
                } else if (status == BluetoothProfile.STATE_DISCONNECTED) {
//                    stopNotification();
//                    notifyDisconnected();
                    mDevice = null;
                }
            }
        }

        //service添加成功会调用此方
        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            if (DEBUG) Log.d(TAG, "service added: " + status);
        }

        //读写Characteristic，在此获得客户端发来的消息
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            if (DEBUG) Log.d(TAG, "onCharacteristicWriteRequest: preparedWrite=" + preparedWrite);
            try {
                mCharacteristicString = new String(value); //客户端发来的消息
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                                int offset, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);
            if (characteristic.getUuid().equals(UUID.fromString(CHAR_UUID_1))) {
                characteristic.setValue("test");
//                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
            }
        }

    };


}
