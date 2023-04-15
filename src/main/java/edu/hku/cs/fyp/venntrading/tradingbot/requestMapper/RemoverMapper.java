package edu.hku.cs.fyp.venntrading.tradingbot.requestMapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoverMapper(String symbol, String interval) {
}
