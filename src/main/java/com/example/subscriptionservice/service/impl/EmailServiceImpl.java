package com.example.subscriptionservice.service.impl;

import com.example.subscriptionservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${email.template.subscription.created}")
    private String createdSubject;

    @Value("${email.template.subscription.updated}")
    private String updatedSubject;

    @Value("${email.template.subscription.activated}")
    private String activatedSubject;

    @Value("${email.template.subscription.deactivated}")
    private String deactivatedSubject;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email sending error: " + e.getMessage());
        }
    }

    @Override
    public void sendSubscriptionCreatedNotification(String adminEmail, String subscriptionName) {
        String body = String.format(
                "Dear Admin,\n\n" +
                        "A new subscription has been created:\n\n" +
                        "Subscription name: %s\n" +
                        "Creation date: %s\n\n" +
                        "You can view the details by accessing the system admin panel.\n\n" +
                        "Best regards,\n" +
                        "System",
                subscriptionName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        );

        sendEmail(adminEmail, createdSubject, body);
    }

    @Override
    public void sendSubscriptionUpdatedNotification(String adminEmail, String subscriptionName) {
        String body = String.format(
                "Dear Admin,\n\n" +
                        "Subscription information has been updated:\n\n" +
                        "Subscription name: %s\n" +
                        "Update date: %s\n\n" +
                        "You can view the changes in the admin panel.\n\n" +
                        "Best regards,\n" +
                        "System",
                subscriptionName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        );

        sendEmail(adminEmail, updatedSubject, body);
    }

    @Override
    public void sendSubscriptionActivatedNotification(String adminEmail, String subscriptionName) {
        String body = String.format(
                "Dear Admin,\n\n" +
                        "Subscription has been activated:\n\n" +
                        "Subscription name: %s\n" +
                        "Activation date: %s\n\n" +
                        "The subscription will now be available to users.\n\n" +
                        "Best regards,\n" +
                        "System",
                subscriptionName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        );

        sendEmail(adminEmail, activatedSubject, body);
    }

    @Override
    public void sendSubscriptionDeactivatedNotification(String adminEmail, String subscriptionName) {
        String body = String.format(
                "Dear Admin,\n\n" +
                        "Subscription has been deactivated:\n\n" +
                        "Subscription name: %s\n" +
                        "Deactivation date: %s\n\n" +
                        "The subscription will no longer be available to users.\n\n" +
                        "Best regards,\n" +
                        "System",
                subscriptionName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        );

        sendEmail(adminEmail, deactivatedSubject, body);
    }
}