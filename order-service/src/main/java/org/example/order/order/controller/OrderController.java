package org.example.order.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.order.order.dto.OrderRequest;
import org.example.order.order.model.Order;
import org.example.order.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController  {

    private final OrderService orderService;


    @GetMapping("/hello")
    public String hello() {
        return "Hello, Orders!";
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        Order order = orderService.placeOrder(request.getUserId(), request.getProductId(), request.getAmount());
        return ResponseEntity.ok(order);
    }

}
