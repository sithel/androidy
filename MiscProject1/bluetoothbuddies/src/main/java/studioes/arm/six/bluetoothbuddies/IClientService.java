package studioes.arm.six.bluetoothbuddies;

import android.bluetooth.BluetoothDevice;

import io.reactivex.Flowable;
import studioes.arm.six.bluetoothbuddies.bluetooth.IBluetoothClientListener;

/**
 * Created by sithel on 3/19/17.
 */

public interface IClientService {
    void startLooking(IBluetoothClientListener listener);

    void connectToServer(BluetoothDevice device);

    void sendMsg(String msg);

    /**
     * @return true if it's in a state that can handle sending msgs to the other person
     */
    boolean isConnected();

    Flowable<String> getMessageUpdates();
}
