package com.example.subscriptionservice.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendSubscriptionCreatedNotification(String adminEmail, String subscriptionName);
    void sendSubscriptionUpdatedNotification(String adminEmail, String subscriptionName);
    void sendSubscriptionActivatedNotification(String adminEmail, String subscriptionName);
    void sendSubscriptionDeactivatedNotification(String adminEmail, String subscriptionName);
}
