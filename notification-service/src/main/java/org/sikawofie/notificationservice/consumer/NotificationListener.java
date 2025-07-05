package org.sikawofie.notificationservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sikawofie.notificationservice.event.OrderPlacedEvent;
import org.sikawofie.notificationservice.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final EmailService emailService;

    @KafkaListener(topics = "order-placed-topic", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("📥 Received order event: {}", event);

        String recipient = "rolandantwisenior47@gmail.com";

        emailService.sendOrderNotification(event, recipient);
    }
}