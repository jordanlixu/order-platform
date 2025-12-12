package org.example.order.order.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.order.job.OutboxEventProcessor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxProcessorScheduler {

    private final OutboxEventProcessor processor;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    @PostConstruct
    public void start() {
        executor.schedule(this::processLoop, 100, TimeUnit.MILLISECONDS);
    }

    private void processLoop() {
        long start = System.currentTimeMillis();

        try {
            processor.processBatch();
        } catch (Exception e) {
            log.error("Error processing outbox batch", e);
            // error backoff
            executor.schedule(this::processLoop, 1000, TimeUnit.MILLISECONDS);
            return;
        }

        long duration = System.currentTimeMillis() - start;

        long nextDelay = Math.max(50, 200 - duration);
        executor.schedule(this::processLoop, nextDelay, TimeUnit.MILLISECONDS);
    }


    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
