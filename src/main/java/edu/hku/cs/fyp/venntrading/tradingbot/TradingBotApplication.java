package edu.hku.cs.fyp.venntrading.tradingbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@EnableAsync
@Slf4j
public class TradingBotApplication implements AsyncConfigurer, CommandLineRunner {
	@Autowired
	Listener listener;

	@Autowired
	TradingBotMapper tradingBotMapper;

	private final String[] SYMBOLS = {"BTCUSDT"};
	private final String[] INTERVALS = {"1s", "1m", "1w", "1M"};

	@Override
	public void run(String...args) throws JsonProcessingException, InterruptedException {
		CountDownLatch barsCounter = new CountDownLatch(SYMBOLS.length * INTERVALS.length);
		for (String symbol: SYMBOLS) {
			for (String interval: INTERVALS) {
				String name = "" + symbol + "_" + interval;
				listener.addBar(symbol, interval, barsCounter);
				tradingBotMapper.addStreamName(name);
			}
		}
		barsCounter.await();

		CountDownLatch listenerCounter = new CountDownLatch(SYMBOLS.length * INTERVALS.length);
		for (String symbol: SYMBOLS) {
			for (String interval: INTERVALS) {
				String name = "" + symbol + "_" + interval;
				listener.addListener(symbol, interval, listenerCounter);
			}
		}
		listenerCounter.await();

		log.info("finished initialising listeners");
	}

	public static void main(String[] args) {
		SpringApplication.run(TradingBotApplication.class, args);
	}
}
