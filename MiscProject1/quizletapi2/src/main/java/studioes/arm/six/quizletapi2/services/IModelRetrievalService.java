package studioes.arm.six.quizletapi2.services;

import io.reactivex.Flowable;
import studioes.arm.six.quizletapi2.models.QSet;

/**
 * Created by sithel on 3/19/17.
 */

public interface IModelRetrievalService {
    // in theory there will be more... do I want to have different observables for different types
    // of requests or different objects? ... how do I indicate the results of a search for example?

    /**
     * @return observable that never finishes
     */
    Flowable<QSet> getQSetFlowable();

    /**
     * Kicks off a server request. When the set is loaded, you can find the results over at
     * {@link IModelRetrievalService#getQSetFlowable()}
     */
    void requestSet(long localId);
}
