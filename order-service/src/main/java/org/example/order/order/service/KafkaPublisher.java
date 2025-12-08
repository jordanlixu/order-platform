package org.example.order.order.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.order.order.model.OutboxEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class KafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Executor executor = Executors.newFixedThreadPool(8);
    private static final String TOPIC = "orders";
    private final ObjectMapper objectMapper;

    public CompletableFuture<Void> publishAsync(OutboxEvent event) {
        String key = String.valueOf(event.getId());
        // Serialize the JsonNode to a String
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event.getPayload());
        } catch (Exception e) {
            // If serialization fails, return a failed CompletableFuture
            return CompletableFuture.failedFuture(e);
        }

        return CompletableFuture.runAsync(() -> kafkaTemplate.send(TOPIC, key, payload), executor)
                .thenAccept(v -> {
                    // success log
                })
                .exceptionally(ex -> {
                    throw new RuntimeException(ex);
                });
    }
}

