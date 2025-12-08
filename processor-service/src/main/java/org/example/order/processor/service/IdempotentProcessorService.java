package org.example.order.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.processor.model.ProcessedEvent;
import org.example.order.processor.repository.ProcessedEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotentProcessorService {

    private final ProcessedEventRepository processedEventRepository;


    private void executeBusinessLogic(String payload) {
        // TODO: Implement your business logic here
    }


    @Transactional
    public void process(String eventId, String payload) {

        try {
            processedEventRepository.save(
                    ProcessedEvent.builder()
                            .eventId(UUID.fromString(eventId))
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate event detected, skipping: {}", eventId);
            return;
        }

        log.info("Processing event: {}", eventId);
        executeBusinessLogic(payload);
    }


}
