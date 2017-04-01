package studioes.arm.six.quizletapi2.api;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import studioes.arm.six.quizletapi2.models.QSet;

/**
 * This is the Service-private portion that actually makes the Quizlset specific calls and
 * transforms the JSON results into our usable Q* immutables
 */

public class ApiClient {
    public static final String TAG = ApiClient.class.getSimpleName();
    private static final String SET_URL = "https://api.quizlet.com/2.0/sets/%d?client_id=BcpDSe7sYr";
    private OkHttpClient client = new OkHttpClient();
    private final BehaviorProcessor<QSet> mQSetProcessor = BehaviorProcessor.create();

    public String fetchSet(long setId)  {
        Request request = new Request.Builder()
                .url(String.format(Locale.ENGLISH, SET_URL, setId))
                .build();
        try {
            try (Response response = client.newCall(request).execute()) {
                Log.v(TAG, "Response from server : "+response);
                if (response != null && response.body() != null) {
                    String jsonResponse = response.body().string();
                    QSet qset = convertResponseToQSet(jsonResponse);
                    mQSetProcessor.onNext(qset);
                } else {
                    Log.e(TAG, "Error encountered trying to load set "+setId+" : "+response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

     QSet convertResponseToQSet(String response) {
        try {
            return new ObjectMapper().readValue(response, QSet.class);
        } catch (IOException e) {
            Log.e(TAG, "Failed to convert response to QSet "+response);
            e.printStackTrace();
        }
        return null;
    }

    public Flowable<QSet> getQSetFlowable() {
        return mQSetProcessor;
    }
}
