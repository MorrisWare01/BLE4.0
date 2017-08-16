package com.win.server.demo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.win.server.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ServerActivity extends Activity {
    private static final String TAG = "BLE";

    private Button btnStopAdv;
    private Button btnAdv;
    private Button btnSendData;
    private EditText etInput;
    private TextView tvServer;

    private BluetoothGattServer mGattServer;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothDevice mConnectedDevice;

    private boolean isAdvertising;
    private boolean isDeviceSet = false;

    private ArrayList<BluetoothGattService> mAdvertisingServices;
    private List<ParcelUuid> mServiceUuids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        isAdvertising = false;

        mBluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        //bluetoothAdapter.setName(BLUETOOTH_ADAPTER_NAME);
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mAdvertisingServices = new ArrayList<BluetoothGattService>();
        mServiceUuids = new ArrayList<ParcelUuid>();

        btnAdv = (Button) findViewById(R.id.buttonAdvStart);
        btnStopAdv = (Button) findViewById(R.id.buttonAdvStop);
        btnSendData = (Button) findViewById(R.id.buttonSendServer);
        tvServer = (TextView) findViewById(R.id.textViewServer);
        etInput = (EditText) findViewById(R.id.editTextInputServer);
        etInput.setText("Server");

        //adding service and characteristics
        BluetoothGattService firstService = new BluetoothGattService(UUID.fromString(BluetoothUtility.SERVICE_UUID_1), BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic firstServiceChar = new BluetoothGattCharacteristic(
                UUID.fromString(BluetoothUtility.CHAR_UUID_1),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        firstService.addCharacteristic(firstServiceChar);

        mAdvertisingServices.add(firstService);
        mServiceUuids.add(new ParcelUuid(firstService.getUuid()));
    }

    @Override
    protected void onDestroy() {
        if (mAdvertisingServices != null) {
            mAdvertisingServices.clear();
            mAdvertisingServices = null;
        }
        if (mServiceUuids != null) {
            mServiceUuids.clear();
            mServiceUuids = null;
        }
        stopAdvertise();
        super.onDestroy();
    }

    public void handleStartClick(View view) {
        startAdvertise();
        btnAdv.setEnabled(false);
        btnStopAdv.setEnabled(true);
    }

    public void handleStopClick(View view) {
        stopAdvertise();
        btnAdv.setEnabled(true);
        btnStopAdv.setEnabled(false);
    }

    public void handleSendClick(View view) {
        if (isDeviceSet && writeCharacteristicToGatt(etInput.getText().toString())) {
            Toast.makeText(ServerActivity.this, "Data written", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Data written from server");
        } else {
            Toast.makeText(ServerActivity.this, "Data not written", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Data not written");
        }
    }

    //Check if bluetooth is enabled, if not, then request enable
    private void enableBluetooth() {
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth NOT supported");
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    private void startGattServer() {
        mGattServer = mBluetoothManager.openGattServer(getApplicationContext(), gattServerCallback);
        for (int i = 0; i < mAdvertisingServices.size(); i++) {
            mGattServer.addService(mAdvertisingServices.get(i));
            Log.e(TAG, "uuid" + mAdvertisingServices.get(i).getUuid());
        }
    }

    //Public method to begin advertising services
    public void startAdvertise() {
        if (isAdvertising) return;
        enableBluetooth();
        startGattServer();

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();

        dataBuilder.setIncludeTxPowerLevel(false); //necessity to fit in 31 byte advertisement
        dataBuilder.setIncludeDeviceName(true);
        for (ParcelUuid serviceUuid : mServiceUuids)
            dataBuilder.addServiceUuid(serviceUuid);

        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);

        mBluetoothLeAdvertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), advertiseCallback);
        isAdvertising = true;
    }

    //Stop ble advertising and clean up
    public void stopAdvertise() {
        if (!isAdvertising) return;
        mBluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
        mGattServer.clearServices();
        mGattServer.close();
        mAdvertisingServices.clear();
        isAdvertising = false;
    }

    public boolean writeCharacteristicToGatt(String data) {
        final BluetoothGattService service = mGattServer.getService(UUID.fromString(BluetoothUtility.SERVICE_UUID_1));
        final BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(BluetoothUtility.CHAR_UUID_1));

        if (mConnectedDevice != null) {
            byte[] bytes = Utils.getHexBytes(data);
            int size = bytes.length;
            byte[] buffer = new byte[20];
            while (size >= 20) {
                System.arraycopy(bytes, 0, buffer, 0, 20);
                characteristic.setValue(buffer);
                mGattServer.notifyCharacteristicChanged(mConnectedDevice, characteristic, true);
                System.arraycopy(bytes, 20, bytes, 0, size - 20);
                size -= 20;
                Log.e("TAG", size + ";");
            }
            if (size > 0) {
                byte[] new_buffer = new byte[size];
                System.arraycopy(bytes, 0, new_buffer, 0, size);
                characteristic.setValue(new_buffer);
                mGattServer.notifyCharacteristicChanged(mConnectedDevice, characteristic, true);
            }
            return true;
        } else
            return false;
    }

    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings advertiseSettings) {
            String successMsg = "Advertisement command attempt successful";
            Log.e(TAG, successMsg);
        }

        @Override
        public void onStartFailure(int i) {
            String failMsg = "Advertisement command attempt failed: " + i;
            Log.e(TAG, failMsg);
        }
    };

    public BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.e(TAG, "onConnectionStateChange status=" + status + "->" + newState);
            mConnectedDevice = device;
            isDeviceSet = true;
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.e(TAG, "service added: " + status);
        }

        String SHAKE_HANDE = "7E 00 39 04 01 FF FF FF FF FF FF FF FF 00 01 00 02 01 01 00 00 00 01 53 4D 44 43 5F 30 30 30 32 FF FF FF FF FF FF FF FC C7 7F FC CB 6F FF FF FF FF FF FF FF FF FF FF 20 69 ";

        @Override
        public void onCharacteristicReadRequest(
                BluetoothDevice device,
                int requestId,
                int offset,
                BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);

            if (characteristic.getUuid().equals(UUID.fromString(BluetoothUtility.CHAR_UUID_1))) {
//                writeCharacteristicToGatt(SHAKE_HANDE.replace(" ", ""));
//                characteristic.setValue(Utils.getHexBytes("FFEe"));
//                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, Utils.getHexBytes("FFEe"));
            }
        }

        @Override
        public void onCharacteristicWriteRequest(
                BluetoothDevice device,
                int requestId,
                BluetoothGattCharacteristic characteristic,
                boolean preparedWrite,
                boolean responseNeeded,
                int offset,
                byte[] value) {
            Log.e(TAG, "Data written: " + Utils.bytes2HexString(value));
            if (value != null) {
                if (value[0] == 0x7E && cap == 0) {
                    cap = value[1] * 256 + value[2];
                    frame = new byte[cap];
                    System.arraycopy(value, 0, frame, size, value.length);
                    size = value.length;
                    Log.e("TAG", size + "/" + cap);
                    if (size >= cap) {
                        send(Utils.bytes2HexString(frame));
                        size = 0;
                        cap = 0;
                    }
                } else {
                    System.arraycopy(value, 0, frame, size, value.length);
                    size += value.length;
                    if (size >= cap) {
                        send(Utils.bytes2HexString(frame));
                        size = 0;
                        cap = 0;
                    }
                }
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            } else
                Log.e(TAG, "value is null");
        }

        private void send(final String tmp) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    tvServer.setText(tmp);
                }
            });
            if (tmp.equals("7E00110400FFFFFFFFFFFFFFFF0001088C")) {
                writeCharacteristicToGatt("7E 00 39 04 01 FF FF FF FF FF FF FF FF 00 01 00 02 01 01 00 00 00 01 53 4D 44 43 5F 30 30 30 32 FF FF FF FF FF FF FF FC C7 7F FC CB 6F FF FF FF FF FF FF FF FF FF FF 20 69 ".replace(" ", ""));
            } else if (tmp.equals("7E 00 15 04 00 FF FF FF FF FF FF FF FF 01 01 01 00 00 01 08 93".replace(" ", ""))) {
                Random random = new Random();
                if (random.nextBoolean()) {
                    writeCharacteristicToGatt("7E 00 16 04 01 FF FF FF FF FF FF FF FF 01 01 01 00 00 01 01 08 96".replace(" ", ""));
                } else {
                    writeCharacteristicToGatt("7E 00 16 04 01 FF FF FF FF FF FF FF FF 01 01 01 00 00 01 00 08 95".replace(" ", ""));
                }
            } else if (tmp.equals("7E 00 15 04 00 FF FF FF FF FF FF FF FF 01 01 02 00 00 01 08 94 ".replace(" ", ""))) {
                Random random = new Random();
                if (random.nextBoolean()) {
                    writeCharacteristicToGatt("7E 00 16 04 01 FF FF FF FF FF FF FF FF 01 01 02 00 00 01 01 08 97".replace(" ", ""));
                } else {
                    writeCharacteristicToGatt("7E 00 16 04 01 FF FF FF FF FF FF FF FF 01 01 02 00 00 01 00 08 96".replace(" ", ""));
                }
            } else if (tmp.equals("7E 00 15 04 00 FF FF FF FF FF FF FF FF 01 01 03 00 00 01 08 95".replace(" ", ""))) {
                Random random = new Random();
                if (random.nextBoolean()) {
                    writeCharacteristicToGatt("7E 00 16 04 01 FF FF FF FF FF FF FF FF 01 01 03 00 00 01 01 08 98".replace(" ", ""));
                } else {
                    writeCharacteristicToGatt("7E 00 16 04 01 FF FF FF FF FF FF FF FF 01 01 03 00 00 01 00 08 97".replace(" ", ""));
                }
            } else if (tmp.equals("7E 00 15 04 00 FF FF FF FF FF FF FF FF 01 01 04 00 00 01 08 96".replace(" ", ""))) {
                Random random = new Random();
                if (random.nextBoolean()) {
                    writeCharacteristicToGatt("7E 00 16 04 01 FF FF FF FF FF FF FF FF 01 01 04 00 00 01 01 08 99".replace(" ", ""));
                } else {
                    writeCharacteristicToGatt("7E 00 16 04 01 FF FF FF FF FF FF FF FF 01 01 04 00 00 01 00 08 98".replace(" ", ""));
                }
            } else if (tmp.equals("7E 00 15 04 00 FF FF FF FF FF FF FF FF 01 01 05 00 00 01 08 97".replace(" ", ""))) {
                Random random = new Random();
                if (random.nextBoolean()) {
                    writeCharacteristicToGatt("7E 00 16 04 01 FF FF FF FF FF FF FF FF 01 01 05 00 00 01 00 08 99".replace(" ", ""));
                } else {
                    writeCharacteristicToGatt("7E 00 16 04 01 FF FF FF FF FF FF FF FF 01 01 05 00 00 01 01 08 9A".replace(" ", ""));
                }
            }else if (tmp.equals("7E 00 15 04 00 FF FF FF FF FF FF FF FF 01 01 06 00 00 01 08 98".replace(" ", ""))) {
                Random random = new Random();
                if (random.nextBoolean()) {
                    writeCharacteristicToGatt("7E 00 16 04 01 FF FF FF FF FF FF FF FF 01 01 06 00 00 01 00 08 9A".replace(" ", ""));
                } else {
                    writeCharacteristicToGatt("7E 00 16 04 01 FF FF FF FF FF FF FF FF 01 01 06 00 00 01 01 08 9B".replace(" ", ""));
                }
            } else if (tmp.equals("7E 00 15 04 00 FF FF FF FF FF FF FF FF 02 01 01 00 00 01 08 94".replace(" ", ""))) {
                Random random = new Random();
                int i = random.nextInt(3);
                if (i == 0) {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 01 00 00 01 69 2B 30 31 2E 36 32 34 30 0A 8D".replace(" ", ""));
                } else if (i == 1) {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 01 00 00 01 FF FF FF FF FF FF FF FF FF 11 95".replace(" ", ""));
                } else {
                    writeCharacteristicToGatt(" 7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 01 00 00 01 6D 2B 30 34 31 2E 32 35 30 0A 90".replace(" ", ""));
                }
            } else if (tmp.equals("7E 00 15 04 00 FF FF FF FF FF FF FF FF 02 01 02 00 00 01 08 95 ".replace(" ", ""))) {
                Random random = new Random();
                int i = random.nextInt(3);
                if (i == 0) {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 02 00 00 01 69 2B 30 2E 32 37 30 37 35 0A 96".replace(" ", ""));
                } else if (i == 1) {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 02 00 00 01 6D 2B 30 30 36 2E 38 37 38 0A A2".replace(" ", ""));
                } else {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 02 00 00 01 FF FF FF FF FF FF FF FF FF 11 96".replace(" ", ""));
                }
            } else if (tmp.equals("7E 00 15 04 00 FF FF FF FF FF FF FF FF 02 01 03 00 00 01 08 96".replace(" ", ""))) {
                Random random = new Random();
                int i = random.nextInt(3);
                if (i == 0) {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 03 00 00 01 69 2D 30 2E 31 34 38 31 30 0A 92".replace(" ", ""));
                } else if (i == 1) {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 03 00 00 01 6D 2D 30 30 33 2E 37 36 32 0A 9A".replace(" ", ""));
                } else {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 03 00 00 01 FF FF FF FF FF FF FF FF FF 11 97".replace(" ", ""));
                }
            } else if (tmp.equals("7E 00 15 04 00 FF FF FF FF FF FF FF FF 02 01 04 00 00 01 08 97".replace(" ", ""))) {
                Random random = new Random();
                int i = random.nextInt(3);
                if (i == 0) {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 04 00 00 01 4E 2D 30 30 30 30 2E 32 00 0A 3C".replace(" ", ""));
                } else if (i == 1) {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 04 00 00 01 FF FF FF FF FF FF FF FF FF 11 98".replace(" ", ""));
                } else {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 04 00 00 01 FF FF FF FF FF FF FF FF FF 11 98".replace(" ", ""));
                }
            }else if (tmp.equals("7E 00 15 04 00 FF FF FF FF FF FF FF FF 02 01 05 00 00 01 08 98".replace(" ", ""))) {
                Random random = new Random();
                int i = random.nextInt(3);
                if (i == 0) {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 05 00 00 01 FF FF FF FF FF FF FF FF FF 11 99".replace(" ", ""));
                } else if (i == 1) {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 05 00 00 01 69 2B 30 33 2E 34 39 32 30 0A 96".replace(" ", ""));
                } else {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 05 00 00 01 6D 2B 30 38 38 2E 36 39 30 0A A7".replace(" ", ""));
                }
            }else if (tmp.equals("7E 00 15 04 00 FF FF FF FF FF FF FF FF 02 01 06 00 00 01 08 99".replace(" ", ""))) {
                Random random = new Random();
                int i = random.nextInt(2);
                if (i == 0) {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 06 00 00 01 FF FF FF FF FF FF FF FF FF 11 9A".replace(" ", ""));
                } else if (i == 1) {
                    writeCharacteristicToGatt("7E 00 1E 04 01 FF FF FF FF FF FF FF FF 02 01 06 00 00 01 48 2B 30 30 30 30 36 35 38 0A 79".replace(" ", ""));
                }
            }
        }
    };

    private int size;
    private byte[] frame;
    private int cap;
}
