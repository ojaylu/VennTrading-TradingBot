package edu.hku.cs.fyp.venntrading.tradingbot;

import edu.hku.cs.fyp.venntrading.tradingbot.requestMapper.BacktestingMapper;
import edu.hku.cs.fyp.venntrading.tradingbot.requestMapper.MetricsMapper;
import edu.hku.cs.fyp.venntrading.tradingbot.requestMapper.RemoverMapper;
import edu.hku.cs.fyp.venntrading.tradingbot.requestMapper.SpawnerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.ta4j.core.*;
import org.ta4j.core.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.criteria.pnl.NetLossCriterion;
import org.ta4j.core.criteria.pnl.NetProfitCriterion;
import org.ta4j.core.criteria.pnl.ProfitLossCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class TradingBotController {
    @Autowired
    ThreadPoolTaskExecutor threadManager;

    @Autowired
    Listener listener;

    @Autowired
    TradingBotMapper tradingBotMapper;

    @GetMapping("/hi")
    public String hi() {
        return "hello world";
    }

    @GetMapping("/trial")
    public double[] trial() {
        String dummy = "BTCUSDT_1s";
        ConcurrentHashMap<String, BarSeries> bars = listener.getBars();
        ClosePriceIndicator indicator = new ClosePriceIndicator(bars.get(dummy));
        MACDIndicator macd = new MACDIndicator(indicator);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
        double macdValue = macd.getValue(bars.get(dummy).getEndIndex()).doubleValue();
        double emaMacdValue = emaMacd.getValue(bars.get(dummy).getEndIndex()).doubleValue();
        double array[] = {macdValue, emaMacdValue, macdValue - emaMacdValue};
        return array;
    }

    @PostMapping("/trading-bot/{id}")
    public Map<String, String> spawnTradingBot(@PathVariable String id, @RequestBody SpawnerMapper body) {
        String streamName = Utils.getStreamName(body.symbol(), body.interval());
        TradingBot bot = new TradingBot(body.key(), body.secret(), id, listener.getBar(streamName), body.strategy(), body.quantity(), body.paper());
        tradingBotMapper.addBot(streamName, id, bot);
        Map<String, String> response = new HashMap<>();
        System.out.println("spawned bot: " + id);
        response.put("status", "started bot successfully");
        return response;
    }

    @DeleteMapping("/trading-bot/{id}")
    public Map<String, String> shutdownTradingBot(@PathVariable String id, @RequestBody RemoverMapper body) {
        String streamName = Utils.getStreamName(body.symbol(), body.interval());
        tradingBotMapper.removeBot(streamName, id);
        System.out.println("stopped bot: " + id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "stopped bot successfully");
        return response;
    }

    @GetMapping("/trading-bot/{id}")
    public Map<String, Object> getTradingBotMetrics(@PathVariable String id, @RequestBody MetricsMapper body) {
        String streamName = Utils.getStreamName(body.symbol(), body.interval());
        System.out.println(streamName);
        Map<String, Object> response = new HashMap<>();
        if(tradingBotMapper.hasBot(streamName, id)) {
            response.put("status", "has bot");
            System.out.println("has bot");
            TradingBot bot = tradingBotMapper.getBot(streamName, id);
            response.put("metrics", bot.metrics());
            return response;
        } else {
            response.put("status", "no bot");
            System.out.println("no bot");
            return response;
        }
    }

    @PostMapping("/backtesting")
    public HashMap<String, Double> backtesting(@RequestBody BacktestingMapper body) {
        String streamName = Utils.getStreamName(body.symbol(), body.interval());
        BarSeries series = listener.getBar(streamName);
        // make a copy to avoid situation where series changes in the middle of the analysis
        BarSeries seriesCopy;
        synchronized(series) {
            seriesCopy = series.getSubSeries(series.getBeginIndex(), series.getEndIndex());
        }
        BarSeriesManager seriesManager = new BarSeriesManager(seriesCopy);
        Strategy strategy = StrategyBuilder.strategyBuilder(body.strategy(), seriesCopy);
        TradingRecord tradingRecord = seriesManager.run(strategy, Trade.TradeType.BUY, DecimalNum.valueOf(body.quantity()));
        AnalysisCriterion profitCriterion = new NetProfitCriterion();
        AnalysisCriterion lossCriterion = new NetLossCriterion();
        AnalysisCriterion returnCriterion = new GrossReturnCriterion();

        System.out.println("profit: " +  profitCriterion.calculate(series, tradingRecord).doubleValue());
        System.out.println("loss: " + lossCriterion.calculate(series, tradingRecord).doubleValue());
        System.out.println("return: " + returnCriterion.calculate(series, tradingRecord).doubleValue());
        System.out.println("net: " + new ProfitLossCriterion().calculate(series, tradingRecord).doubleValue());

        HashMap<String, Double> response = new HashMap<>();
        response.put("profit", Utils.criterionToDouble(new NetProfitCriterion(), series, tradingRecord));
        response.put("loss", Utils.criterionToDouble(new NetLossCriterion(), series, tradingRecord));
        response.put("net", Utils.criterionToDouble(new ProfitLossCriterion(), series, tradingRecord));
        response.put("return", (Utils.criterionToDouble(new GrossReturnCriterion(), series, tradingRecord) - 1) * 100);

        return response;
    }
}
