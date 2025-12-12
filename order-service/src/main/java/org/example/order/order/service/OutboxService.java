package org.example.order.order.service;

import org.example.order.order.model.OutboxEvent;
import org.example.order.order.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutboxService {

    private final OutboxEventRepository repository;

    public OutboxService(OutboxEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<OutboxEvent> fetchAndMarkProcessingBatch(int batchSize) {
        // native FOR UPDATE SKIP LOCKED
        return repository.fetchAndMarkProcessingBatch(batchSize);
    }

    @Transactional
    public void recoverStuckProcessing() {
        repository.markProcessingTimeoutAsFailed();
    }
}
