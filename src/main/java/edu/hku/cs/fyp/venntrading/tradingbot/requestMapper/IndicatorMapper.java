package edu.hku.cs.fyp.venntrading.tradingbot.requestMapper;

import java.util.List;

public record IndicatorMapper(
        float weighting, List<Float> parameters
) {}