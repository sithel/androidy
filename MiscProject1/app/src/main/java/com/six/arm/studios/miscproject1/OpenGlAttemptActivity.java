package com.six.arm.studios.miscproject1;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.six.arm.studios.miscproject1.bluetooth.BlueToothTalker;
import com.six.arm.studios.miscproject1.interfaces.BluetoothListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import studioes.arm.six.quizletapi20modelretrieval.models.QSet;
import studioes.arm.six.quizletapi20modelretrieval.services.IModelRetrievalService;
import studioes.arm.six.quizletapi20modelretrieval.services.ModelRetrievalService;

public class OpenGlAttemptActivity extends AppCompatActivity implements BluetoothListener {
    @LayoutRes
    public static final int LAYOUT_ID = R.layout.activity_open_gl_attempt;
    public static final String TAG = "RebeccaActivity";
    public static final int REQUEST_ENABLE_BT = 100;

    @BindView(R.id.fancy_gl_surface) MyGLSurfaceView mGLView;
    @BindView(R.id.start_looking_button) View mLookForDevicesButton;
    @BindView(R.id.be_visible_button) View mBeVisibleButton;
    @BindView(R.id.spotted_devices) LinearLayout mDeviceList;
    @BindView(R.id.bluetooth_read) TextView mReadTxt;
    @BindView(R.id.bluetooth_write) TextView mWriteTxt;

    @BindView(R.id.qset_id) EditText mSetId;
    @BindView(R.id.server_results) TextView mServerResults;

    BluetoothStuff mBluetoothStuff;

    IModelRetrievalService mService;
    boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(ModelRetrievalService.startIntent(this, "fakeClientId"));

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View v = getLayoutInflater().inflate(LAYOUT_ID, null);
        setContentView(v);
        ButterKnife.bind(this);
        mGLView.setPreserveEGLContextOnPause(true);

        Handler handler = new Handler() {
            int ri = 0;
            int wi = 0;

            @Override public void handleMessage(Message msg) {
                super.handleMessage(msg);
                byte[] byteArray = (byte[]) msg.obj;
                int length = msg.arg1;
                byte[] resultArray = length == -1 ? byteArray : new byte[length];
                for (int i = 0; i < byteArray.length && i < length; ++i) {
                    resultArray[i] = byteArray[i];
                }
                String text = new String(resultArray, StandardCharsets.UTF_8);
                if (msg.what == BlueToothTalker.MessageConstants.MESSAGE_WRITE) {
                    Log.i(TAG, "we just wrote... [" + length + "] '" + text + "'");
                    mWriteTxt.setText(++wi + "] " + text);
                } else if (msg.what == BlueToothTalker.MessageConstants.MESSAGE_READ) {
                    Log.i(TAG, "we just read... [" + length + "] '" + text + "'");
                    Log.i(TAG, "    >>r " + Arrays.toString((byte[]) msg.obj));
                    mReadTxt.setText(++ri + "] " + text);
                    mBluetoothStuff.mTalker.write("I heard you : " + Math.random() + "!");
                }
            }
        };
        mBluetoothStuff = new BluetoothStuff(this, handler);
        if (mBluetoothStuff.ensureBluetoothSetup(this)) {
            hookUpThings();
            return;
        }
    }

    @OnClick(R.id.bluetooth_write)
    public void handleWriteClick() {
        Log.i(TAG, "... am desperately trying to write something... " + Arrays.toString(new byte[]{63, 24, 100, 8, 65}));
        mBluetoothStuff.mTalker.write("button press");
    }

    @OnClick(R.id.service_ping_btn)
    public void handleServicePing() {
        if (!mBound) {
            Toast.makeText(this, "wtf, y no service bound?", Toast.LENGTH_SHORT).show();
            return;
        }
        String userInput = mSetId.getText().toString();
        try {
            Long id = Long.valueOf(userInput);
            mService.requestSet(id);
        } catch (NumberFormatException e) {
            Log.d(TAG, "couldn't make a number out of " + userInput);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothStuff != null) {
            mBluetoothStuff.shutShitDown(this);
        }

    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Well fuck you too", Toast.LENGTH_LONG).show();
            return;
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "User approved BlueTooth on...");
        } else {
            Log.i(TAG, "I got back a result I've no idea what it is... Request Code: " + requestCode + ", result code " + resultCode + " :: " + data);
        }
        hookUpThings();
    }

    public void hookUpThings() {
        List<Pair<String, String>> pairedDevices = mBluetoothStuff.getExistingPairedDevices();
        for (Pair<String, String> device : pairedDevices) {
            Log.i(TAG, "I see pre-existing device info : " + device.first + " [" + device.second + "] ");
        }
        mBeVisibleButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Log.i(TAG, "be visible...");
                mBluetoothStuff.beVisibleToDevices(OpenGlAttemptActivity.this);
                try {
                    mBluetoothStuff.startAsServer(OpenGlAttemptActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mLookForDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Log.i(TAG, "start looking...");
                mBluetoothStuff.lookForDevices(OpenGlAttemptActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        mGLView.onResume();
    }

    @Override
    protected void onPause() {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        mGLView.onPause();
    }


    //region Bluetooth listener

    @Override public void onDeviceFound(BluetoothDevice device) {
        String deviceName = device.getName();
        String deviceHardwareAddress = device.getAddress(); // MAC address
        addNewDeviceUI(deviceName, deviceHardwareAddress, device);
    }

    //endregion

    //region private helper methods
    private void addNewDeviceUI(String name, final String macAddress, final BluetoothDevice device) {
        // TODO : note, we'll get this callback multiple times, make sure we only show items once
        TextView child = new TextView(this);
        child.setText(name + ":" + macAddress);
        child.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Log.i(TAG, "Totes going to do something with this now... " + macAddress);
                mBluetoothStuff.tryToConnectToServer(OpenGlAttemptActivity.this, device);
            }
        });
        mDeviceList.addView(child);
    }
    //endregion


    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, ModelRetrievalService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);   // if I only JUST bind, the service dies when we background :'(
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private void hookUpServiceSubscribers() {
        Log.i(TAG, "Hooking up Service Subscribers ");
        mService.getQSetFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((QSet qSet) -> mServerResults.setText(qSet.title()))
        ;
    }

    private void unhookServiceSubscribers() {
        Log.i(TAG, "UN hooking service subscribers");

    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ModelRetrievalService.LocalBinder binder = (ModelRetrievalService.LocalBinder) service;
            mService = binder.getService();
            hookUpServiceSubscribers();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            unhookServiceSubscribers();
            mBound = false;
        }
    };
}
