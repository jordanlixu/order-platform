package org.example.order.order.loadtest;


import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class OutboxSimulation extends Simulation {

    private static final String[] INSTANCES = {
            "http://localhost:8080/orders",
            "http://localhost:8081/orders",
            "http://localhost:8082/orders"
    };

    private Supplier<Iterator<Map<String, Object>>> orderFeederSupplier(int instanceIndex) {
        return () -> Stream.generate(() -> {
            Map<String, Object> order = new HashMap<>();
            order.put("userId", "user" + ThreadLocalRandom.current().nextInt(1000, 9999));
            order.put("productId", "product" + ThreadLocalRandom.current().nextInt(1000, 9999));
            order.put("amount", 100);
            order.put("instanceUrl", INSTANCES[instanceIndex]);
            return order;
        }).iterator();
    }

    public OutboxSimulation() {

        ScenarioBuilder[] scenarios = new ScenarioBuilder[INSTANCES.length];

        for (int i = 0; i < INSTANCES.length; i++) {

            scenarios[i] = scenario("Outbox Instance " + (i + 1))
                    .feed(orderFeederSupplier(i)) // feed 随机订单 + instanceUrl
                    .repeat(100) // 循环 100 次，每次发一个订单
                    .on(exec(
                            http("Trigger Outbox")
                                    .post("#{instanceUrl}")
                                    .body(StringBody("{\"userId\": \"#{userId}\", \"productId\": \"#{productId}\", \"amount\": #{amount}}"))
                                    .asJson()
                                    .check(status().is(200))
                    ).pause(Duration.ofMillis(10))); // 每次请求间隔 10ms
        }

        // 每个实例单 bean → 每个 scenario 用 5 个用户
        HttpProtocolBuilder httpProtocol = http
                .acceptHeader("application/json")
                .contentTypeHeader("application/json");
        setUp(
                scenarios[0].injectOpen(atOnceUsers(5)),
                scenarios[1].injectOpen(atOnceUsers(5)),
                scenarios[2].injectOpen(atOnceUsers(5))
        ).protocols(httpProtocol);
    }
}
