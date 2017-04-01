package studioes.arm.six.bluetoothbuddies;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import studioes.arm.six.bluetoothbuddies.bluetooth.BluetoothStuff;
import studioes.arm.six.bluetoothbuddies.bluetooth.BluetoothTalker;
import studioes.arm.six.bluetoothbuddies.bluetooth.IBluetoothClientListener;

/**
 * Created by sithel on 3/19/17.
 */

public class ServerService extends Service implements IServerService {
    public static final String TAG = ServerService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();

    public static Intent createStartIntent(Context context) {
        Intent intent = new Intent(context, ServerService.class);
        return intent;
    }

    BluetoothStuff mBluetoothStuff;
    BehaviorProcessor<String> mIncomingMsgs = BehaviorProcessor.create();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public IServerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ServerService.this;
        }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see us trying to start w/ client id (flags [" + flags + "], startId [" + startId + "]) : " + intent);
        return START_STICKY;
    }

    @Nullable @Override public IBinder onBind(Intent intent) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see an onBind request : " + intent);
        return mBinder;
    }

    @Override public boolean onUnbind(Intent intent) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see an onUNbind request : " + intent);
        // TODO : start countdown timer to shut down service
        return true;
    }

    @Override public void onRebind(Intent intent) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see an onREbind request : " + intent);
        // TODO : stop countdown timer, keep service
        super.onRebind(intent);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, TAG + " done", Toast.LENGTH_SHORT).show();
        if (mBluetoothStuff != null) {
            mBluetoothStuff.shutShitDown(this);
        }
    }



    private Handler getHandler() {
        return new Handler() {
//            int ri = 0;

            @Override public void handleMessage(Message msg) {
                super.handleMessage(msg);
                byte[] byteArray = (byte[]) msg.obj;
                int length = msg.arg1;
                byte[] resultArray = length == -1 ? byteArray : new byte[length];
                for (int i = 0; i < byteArray.length && i < length; ++i) {
                    resultArray[i] = byteArray[i];
                }
                String text = new String(resultArray, StandardCharsets.UTF_8);
                if (msg.what == BluetoothTalker.MessageConstants.MESSAGE_WRITE) {
                    Log.i(TAG, "we just wrote... [" + length + "] '" + text + "'");
//                    mIncomingMsgs.onNext(text);
                } else if (msg.what == BluetoothTalker.MessageConstants.MESSAGE_READ) {
                    Log.i(TAG, "we just read... [" + length + "] '" + text + "'");
                    Log.i(TAG, "    >>r " + Arrays.toString((byte[]) msg.obj));
                    mIncomingMsgs.onNext(text);
                    sendMsg("I heard you : " + Math.random() + "!");
//                    mReadTxt.setText(++ri + "] " + text);
//                    if (mServerBound && mServerService.isConnected()) {
//                        mServerService.sendMsg("I heard you : " + Math.random() + "!");
//                    } else if (mClientBound && mClientService.isConnected()) {
//                        mClientService.sendMsg("I heard you : " + Math.random() + "!");
//                    }
//                    mBluetoothStuff.mTalker.write();
                }
            }
        };
    }
    //region interface methods

    @Override public void startHosting(IBluetoothClientListener listener) {
        if (mBluetoothStuff == null) {
            mBluetoothStuff = new BluetoothStuff(listener, getHandler());
        }
        if (!mBluetoothStuff.ensureBluetoothSetup(this)) {
            return;
        }
        mBluetoothStuff.beVisibleToDevices(listener);
        try {
            mBluetoothStuff.startAsServer(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public boolean isConnected() {
        return mBluetoothStuff != null && mBluetoothStuff.mTalker != null;
    }

    @Override public void sendMsg(String msg) {
        mBluetoothStuff.mTalker.write(msg);
    }

    @Override public Flowable<String> getMessageUpdates() {
        return mIncomingMsgs;
    }
    //endregion
}
