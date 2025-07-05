package org.sikawofie.notificationservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sikawofie.notificationservice.event.OrderPlacedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    @KafkaListener(topics = "order-placed-topic", containerFactory = "kafkaListenerContainerFactory")
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("📥 Received order event: Order ID={}, Customer ID={}, Status={}",
                event.getOrderId(), event.getCustomerId(), event.getStatus());
        // emailService.sendOrderNotification(event, event.getEmail());
    }
}