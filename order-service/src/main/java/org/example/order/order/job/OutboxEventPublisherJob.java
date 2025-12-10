package org.example.order.order.job;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.order.model.OutboxEvent;
import org.example.order.order.model.OutboxStatus;
import org.example.order.order.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisherJob {

    private final OutboxEventRepository outboxEventRepository;

    private static final String TOPIC = "orders";
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRY = 5;

    @Scheduled(fixedDelay = 5000)
    public void publish() {
        this.processOutbox();
    }

    void processOutbox() {

        List<CompletableFuture<SendResult<String, String>>> futures =
                outboxEventRepository.fetchNewEvents(BATCH_SIZE)
                        .stream()
                        .map(this::process)
                        .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    }

    private CompletableFuture
            <SendResult<String, String>> process(OutboxEvent outboxRecord) {

        String key = String.valueOf(outboxRecord.getId());
        String payload = outboxRecord.getPayload().toString();

        return
                kafkaTemplate.send(TOPIC, key, payload)
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                log.info("Sent record to Kafka: {}", outboxRecord);
                                outboxRecord.setProcessedAt(OffsetDateTime.now());
                                outboxRecord.setStatus(OutboxStatus.DONE);
                                outboxEventRepository.save(outboxRecord);
                            } else {
                                log.warn("Failed to publish {}", outboxRecord, ex);
                            }
                        });
    }

}

