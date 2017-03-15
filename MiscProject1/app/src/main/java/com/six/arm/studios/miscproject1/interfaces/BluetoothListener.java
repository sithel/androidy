package com.six.arm.studios.miscproject1.interfaces;

import android.bluetooth.BluetoothDevice;

/**
 * Created by sithel on 3/14/17.
 */

public interface BluetoothListener {
    void onDeviceFound(BluetoothDevice device);
}
