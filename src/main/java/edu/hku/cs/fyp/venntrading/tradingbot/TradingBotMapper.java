package edu.hku.cs.fyp.venntrading.tradingbot;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TradingBotMapper {
    private ConcurrentHashMap<String, ConcurrentHashMap<String, TradingBot>> map;

    public TradingBotMapper() {
        this.map = new ConcurrentHashMap<String, ConcurrentHashMap<String, TradingBot>>();
    }

    public void addStreamName(String streamName) {
        map.put(streamName, new ConcurrentHashMap<String, TradingBot>());
    }

    public void addBot(String streamName, String id, TradingBot bot) {
        map.get(streamName).put(id, bot);
    }

    public void removeBot(String streamName, String id) {
        // TradingBot bot = map.get(streamName).get(id);
        // bot.terminateThread();
        map.get(streamName).remove(id);
    }

    public TradingBot getBot(String streamName, String id) {
        return map.get(streamName).remove(id);
    }

    public ConcurrentHashMap<String, TradingBot>  getBots(String streamName) {
        return map.get(streamName);
    }
}
