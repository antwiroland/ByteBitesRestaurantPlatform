package org.sikawofie.restaurantservice.consumer;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sikawofie.restaurantservice.event.OrderPlacedEvent;
import org.sikawofie.restaurantservice.service.RestaurantService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@AllArgsConstructor
public class RestaurantListener {
    private RestaurantService restaurantService;
    @KafkaListener(topics = "order-placed-topic", containerFactory = "kafkaListenerContainerFactory")
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("📥 Received order event: Order ID={}, Customer ID={}, Status={}",
                event.getOrderId(), event.getCustomerId(), event.getStatus());
    }
}
