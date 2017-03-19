package studioes.arm.six.quizletapi20modelretrieval.api;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import studioes.arm.six.quizletapi20modelretrieval.models.ImmutableQSet;
import studioes.arm.six.quizletapi20modelretrieval.models.ImmutableQTerm;
import studioes.arm.six.quizletapi20modelretrieval.models.ImmutableQUser;
import studioes.arm.six.quizletapi20modelretrieval.models.QSet;
import studioes.arm.six.quizletapi20modelretrieval.models.QTerm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by sithel on 3/18/17.
 */

public class ApiClientTest {
    @Test
    public void testBasicSetRequest() {
        ApiClient client = new ApiClient();
        String resp = client.fetchSet(415);
        assertNotNull(resp);
        QSet set = client.convertResponseToQSet(resp);
        QSet expectedSet = new ImmutableQSet.Builder()
                .id(415)
                .title("U.S. State Capitals")
                .url("/415/us-state-capitals-flash-cards/")
                .description("")
                .wordLanguageCode("en")
                .definitionLanguageCode("en")
                .termCount(50)
                .hasImages(false)
                .creatorUsername("asuth")
                .creator(new ImmutableQUser.Builder()
                        .profileImageUrl("https://quizlet.com/fb-pic/scontent.xx.fbcdn.net/v/t1.0-1/310044_10200425626336483_2068709214_n.jpg?oh=459809bf08a3a70f6b0d9d54e4c54e84&oe=593BB801")
                        .id(1)
                        .username("asuth")
                        .accountType("teacher")
                        .build())
                .build();
        List<QTerm> terms = set.terms();
        // don't wanna' mock out all ther terms, lets just check for equals w/o them
        QSet testableSet = new ImmutableQSet.Builder()
                .from(set)
                .terms(new ArrayList<QTerm>())
                .build();
        assertEquals(expectedSet, testableSet);

        assertEquals(50, terms.size());
        QTerm expectedTerm = new ImmutableQTerm.Builder()
                .id(1277349735)
                .word("Alabama")
                .definition("Montgomery")
                .rank(0)
                .build();
        assertEquals(expectedTerm, terms.get(0));
    }
}
