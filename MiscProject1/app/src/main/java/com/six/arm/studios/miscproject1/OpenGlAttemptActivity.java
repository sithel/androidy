package com.six.arm.studios.miscproject1;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import studioes.arm.six.bluetoothbuddies.ClientService;
import studioes.arm.six.bluetoothbuddies.IClientService;
import studioes.arm.six.bluetoothbuddies.IServerService;
import studioes.arm.six.bluetoothbuddies.ServerService;
import studioes.arm.six.bluetoothbuddies.bluetooth.IBluetoothClientListener;
import studioes.arm.six.graphics3d.text.CubeThing;
import studioes.arm.six.graphics3d.text.IRenderableView;
import studioes.arm.six.graphics3d.text.TextSurfaceRenderer;
import studioes.arm.six.quizletapi2.models.QSet;
import studioes.arm.six.quizletapi2.services.IModelRetrievalService;
import studioes.arm.six.quizletapi2.services.ModelRetrievalService;

public class OpenGlAttemptActivity extends AppCompatActivity implements IBluetoothClientListener {
    @LayoutRes
    public static final int LAYOUT_ID = R.layout.activity_open_gl_attempt;
    public static final String TAG = "RebeccaActivity";
    public static final int REQUEST_ENABLE_BT = 100;
    public static final int REQUEST_DISCOVERABLE_CODE = 42;
    public static final int REQUEST_PERMISSIONS_CODE = 666;

    @BindView(R.id.dummy_view) View mDummyView;
    @BindView(R.id.fancy_gl_surface) MyGLSurfaceView mGLView;
    @BindView(R.id.base_gl_surface) GLSurfaceView mBaseGlView;
    @BindView(R.id.text_to_render_3d) IRenderableView mRenderableView;
    @BindView(R.id.start_looking_button) TextView mPlayerButton;
    @BindView(R.id.start_hosting_button) TextView mHostButton;
    @BindView(R.id.spotted_devices) LinearLayout mDeviceList;
    @BindView(R.id.bluetooth_read) TextView mReadTxt;
    @BindView(R.id.bluetooth_write) TextView mWriteTxt;

    @BindView(R.id.qset_id) EditText mSetId;
    @BindView(R.id.server_results) TextView mServerResults;


    IModelRetrievalService mModelService;
    boolean mModelBound = false;
    IServerService mServerService;
    boolean mServerBound = false;
    IClientService mClientService;
    boolean mClientBound = false;

    TextSurfaceRenderer mTextRenderer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(ModelRetrievalService.startIntent(this, "fakeClientId"));
        startService(ClientService.createStartIntent(this));
        startService(ServerService.createStartIntent(this));

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View v = getLayoutInflater().inflate(LAYOUT_ID, null);
        setContentView(v);
        ButterKnife.bind(this);

        mGLView.setPreserveEGLContextOnPause(true);

        mTextRenderer = new CubeThing(this);
        mRenderableView.setTextRenderer(mTextRenderer);
        mBaseGlView.setEGLContextClientVersion(3);
        mBaseGlView.setRenderer(mTextRenderer);

