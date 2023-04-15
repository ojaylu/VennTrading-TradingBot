package edu.hku.cs.fyp.venntrading.tradingbot.requestMapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BacktestingMapper(
        String symbol,
        String interval,
        Map<String, Map<String, Integer>> strategy,
        Double quantity
) {
}
