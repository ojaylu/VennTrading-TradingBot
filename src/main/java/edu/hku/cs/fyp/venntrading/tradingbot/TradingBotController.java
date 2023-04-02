package edu.hku.cs.fyp.venntrading.tradingbot;

import edu.hku.cs.fyp.venntrading.tradingbot.responseMapper.RemoverMapper;
import edu.hku.cs.fyp.venntrading.tradingbot.responseMapper.SpawnerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

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
        return "carrick is gay";
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
    public String spawnTradingBot(@PathVariable String id, @RequestBody SpawnerMapper body) {
        String streamName = Utils.getStreamName(body.symbol(), body.interval());
        TradingBot bot = new TradingBot(body.key(), body.secret(), id, listener.getBar(streamName), body.strategy());
        tradingBotMapper.addBot(streamName, id, bot);
        return "spawned bot successfully";
    }

    @DeleteMapping("/trading-bot/{id}")
    public String shutdownTradingBot(@PathVariable String id, @RequestBody RemoverMapper body) {
        String streamName = Utils.getStreamName(body.symbol(), body.interval());
        tradingBotMapper.removeBot(streamName, id);
        return "shutdown trading bot: " + id;
    }
}
