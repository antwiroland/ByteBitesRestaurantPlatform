package org.sikawofie.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sikawofie.notificationservice.event.OrderPlacedEvent;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOrderNotification(OrderPlacedEvent event, String recipient) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipient);
            helper.setSubject("🍽 New Order Placed!");
            helper.setText(buildEmailContent(event), true);

            mailSender.send(message);

            log.info("✅ Email sent to {}", recipient);
        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }

    private String buildEmailContent(OrderPlacedEvent event) {
        return """
            <h2>📦 New Order Received!</h2>
            <p><b>Order ID:</b> %d</p>
            <p><b>Customer ID:</b> %d</p>
            <p><b>Restaurant ID:</b> %d</p>
            <p><b>Status:</b> %s</p>
            <hr/>
            <p>Thank you for using ByteBites!</p>
        """.formatted(event.getOrderId(), event.getCustomerId(), event.getRestaurantId(), event.getStatus());
    }
}
