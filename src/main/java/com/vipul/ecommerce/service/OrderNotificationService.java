package com.vipul.ecommerce.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OrderNotificationService {

    @Async
    public void sendOrderConfirmation(Long orderId, String email) {

        System.out.println(
                "Sending order confirmation for order #" +
                        orderId + " to " + email
        );

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println(
                "Order confirmation sent for order #" + orderId
        );
    }
}