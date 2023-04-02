package edu.hku.cs.fyp.venntrading.tradingbot.responseMapper;

import java.util.List;
import java.util.Map;

public record SpawnerMapper(
        String key, String secret, String symbol, String interval, Map<String, Integer[]> strategy
) {}