package studioes.arm.six.quizletapi2.models;

import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.immutables.value.Value;

/**
 * Created by sithel on 3/18/17.
 */

@Value.Immutable
@Value.Style(builder = "new") // builder has to have constructor
@JsonDeserialize(builder = ImmutableQTerm.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface QTerm {
    @JsonProperty("id") long id();
    @JsonProperty("term") String word();
    @JsonProperty("definition") String definition();
    @JsonProperty("rank") int rank();
    @JsonProperty("image") @Nullable QImage image();
}
