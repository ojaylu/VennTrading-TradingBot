package edu.hku.cs.fyp.venntrading.tradingbot;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;

import java.util.Map;

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

    public TradingBot(String key, String secret, String id, BarSeries series, Map<String, Map<String, Integer>> strategyPayload) {
        this.key = key;
        this.secret = secret;
        this.id = id;
        this.series = series;
        this.strategy = StrategyBuilder.strategyBuilder(strategyPayload, series);
    }

    @Override
    public void run() {
        int endIndex = series.getEndIndex();

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