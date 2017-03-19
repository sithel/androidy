package studioes.arm.six.quizletapi20modelretrieval.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import studioes.arm.six.quizletapi20modelretrieval.models.QSet;

/**
 * Created by sithel on 3/18/17.
 */

public class ApiClient {
    private static final String SET_URL = "https://api.quizlet.com/2.0/sets/%d?client_id=BcpDSe7sYr";
    private OkHttpClient client = new OkHttpClient();
    private final BehaviorProcessor<QSet> mQSetProcessor = BehaviorProcessor.create();

    public String fetchSet(long setId)  {
        Request request = new Request.Builder()
                .url(String.format(Locale.ENGLISH, SET_URL, setId))
                .build();
        try {
            try (Response response = client.newCall(request).execute()) {
                mQSetProcessor.onNext(convertResponseToQSet(response.body().string()));
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
            e.printStackTrace();
        }
        return null;
    }

    public Flowable<QSet> getQSetFlowable() {
        return mQSetProcessor;
    }
}
