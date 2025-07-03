package org.sikawofie.notificationservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.sikawofie.notificationservice.dto.OrderPlacedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationListener {

    @KafkaListener(topics = "order-placed-topic", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("🔔 Sending notification: Order #{} placed by customer {} for restaurant {}",
                event.getOrderId(), event.getCustomerId(), event.getRestaurantId());

        // Simulate email/push notification
        System.out.println("📧 Email sent to restaurant/customer for Order #" + event.getOrderId());
    }
}
