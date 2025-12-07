package org.example.order.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.processor.model.ProcessedEvent;
import org.example.order.processor.repository.ProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotentProcessorService {

    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void process(String eventId, String payload) {
        // Idempotency check
        if (processedEventRepository.existsById(Long.parseLong(eventId))) {
            log.info("Skipping duplicate event: {}", eventId);
            return;
        }

        // Execute business logic
        log.info("Processing event: {}", eventId);
        executeBusinessLogic(payload);

        // Record event ID to ensure it is not processed again
        processedEventRepository.save(
                ProcessedEvent.builder()
                        .eventId(Long.valueOf(eventId))
                        .createdAt(OffsetDateTime.now().toLocalDateTime())
                        .build()
        );
    }

    private void executeBusinessLogic(String payload) {
        // TODO: Implement your business logic here
    }

}
