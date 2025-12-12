package org.example.order.order.job;


import lombok.extern.slf4j.Slf4j;
import org.example.order.order.model.OutboxEvent;
import org.example.order.order.model.OutboxStatus;
import org.example.order.order.repository.OutboxEventRepository;
import org.example.order.order.service.OutboxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class OutboxEventProcessor {

    private final OutboxService txService;
    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    private static final int BATCH_SIZE = 50;

    public OutboxEventProcessor(
            OutboxService txService,
            OutboxEventRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${outbox.event.topic:orders}") String topic
    ) {
        this.txService = txService;
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }


    public void processBatch() {
        // processor.recoverStuckProcessing(Duration.ofMinutes(1)); // 可选
        txService.recoverStuckProcessing();
        List<OutboxEvent> events = txService.fetchAndMarkProcessingBatch(BATCH_SIZE);
        if (events.isEmpty()) return;

        List<CompletableFuture<SendResult<String, String>>> futures = events.stream()
                .map(event -> kafkaTemplate.send(topic, String.valueOf(event.getId()), event.getPayload().toString())
                        .handle((result, ex) -> {
                            if (ex != null) {
                                event.setStatus(OutboxStatus.FAILED);
                                log.warn("Failed to send event {}", event.getId(), ex);
                            } else {
                                event.setStatus(OutboxStatus.DONE);
                                event.setProcessedAt(OffsetDateTime.now());
                            }
                            return result;
                        }))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        repository.saveAll(events);
    }

}

