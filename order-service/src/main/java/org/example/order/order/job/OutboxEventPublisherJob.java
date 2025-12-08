package org.example.order.order.job;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.order.model.OutboxEvent;
import org.example.order.order.model.OutboxStatus;
import org.example.order.order.repository.OutboxEventRepository;
import org.example.order.order.service.KafkaPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisherJob {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaPublisher kafkaPublisher;

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRY = 5;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publish() {
        // 1) atomic fetch & mark
        /*
         * Alternative:
         * @Transactional
         * public List<OutboxEvent> fetchAndMarkProcessing(int batchSize) {
         *    var ids = repo.fetchIdsForProcessing(batchSize);
         *    if (ids.isEmpty()) return List.of();
         *    repo.markProcessing(ids);
         *    return repo.findAllById(ids);
         * }
         */
        List<OutboxEvent> events = outboxEventRepository.fetchAndMarkProcessing(BATCH_SIZE);
        if (events == null || events.isEmpty()) return;
        log.info("Fetched {} events", events.size());

        // 2) async publish
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (OutboxEvent e : events) {
            CompletableFuture<Void> f = kafkaPublisher.publishAsync(e)
                    .thenRun(() -> {
                        e.setStatus(OutboxStatus.DONE);
                        e.setProcessedAt(OffsetDateTime.now());
                    })
                    .exceptionally(ex -> {
                        // increment retry and decide state
                        e.setRetryCount(Optional.ofNullable(e.getRetryCount()).orElse(0) + 1);
                        if (e.getRetryCount() >= MAX_RETRY) {
                            e.setStatus(OutboxStatus.FAILED);
                        } else {
                            e.setStatus(OutboxStatus.NEW); // ready for next round
                        }
                        return null;
                    });
            futures.add(f);
        }

        // 3) wait for all async to finish
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 4) batch save statuses in one DB operation
        outboxEventRepository.saveAll(events);
        log.info("Batch updated {} events", events.size());
    }
}

