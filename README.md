# Order Platform Demo

A learning project for Kafka event-driven microservices
Using Outbox Pattern to ensure reliable event delivery and idempotent processing.

## Architecture
      Order Created
             |
             v
      Order Service
             |
             | write to Outbox (DB)
             v
      PostgreSQL
             |
             | Outbox Event Publisher Job
             v
      Kafka Topic ("orders")
             |
             | consume event (idempotent)
             v
      Processor Service


## Run the Project
      1. Start infrastructure (Kafka + PostgreSQL)
         docker compose up -d
      
      2. Start services in IntelliJ
      
         Run order-service
            
         Run processor-service

## Test the API

      Send request:
      
      POST http://localhost:8080/orders
      
      
      Example JSON:
      
      {
        "userId": "user123",
        "productId": "product456",
        "amount": 100
      }
      
      
      Expected flow:
      
      Order inserted into database
      
      Outbox event stored
      
      Event published to Kafka
      
      Processor consumes and logs handling result