        // TODO : *somebody* needs to warn about the Bluetooth not on... who?
//        mBluetoothStuff = new BluetoothStuff(this, handler);
//        if (mBluetoothStuff.ensureBluetoothSetup(this)) {
//            hookUpThings();
//            return;
//        }
    }

    @OnClick(R.id.bluetooth_write)
    public void handleWriteClick() {
        Log.i(TAG, "... am desperately trying to write something... " + Arrays.toString(new byte[]{63, 24, 100, 8, 65}));
        if (mServerBound && mServerService.isConnected()) {
            mServerService.sendMsg("button press");
        } else if (mClientBound && mClientService.isConnected()) {
            mClientService.sendMsg("button press");
        }
    }

    @OnClick(R.id.service_ping_btn)
    public void handleServicePing() {
        if (!mModelBound) {
            Toast.makeText(this, "wtf, y no service bound?", Toast.LENGTH_SHORT).show();
            return;
        }
        String userInput = mSetId.getText().toString();
        try {
            Long id = Long.valueOf(userInput);
            mModelService.requestSet(id);
        } catch (NumberFormatException e) {
            Log.d(TAG, "couldn't make a number out of " + userInput);
        }
    }

    @OnClick(R.id.start_hosting_button)
    public void handleStartHostingClick() {
        if (mServerBound) {
            mServerService.startHosting(this);
        } else {
            Log.e(TAG, "Wanted to be a Host but we're not server bound yet");
        }
    }

    @OnClick(R.id.start_looking_button)
    public void handleJoinClick() {
        if (mClientBound) {
            mClientService.startLooking(this);
        } else {
            Log.e(TAG, "Wanted to be a Player but we're not server bound yet");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO : blow stuff up / shut down services? maybe? maybe not
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Well fuck you too, bluetooth is only the core of everything here", Toast.LENGTH_LONG).show();
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "User approved BlueTooth on...");
        } else if (requestCode == REQUEST_DISCOVERABLE_CODE) {
            if (resultCode <= 0) {
                Toast.makeText(this, "Well fuck you too, we didn't want you to host", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Discoverable mode enabled. " + resultCode + " seconds", Toast.LENGTH_SHORT).show();
                mHostButton.setTextColor(ContextCompat.getColor(this, R.color.textColor));
                mHostButton.setText("HOSTING...");
                Observable.timer(resultCode, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((t) -> {
                            if (mHostButton == null) {
                                return;
                            }
                            mHostButton.setText("Be a Host");
                            mHostButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                        });
            }
        } else {
            Log.w(TAG, "I got back a result I've no idea what it is... Request Code: " + requestCode + ", result code " + resultCode + " :: " + data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE:
                if (grantResults.length > 0) {
                    for (int gr : grantResults) {
                        // Check if request is granted or not
                        if (gr != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "What, you wont give me my permissions? fuck you!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    //TODO - Add your code here to start Discovery

                }
                break;
            default:
                return;
        }
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

    @Override public void requestDiscoverabilityIntent(Intent intent) {
        Log.i(TAG, "About to start the discoverable intent...");
        startActivityForResult(intent, REQUEST_DISCOVERABLE_CODE);
    }

    @Override public void isDiscoverable(boolean isDiscoverable) {
        mPlayerButton.setTextColor(ContextCompat.getColor(this, isDiscoverable ? R.color.textColor : R.color.colorPrimary));
        mPlayerButton.setText(isDiscoverable ? "LOOKING.... " : "Look for a Host");
    }

    @Override public void requestPermission(String[] requestedPermissions) {
        requestPermissions(requestedPermissions, REQUEST_PERMISSIONS_CODE);
    }

    //endregion

    //region private helper methods
    private void addNewDeviceUI(String name, final String macAddress, final BluetoothDevice device) {
        // TODO : note, we'll get this callback multiple times, make sure we only show items once
        TextView child = new TextView(this);
        child.setText(name + ":" + macAddress);
        child.setOnClickListener((view) -> {
            if (mClientBound) {
                mClientService.connectToServer(device);
            }
        });
        mDeviceList.addView(child);
    }
    //endregion


    @Override
    protected void onStart() {
        super.onStart();
        // if I only JUST bind, the service dies when we background :'(
        // TODO : inspect these flags, I bet we want a different ont
        bindService(new Intent(this, ModelRetrievalService.class), mModelConnection, Context.BIND_AUTO_CREATE);
        bindService(new Intent(this, ServerService.class), mServerConnection, Context.BIND_AUTO_CREATE);
        bindService(new Intent(this, ClientService.class), mClientConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mModelBound) {
            unbindService(mModelConnection);
            mModelBound = false;
        }
        if (mServerBound) {
            unbindService(mServerConnection);
            mServerBound = false;
        }
        if (mClientBound) {
            unbindService(mClientConnection);
            mClientBound = false;
        }
    }
    TextToSpeech ttobj;

    private void hookUpServiceSubscribers() {
        if (ttobj == null) {
            ttobj = new TextToSpeech(this, status -> {

                Log.w(TAG, "I totes just inited this TTS object");
                Log.i(TAG, "delayed - available langs "+ ttobj.getAvailableLanguages());
                Log.i(TAG, "delayed - available engines "+ ttobj.getEngines());
                Log.i(TAG, "delayed - available voices "+ ttobj.getVoices());
                Log.i(TAG, "delayed - available default engine "+ ttobj.getDefaultEngine());
                Log.i(TAG, "delayed - available default voice "+ ttobj.getDefaultVoice());
                Log.i(TAG, "delayed - max speech input length "+ ttobj.getMaxSpeechInputLength());
            });
            Log.i(TAG, "available langs "+ ttobj.getAvailableLanguages());
            Log.i(TAG, "available engines "+ ttobj.getEngines());
            Log.i(TAG, "available voices "+ ttobj.getVoices());
            Log.i(TAG, "available default engine "+ ttobj.getDefaultEngine());
            Log.i(TAG, "available default voice "+ ttobj.getDefaultVoice());
        }
        ttobj.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) {
                Log.i(TAG, "starting utterance : "+utteranceId);
            }

            @Override public void onDone(String utteranceId) {
                Log.i(TAG, "finishing utterance : "+utteranceId);
            }

            @Override public void onError(String utteranceId) {
                Log.i(TAG, "erroring on utterance : "+utteranceId);
            }
        });
        Log.i(TAG, "Hooking up Service Subscribers ");
        mModelService.getQSetFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((QSet qSet) -> {
                    ttobj.setLanguage(Locale.UK);
//                    ttobj.speak("sharks", TextToSpeech.QUEUE_FLUSH, null);
                    int result = ttobj.speak("sharks", TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString());
                    Log.i(TAG, "We just tried to TTS and got : "+result);
                    mServerResults.setText(qSet.title());
                    mRenderableView.setText(qSet.title());
                })
        ;
    }

    private void unhookServiceSubscribers() {
        Log.i(TAG, "UN hooking service subscribers");
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mModelConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ModelRetrievalService.LocalBinder binder = (ModelRetrievalService.LocalBinder) service;
            mModelService = binder.getService();
            hookUpServiceSubscribers();
            mModelBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            unhookServiceSubscribers();
            mModelBound = false;
        }
    };
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mServerConnection = new ServiceConnection() {

        int msgCount = 0;

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ServerService.LocalBinder binder = (ServerService.LocalBinder) service;
            mServerService = binder.getService();
            mServerService.getMessageUpdates().subscribe((msg) -> {
                mWriteTxt.setText(++msgCount + "] " + msg);
            });
            mServerBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mServerBound = false;
        }
    };
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mClientConnection = new ServiceConnection() {

        int msgCount = 0;

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ClientService.LocalBinder binder = (ClientService.LocalBinder) service;
            mClientService = binder.getService();
            mClientService.getMessageUpdates().subscribe((msg) -> {
                mReadTxt.setText(++msgCount + "] " + msg);
            });
            mClientBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mClientBound = false;
        }
    };
}
