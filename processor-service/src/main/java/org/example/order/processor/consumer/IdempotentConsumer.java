package org.example.order.processor.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.order.processor.service.IdempotentProcessorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class IdempotentConsumer {

    private final IdempotentProcessorService idempotentProcessorService;

    @KafkaListener(topics = "orders", groupId = "processor-group")
    public void listen(ConsumerRecord<String, String> record) {
        log.info("Received event: {}", record.value());
        String eventId = record.key(); // 也可以从 header 取
        String payload = record.value();

        if (eventId == null) {
            log.error("Event key missing, cannot ensure idempotency!");
            return;
        }

        idempotentProcessorService.process(eventId, payload);
    }
}
