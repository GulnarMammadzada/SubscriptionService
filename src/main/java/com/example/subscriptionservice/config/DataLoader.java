package com.example.subscriptionservice.config;

import com.example.subscriptionservice.entity.Subscription;
import com.example.subscriptionservice.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (subscriptionRepository.count() == 0) {
            logger.info("Loading initial subscription data...");
            loadSubscriptionData();
        }
    }

    private void loadSubscriptionData() {
        // Streaming Services
        createSubscription("Netflix", "Video streaming service", new BigDecimal("9.99"), "Streaming", "MONTHLY",
                "https://netflix.com", "https://logo.clearbit.com/netflix.com");

        createSubscription("Spotify", "Music streaming service", new BigDecimal("9.99"), "Music", "MONTHLY",
                "https://spotify.com", "https://logo.clearbit.com/spotify.com");

        createSubscription("YouTube Premium", "Ad-free YouTube and music", new BigDecimal("11.99"), "Streaming", "MONTHLY",
                "https://youtube.com", "https://logo.clearbit.com/youtube.com");

        createSubscription("Amazon Prime", "Shopping and streaming benefits", new BigDecimal("8.99"), "Shopping", "MONTHLY",
                "https://amazon.com", "https://logo.clearbit.com/amazon.com");

        // Gaming Services
        createSubscription("PlayStation Plus", "Gaming subscription", new BigDecimal("9.99"), "Gaming", "MONTHLY",
                "https://playstation.com", "https://logo.clearbit.com/playstation.com");

        createSubscription("Xbox Game Pass", "Gaming subscription", new BigDecimal("14.99"), "Gaming", "MONTHLY",
                "https://xbox.com", "https://logo.clearbit.com/xbox.com");

        // Software Services
        createSubscription("Microsoft 365", "Office suite", new BigDecimal("6.99"), "Software", "MONTHLY",
                "https://microsoft.com", "https://logo.clearbit.com/microsoft.com");

        createSubscription("Adobe Creative Cloud", "Design software suite", new BigDecimal("20.99"), "Software", "MONTHLY",
                "https://adobe.com", "https://logo.clearbit.com/adobe.com");

        // Cloud Storage
        createSubscription("Dropbox", "Cloud storage service", new BigDecimal("9.99"), "Storage", "MONTHLY",
                "https://dropbox.com", "https://logo.clearbit.com/dropbox.com");

        createSubscription("iCloud+", "Apple cloud storage", new BigDecimal("0.99"), "Storage", "MONTHLY",
                "https://apple.com", "https://logo.clearbit.com/apple.com");

        logger.info("Initial subscription data loaded successfully");
    }

    private void createSubscription(String name, String description, BigDecimal price,
                                    String category, String billingPeriod, String websiteUrl, String logoUrl) {
        if (!subscriptionRepository.existsByName(name)) {
            Subscription subscription = new Subscription(name, description, price, "AZN", category, billingPeriod);
            subscription.setWebsiteUrl(websiteUrl);
            subscription.setLogoUrl(logoUrl);
            subscriptionRepository.save(subscription);
            logger.debug("Created subscription: {}", name);
        }
    }
}