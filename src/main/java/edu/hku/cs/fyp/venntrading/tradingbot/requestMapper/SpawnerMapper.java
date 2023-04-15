package edu.hku.cs.fyp.venntrading.tradingbot.requestMapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpawnerMapper(
        String key, String secret, String symbol, String interval, Map<String, Map<String, Integer>> strategy
) {}