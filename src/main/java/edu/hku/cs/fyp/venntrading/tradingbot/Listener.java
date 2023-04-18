package edu.hku.cs.fyp.venntrading.tradingbot;

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.WebsocketStreamClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.hku.cs.fyp.venntrading.tradingbot.jacksonMappers.KlineData;
import edu.hku.cs.fyp.venntrading.tradingbot.jacksonMappers.KlineWSMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Service
@Slf4j
public class Listener {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TradingBotMapper tradingBotMapper;

    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

//    @Autowired
//    Locks locks;

    private ConcurrentHashMap<String, BarSeries> bars;
    private ConcurrentHashMap<String, KlineData> lastMsgs;
    private ConcurrentHashMap<String, Long> timestamps;
    private static final int DAYS = 400;

    public Listener() {
        bars = new ConcurrentHashMap<>();
        lastMsgs = new ConcurrentHashMap<>();
        timestamps = new ConcurrentHashMap<>();
    }

    @Async
    public void addBar(String symbol, String interval, CountDownLatch counter) throws JsonProcessingException {
        String name = "" + symbol + "_" + interval;
        BarSeries series = new BaseBarSeries(name);
        series.setMaximumBarCount(DAYS);
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        SpotClientImpl client = new SpotClientImpl();

        parameters.put("symbol", symbol);
        parameters.put("interval", interval);
        parameters.put("limit", DAYS);

        String result = client.createMarket().klines(parameters);

        ArrayNode arrayNode = objectMapper.readValue(result, ArrayNode.class);
        int size = arrayNode.size();
        for (int i = 0; i < size; i++) {
            JsonNode node = arrayNode.get(i);
            long openTimeMillis = node.get(0).asLong();
            String openPrice = node.get(1).asText();
            String highPrice = node.get(2).asText();
            String lowPrice = node.get(3).asText();
            String closePrice = node.get(4).asText();
            String volume = node.get(5).asText();
            long closeTimeMillis = node.get(6).asLong();
            ZonedDateTime closeTime = Utils.longToZonedDateTime(closeTimeMillis);
            // long noOfTrades = node.get(8).asLong();
            if (i != size - 1)
                series.addBar(closeTime, openPrice, highPrice, lowPrice, closePrice, volume);
            else {
                timestamps.put(name, closeTimeMillis); // get latest timestamp
                lastMsgs.put(name, new KlineData(
                        openTimeMillis,
                        closeTimeMillis,
                        symbol,
                        interval,
                        openPrice,
                        closePrice,
                        highPrice,
                        lowPrice,
                        volume
                ));
            }
        }

        bars.put(name, series);
        log.trace("finish putting in bar");
        counter.countDown();
    }

    @Async
    public void addListener(String symbol, String interval, CountDownLatch counter) {
        WebsocketStreamClientImpl wsStreamClient = new WebsocketStreamClientImpl(); // defaults to live exchange unless stated.
        int streamID = wsStreamClient.klineStream(symbol, interval, (event) -> {
            KlineWSMapper eventObject = null;
            try {
                eventObject = objectMapper.readValue(event, KlineWSMapper.class);
            } catch (JsonProcessingException e) {
                System.out.println(e);
            }
            assert eventObject != null;
            String name = Utils.getStreamName(symbol, interval);
            long timestamp = eventObject.klineData().closeTime();
            long lastTimestamp = this.getTimestamp(name);
            if (timestamp != lastTimestamp) { // new timestamp
                KlineData data = this.getLastMsg(name);
                this.setBar(name, data);
                // locks.notifyLocks(name);
                this.setTimestamp(name, timestamp);
                for(Map.Entry<String, TradingBot> bot: tradingBotMapper.getBots(name).entrySet()) {
                    threadPoolTaskExecutor.execute(bot.getValue());
                }
            }
            this.setLastMsg(name, eventObject.klineData());
        });
        counter.countDown();
    }

    public ConcurrentHashMap<String, BarSeries> getBars() {
        return bars;
    }

    public BarSeries getBar(String name) {
        return bars.get(name);
    }

    public void setBar(String name, KlineData data) {
        BarSeries series = bars.get(name);
        synchronized(series) {
            series.addBar(
                    Utils.longToZonedDateTime(data.closeTime()),
                    data.openPrice(),
                    data.highPrice(),
                    data.lowPrice(),
                    data.closePrice(),
                    data.volume()
            );
        }
    }

    public Long getTimestamp(String name) {
        return timestamps.get(name);
    }

    public void setTimestamp(String name, Long timestamp) {
        timestamps.replace(name, timestamp);
    }

    public KlineData getLastMsg(String name) {
        return lastMsgs.get(name);
    }

    public void setLastMsg(String name, KlineData data) {
        lastMsgs.put(name, data);
    }
}
