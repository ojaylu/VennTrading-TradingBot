package edu.hku.cs.fyp.venntrading.tradingbot.jacksonMappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KlineWSMapper(@JsonProperty("e") String eventType, @JsonProperty("E") long eventTime,
                            @JsonProperty("s") String symbol, @JsonProperty("k") KlineData klineData) {
}