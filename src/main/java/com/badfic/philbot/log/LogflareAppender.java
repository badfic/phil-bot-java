package com.badfic.philbot.log;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class LogflareAppender extends AppenderBase<ILoggingEvent> {

    // Set by logback config properties
    @Getter @Setter private PatternLayoutEncoder encoder;
    @Getter @Setter private String logflareUrl;
    @Getter @Setter private String logflareApiKey;

    private final RestTemplate restTemplate;
    private final BlockingQueue<String> queue;
    private final Thread worker;

    public LogflareAppender() {
        restTemplate = new RestTemplate();
        queue = new ArrayBlockingQueue<>(64);
        worker = Thread.startVirtualThread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String message = queue.take();

                    sendMessage(message);

                    // The rate limit for Logflare is 5 per second
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } catch (Exception ignored) {}
            }
        });
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        try {
            String message;
            while ((message = queue.poll()) != null) {
                sendMessage(message);
            }
        } catch (Exception ignored) {}

        worker.interrupt();

        super.stop();
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        try {
            String message = new String(encoder.encode(iLoggingEvent));
            queue.add(message);
        } catch (Exception ignored) {}
    }

    private void sendMessage(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set("X-API-KEY", logflareApiKey);

        restTemplate.exchange(logflareUrl, HttpMethod.POST, new HttpEntity<>(Map.of("message", message), headers), String.class);
    }
}
