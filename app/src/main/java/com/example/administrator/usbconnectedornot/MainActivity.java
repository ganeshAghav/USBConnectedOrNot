package com.example.administrator.usbconnectedornot;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "UsbHost";

    TextView mDeviceText;
    Button mConnectButton;
    UsbManager mUsbManager;
    UsbDevice mDevice;
    PendingIntent mPermissionIntent;

    private static final int REQUEST_TYPE = 0x80;
    private static final int REQUEST = 0x06;
    private static final int REQ_VALUE = 0x200;
    private static final int REQ_INDEX = 0x00;
    private static final int LENGTH = 64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mDeviceText = (TextView) findViewById(R.id.text_status);
        mConnectButton = (Button) findViewById(R.id.button_connect);
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        updateDeviceList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
    }

    public void onConnectClick(View v) {
        if (mDevice == null) {
            return;
        }
        mUsbManager.requestPermission(mDevice, mPermissionIntent);
    }


    private static final String ACTION_USB_PERMISSION = "com.android.recipes.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        && device != null) {

                    getDeviceStatus(device);
                } else {
                    Log.d(TAG, "permission denied for device " + device);
                }
            }
        }
    };


    private void getDeviceStatus(UsbDevice device) {
        UsbDeviceConnection connection = mUsbManager.openDevice(device);
        byte[] buffer = new byte[LENGTH];
        connection.controlTransfer(REQUEST_TYPE, REQUEST, REQ_VALUE, REQ_INDEX,
                buffer, LENGTH, 2000);
        connection.close();
    }

    private void updateDeviceList() {
        HashMap<String, UsbDevice> connectedDevices = mUsbManager
                .getDeviceList();
        if (connectedDevices.isEmpty()) {
            mDevice = null;
            mDeviceText.setText("No Devices Currently Connected");
            mConnectButton.setEnabled(false);


        } else {
            for (UsbDevice device : connectedDevices.values()) {
                mDevice = device;
            }
            mDeviceText.setText("USB Device connected");
            mConnectButton.setEnabled(true);
        }
    }
}