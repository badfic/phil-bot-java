package com.badfic.philbot.log;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

@Component
@NoArgsConstructor
@Getter
@Setter
public class LogflareAppender extends AppenderBase<ILoggingEvent> {

    // Set by Spring Autowiring
    private static RestTemplate REST_TEMPLATE;
    private static ObjectMapper OBJECT_MAPPER;

    // Set by config properties
    private PatternLayoutEncoder encoder;
    private String logflareUrl;
    private String logflareApiKey;

    // Set on start
    private BlockingQueue<String> queue;
    private Thread worker;
    private volatile boolean workerStopped;

    @Autowired
    public LogflareAppender(RestTemplate restTemplate, ObjectMapper objectMapper) {
        REST_TEMPLATE = restTemplate;
        OBJECT_MAPPER = objectMapper;
    }

    @Override
    public void start() {
        queue = new ArrayBlockingQueue<>(256);
        worker = new Thread(() -> {
            while (!workerStopped && !Thread.currentThread().isInterrupted()) {
                try {
                    if (REST_TEMPLATE != null) {
                        String message = queue.poll();

                        if (message != null) {
                            sendMessage(message);
                        }
                    }

                    // The rate limit for Logflare is 5 per second
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
                } catch (Exception ignored) {}
            }
        });
        worker.start();
        super.start();
    }

    @Override
    public void stop() {
        workerStopped = true;
        worker.interrupt();

        try {
            worker.join(1000);
        } catch (Exception ignored) {}

        try {
            String message;
            while ((message = queue.poll()) != null) {
                sendMessage(message);
            }
        } catch (Exception ignored) {}

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
        REST_TEMPLATE.execute(logflareUrl, HttpMethod.POST,
                request -> {
                    request.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    request.getHeaders().set("X-API-KEY", logflareApiKey);

                    request.getBody().write(OBJECT_MAPPER.writeValueAsBytes(Map.of("message", message)));
                }, (ResponseExtractor<String>) response -> null);
    }
}
