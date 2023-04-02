package edu.hku.cs.fyp.venntrading.tradingbot;

import com.binance.connector.client.impl.WebsocketStreamClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.hku.cs.fyp.venntrading.tradingbot.jacksonMappers.KlineWSMapper;
import edu.hku.cs.fyp.venntrading.tradingbot.responseMapper.IndicatorMapper;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.AbstractRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TradingBot implements Runnable {
    private BarSeries series;
    private String key;
    private String secret;
    private String id;
    private Strategy strategy;
    private Rule entryTry = new IdentityRule();
    private Rule exitTry = new IdentityRule();

    public TradingBot() {}

    public TradingBot(String key, String secret, String id, BarSeries series, Map<String, Integer[]> strategyPayload) {
        this.key = key;
        this.secret = secret;
        this.id = id;
        this.series = series;
        this.strategy = strategyBuilder(strategyPayload);
    }

    private Strategy strategyBuilder(Map<String, Integer[]> strategyPayload) {
        ClosePriceIndicator indicator = new ClosePriceIndicator(series);
        Rule entryRule = new IdentityRule();
        Rule exitRule = new IdentityRule();
        for(Map.Entry<String, Integer[]> entry: strategyPayload.entrySet()) {
            Integer[] parameters = entry.getValue();
            switch(entry.getKey()) {
                case "macd":
                    MACDIndicator macd = new MACDIndicator(indicator, parameters[0], parameters[1]);
                    EMAIndicator emaMacd = new EMAIndicator(macd, parameters[2]);
                    entryRule = entryRule.and(new OverIndicatorRule(macd, emaMacd));
                    exitRule = exitRule.and(new UnderIndicatorRule(macd, emaMacd));
                    break;
                case "rsi":
                    RSIIndicator rsi = new RSIIndicator(indicator, 14);
                    entryRule = entryRule.and(new UnderIndicatorRule(rsi, parameters[0]));
                    exitRule = exitRule.and(new OverIndicatorRule(rsi, parameters[1]));
                    break;
            }
        }
        entryTry = entryRule;
        exitTry = exitRule;
        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public void run() {
        int endIndex = series.getEndIndex();

        System.out.println("end index: " + endIndex);
        ClosePriceIndicator indicator = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(indicator, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
        Rule dummy = new IdentityRule().and(new OverIndicatorRule(macd, emaMacd));
        System.out.println("trace: " + dummy.isSatisfied(endIndex));
        System.out.println("trace entry: " + entryTry.isSatisfied(endIndex));
        System.out.println("trace exit: " + exitTry.isSatisfied(endIndex));

        if (strategy.shouldEnter(endIndex)) {
            // Entering...
            System.out.println("enter trade");
        } else if (strategy.shouldExit(endIndex)) {
            // Exiting...
            System.out.println("exit trade");
        }

    }
}



//public class TradingBot implements Runnable {
//    String key;
//    String secret;
//    boolean newInterval = false;
//    Thread currentThread;
//    String id;
//
//
//    public TradingBot() {
//
//    }
//
//    public TradingBot(String key, String secret, String id) {
//        this.key = key;
//        this.secret = secret;
//        this.id = id;
//    }
//
//    private void binanceConnector(String key, String secret) {
//    }
//
//    @Override
//    public synchronized void run() {
//        currentThread = Thread.currentThread();
//        while(true) {
//            try {
//                while(!newInterval) {
//                    wait();
//                }
//                System.out.println("trading bot " + id + " executed trade");
//                newInterval = false;
//            } catch (InterruptedException e) {
//                return;
//            }
//        }
//    }
//
//    public synchronized void wakeUpThread() {
//        newInterval = true;
//        notify();
//    }
//
//    public synchronized void terminateThread() {
//        currentThread.interrupt();
//    }
//}

class IdentityRule extends AbstractRule {
    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        return true;
    }
}