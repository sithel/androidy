package com.six.arm.studios.miscproject1;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;

import com.six.arm.studios.miscproject1.bluetooth.BlueToothTalker;
import com.six.arm.studios.miscproject1.interfaces.BluetoothListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.six.arm.studios.miscproject1.OpenGlAttemptActivity.REQUEST_ENABLE_BT;

/**
 * Created by sithel on 3/9/17.
 */

public class BluetoothStuff {
    public static final UUID APP_UUID = UUID.fromString("c6293521-3154-4765-933b-19219e20f0a9");

    public static final String TAG = "RebeccaBluetoothStuff";
    public static final int DISCOVERABLE_DURATION_S = 300;
    BluetoothAdapter mBluetoothAdapter;

    BluetoothListener mMyListener;
    BlueToothTalker mTalker;
    Handler mHandler;

    public BluetoothStuff(OpenGlAttemptActivity context, Handler handler) {
        mMyListener = context;
        mHandler = handler;
    }


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.e(TAG, "IT IS FOUND!!!!!!!! " + action);
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                mMyListener.onDeviceFound(device);
                Log.i(TAG, "Totes just got an update about " + deviceName + " [" + deviceHardwareAddress + "]");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "Done with discovery... :(  ");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i(TAG, "We're starting... fingers crossed");
            } else {
                Log.i(TAG, "... what is this?? " + action);
            }
        }
    };

    /**
     * @return true if we are good to go! False if we gotta' wait for shit
     */
    public boolean ensureBluetoothSetup(OpenGlAttemptActivity context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(context, "Yo, dawg, no blue tooth. So sad!", Toast.LENGTH_LONG).show();
            throw new IllegalStateException("sucks");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    public List<Pair<String, String>> getExistingPairedDevices() {
        List<Pair<String, String>> deviceInfo = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                deviceInfo.add(new Pair<>(deviceName, deviceHardwareAddress));
            }
        }
        return deviceInfo;
    }

    private boolean mIsRegistered = false;
    public void shutShitDown(OpenGlAttemptActivity context) {
        // Don't forget to unregister the ACTION_FOUND receiver.
        if (mIsRegistered) {
            context.unregisterReceiver(mReceiver);
        }
    }

    public void beVisibleToDevices(OpenGlAttemptActivity context) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION_S);
        context.startActivity(discoverableIntent);
    }

    public void tryToConnectToServer(OpenGlAttemptActivity openGlAttemptActivity, BluetoothDevice device) {
        new ConnectThread(device).start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                String name = "sharks";
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(name, APP_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
//                    Note that when accept() returns the BluetoothSocket, the socket is already
// connected. Therefore, you shouldn't call connect(), as you do from the client side.
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }


        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

    }


    private void manageMyConnectedSocket(BluetoothSocket socket) {
        Log.i(TAG, "OMG! OMG! I've got a fucking socket!! Now what?? "+socket);
        mTalker = new BlueToothTalker(mHandler, socket);
        Log.i(TAG, "a");
        mTalker.start();
        Log.i(TAG, "b");
        mTalker.write("sharks!".getBytes());
        Log.i(TAG, "c");
    }

    public void startAsServer(OpenGlAttemptActivity context) throws IOException {
        new AcceptThread().start();
    }

    public void lookForDevices(OpenGlAttemptActivity context) {
        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        context.registerReceiver(mReceiver, bluetoothFilter);

        mIsRegistered = true;

        // TODO : check for the "nearby" permission and flip the fuck out if we don't have it

        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        Log.i(TAG, "Just kicked off startDiscovery: "+mBluetoothAdapter.startDiscovery());
    }
}
