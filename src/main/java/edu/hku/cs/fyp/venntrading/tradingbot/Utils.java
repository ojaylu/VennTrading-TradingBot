package edu.hku.cs.fyp.venntrading.tradingbot;

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Utils {
    public static ZonedDateTime longToZonedDateTime(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        return instant.atZone(ZoneId.of("Asia/Hong_Kong"));
    }

    public static String getStreamName(String symbol, String interval) {
        return "" + symbol + "_" + interval;
    }

    public static Double criterionToDouble(AnalysisCriterion criterion, BarSeries series, TradingRecord tradingRecord) {
        return criterion.calculate(series, tradingRecord).doubleValue();
    }
}
