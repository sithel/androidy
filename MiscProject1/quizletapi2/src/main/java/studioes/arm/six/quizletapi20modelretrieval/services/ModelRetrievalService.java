package studioes.arm.six.quizletapi20modelretrieval.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import io.reactivex.Flowable;
import studioes.arm.six.quizletapi20modelretrieval.api.ApiClient;
import studioes.arm.six.quizletapi20modelretrieval.models.QSet;

/**
 * Your one-stop-shop for all data needs. You provide us your API key/credential info and we do
 * the rest.
 * <p>
 * This class is supposed to spin up as needed and linger for a while before shutting down when
 * unused after X period of time. Right now there's no backing DB storage, but eventually there
 * will be... and caching too!
 */
public class ModelRetrievalService extends Service implements IModelRetrievalService {
    public static final String TAG = ModelRetrievalService.class.getSimpleName();
    private static final String CLIENT_ID_ARG = "clientIdArg";
    private final ApiClient mClient = new ApiClient();
    private final IBinder mBinder = new LocalBinder();

    public static Intent startIntent(Context context, String fakeClientId) {
        Intent intent = new Intent(context, ModelRetrievalService.class);
        intent.putExtra(CLIENT_ID_ARG, fakeClientId);
        return intent;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public IModelRetrievalService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ModelRetrievalService.this;
        }
    }

    @Override public void onCreate() {
        super.onCreate();
        Log.i(TAG, Thread.currentThread().getName() + "] I see us trying to create... lets only do this once, ok?");
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.i(TAG, Thread.currentThread().getName() + "] I have a null intent... maybe I should seriously think about shutting down? (flags ["+flags+"], startId ["+startId+"])");
            return START_STICKY;
        }
        Log.i(TAG, Thread.currentThread().getName() + "] I see us trying to start w/ client id (flags ["+flags+"], startId ["+startId+"]) : " + intent.getStringExtra(CLIENT_ID_ARG));
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see an onBind request : "+intent);
        return mBinder;
    }

    @Override public boolean onUnbind(Intent intent) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see an onUNbind request : "+intent);
//        return super.onUnbind(intent);
        return true;
    }

    @Override public void onRebind(Intent intent) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see an onREbind request : "+intent);
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "ModelRetrievalService done", Toast.LENGTH_SHORT).show();
    }

    //region interface methods

    @Override public Flowable<QSet> getQSetFlowable() {
        // in theory I would own this observable and select between my cache and the API to find it
        return mClient.getQSetFlowable();
    }

    @Override public void requestSet(final long setId) {
        Runnable runnable = new Runnable() {
            @Override public void run() {
                mClient.fetchSet(setId);
            }
        };
        new Thread(runnable).start();

    }

    //endregion
}
