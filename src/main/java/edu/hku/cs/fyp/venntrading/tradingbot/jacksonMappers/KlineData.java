package edu.hku.cs.fyp.venntrading.tradingbot.jacksonMappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KlineData(
        @JsonProperty("t") long openTime, @JsonProperty("T") long closeTime,
                        @JsonProperty("s") String symbol, @JsonProperty("i") String interval,
                        @JsonProperty("o") String openPrice, @JsonProperty("c") String closePrice,
                        @JsonProperty("h") String highPrice, @JsonProperty("l") String lowPrice,
                        @JsonProperty("v") String volume
//                        @JsonProperty("n") int numTrades,
//                        @JsonProperty("x") boolean isClosed,
//                        @JsonProperty("q") String quoteVolume,
//                        @JsonProperty("V") String takerBuyVolume,
//                        @JsonProperty("Q") String takerBuyQuoteVolume
) {
    // getters and setters
}