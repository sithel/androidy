package studioes.arm.six.quizletapi20modelretrieval.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.immutables.value.Value;

/**
 * Created by sithel on 3/18/17.
 */

@Value.Immutable
@Value.Style(builder = "new") // builder has to have constructor
@JsonDeserialize(builder = ImmutableQImage.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface QImage {
    @JsonProperty("url") String url();
    @JsonProperty("width") int width();
    @JsonProperty("height") int height();
}
