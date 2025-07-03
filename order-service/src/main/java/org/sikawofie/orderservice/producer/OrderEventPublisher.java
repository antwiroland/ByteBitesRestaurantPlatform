package org.sikawofie.orderservice.producer;

import lombok.RequiredArgsConstructor;
import org.sikawofie.orderservice.events.OrderPlacedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void publish(OrderPlacedEvent event) {
        kafkaTemplate.send("order-placed-topic", event);
    }
}
