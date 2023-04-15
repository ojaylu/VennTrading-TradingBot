package edu.hku.cs.fyp.venntrading.tradingbot;

import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.AbstractRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.Map;

public class StrategyBuilder {
    public static Strategy strategyBuilder(Map<String, Map<String, Integer>> strategyPayload, BarSeries series) {
        ClosePriceIndicator indicator = new ClosePriceIndicator(series);
        Rule entryRule = new IdentityRule();
        Rule exitRule = new IdentityRule();
        for (Map.Entry<String, Map<String, Integer>> entry : strategyPayload.entrySet()) {
            Map<String, Integer> parameters = entry.getValue();
            switch (entry.getKey()) {
                case "MACD":
                    MACDIndicator macd = new MACDIndicator(indicator, parameters.get("short"), parameters.get("long"));
                    EMAIndicator emaMacd = new EMAIndicator(macd, parameters.get("ema"));
                    entryRule = entryRule.and(new OverIndicatorRule(macd, emaMacd));
                    exitRule = exitRule.and(new UnderIndicatorRule(macd, emaMacd));
                    break;
                case "RSI":
                    RSIIndicator rsi = new RSIIndicator(indicator, 14);
                    entryRule = entryRule.and(new UnderIndicatorRule(rsi, parameters.get("lower")));
                    exitRule = exitRule.and(new OverIndicatorRule(rsi, parameters.get("upper")));
                    break;
                case "PSAR":
                    ParabolicSarIndicator psar = new ParabolicSarIndicator(series);
                    entryRule = entryRule.and(new UnderIndicatorRule(psar, indicator));
                    exitRule = exitRule.and(new OverIndicatorRule(psar, indicator));
                case "EMA":
                    EMAIndicator ema = new EMAIndicator(indicator, parameters.get("period"));
                    entryRule = entryRule.and(new UnderIndicatorRule(ema, indicator));
                    exitRule = exitRule.and(new OverIndicatorRule(ema, indicator));
            }
        }
        return new BaseStrategy(entryRule, exitRule);
    }
}

class IdentityRule extends AbstractRule {
    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        return true;
    }
}