package org.example.order.order.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.example.order.order.model.Order;
import org.example.order.order.model.OutboxEvent;
import org.example.order.order.model.OutboxStatus;
import org.example.order.order.repository.OrderRepository;
import org.example.order.order.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;


    /**
     * Place an order and write the Outbox event in a single transaction.
     */
    @Transactional
    public Order placeOrder(String userId, String productId, Integer amount) {

        // 1️⃣ Save order
        Order order = Order.builder()
                .userId(userId)
                .productId(productId)
                .amount(amount)
                .status("CREATED")
                .createdAt(OffsetDateTime.now().toLocalDateTime())
                .build();
        orderRepository.save(order);


        ObjectNode jsonPayload = objectMapper.createObjectNode();
        jsonPayload.put("orderId", order.getId());
        jsonPayload.put("userId", userId);

        // 2️⃣ Save outbox event
        OutboxEvent event = OutboxEvent.builder()
                .aggregateId(order.getId())
                .eventType("OrderCreated")
                .payload(jsonPayload)
                .status(OutboxStatus.NEW)
                .retryCount(0)
                .createdAt(OffsetDateTime.now())
                .build();
        outboxEventRepository.save(event);

        return order;
    }
}
