package edu.hku.cs.fyp.venntrading.tradingbot;

import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.criteria.pnl.NetLossCriterion;
import org.ta4j.core.criteria.pnl.NetProfitCriterion;
import org.ta4j.core.criteria.pnl.ProfitLossCriterion;
import org.ta4j.core.num.DecimalNum;

import java.util.HashMap;
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
    private double quantity;
    private boolean buyFlag = true;
    private TradingRecord tradingRecord = new BaseTradingRecord();
    private boolean paper;

    public TradingBot() {}

    public TradingBot(String key, String secret, String id, BarSeries series, Map<String, Map<String, Integer>> strategyPayload, double quantity, boolean paper) {
        this.key = key;
        this.secret = secret;
        this.id = id;
        this.series = series;
        this.strategy = StrategyBuilder.strategyBuilder(strategyPayload, series);
        this.quantity = quantity;
        this.paper = paper;
    }

    public Map<String, Double> metrics() {
        synchronized(tradingRecord) {
            synchronized(series) {
                HashMap<String, Double> response = new HashMap<>();
                response.put("profit", Utils.criterionToDouble(new NetProfitCriterion(), series, tradingRecord));
                response.put("loss", Utils.criterionToDouble(new NetLossCriterion(), series, tradingRecord));
                response.put("net", Utils.criterionToDouble(new ProfitLossCriterion(), series, tradingRecord));
                response.put("return", (Utils.criterionToDouble(new GrossReturnCriterion(), series, tradingRecord) - 1) * 100);
                return response;
            }
        }
    }

    @Override
    public void run() {
        int endIndex = series.getEndIndex();

        if (strategy.shouldEnter(endIndex)) {
            // Entering...
            System.out.println("enter trade");
            if(buyFlag) {
                synchronized(tradingRecord) {
                    synchronized(series) {
                        tradingRecord.enter(endIndex, series.getBar(endIndex).getClosePrice(), DecimalNum.valueOf(quantity));
                    }
                }
                if(!paper) {

                }
                buyFlag = false;
                System.out.println("entered trade");
            }
        } else if (strategy.shouldExit(endIndex)) {
            // Exiting...
            System.out.println("exit trade");
            if(!buyFlag) {
                synchronized(tradingRecord) {
                    synchronized(series) {
                        tradingRecord.exit(endIndex, series.getBar(endIndex).getClosePrice(), DecimalNum.valueOf(quantity));
                    }
                }
                if(!paper) {

                }
                buyFlag = true;
                System.out.println("exited trade");
            }
        }

    }
}