package studioes.arm.six.bluetoothbuddies.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by sithel on 3/19/17.
 */

public interface IBluetoothClientListener {
    void onDeviceFound(BluetoothDevice device);
}
