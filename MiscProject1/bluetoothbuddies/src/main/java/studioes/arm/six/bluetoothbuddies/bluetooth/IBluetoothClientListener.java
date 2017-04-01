package studioes.arm.six.bluetoothbuddies.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;

/**
 * Created by sithel on 3/19/17.
 */

public interface IBluetoothClientListener {
    void onDeviceFound(BluetoothDevice device);
    void requestDiscoverabilityIntent(Intent intent);
    void isDiscoverable(boolean isDiscoverable);
    void requestPermission(String[] requestedPermissions);
}
