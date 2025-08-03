package com.example.subscriptionservice.service;

import com.example.subscriptionservice.dto.SubscriptionRequest;
import com.example.subscriptionservice.dto.SubscriptionResponse;
import com.example.subscriptionservice.entity.Subscription;
import com.example.subscriptionservice.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    // User/Public methods (existing functionality)
    public List<SubscriptionResponse> getAllSubscriptions() {
        logger.info("Getting all active subscriptions");

        List<Subscription> subscriptions = subscriptionRepository.findByIsActive(true);
        List<SubscriptionResponse> response = subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        logger.info("Retrieved {} subscriptions", response.size());

        return response;
    }

    public SubscriptionResponse getSubscriptionById(Long id) {
        logger.info("Getting active subscription by ID: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> {
                    logger.warn("Active subscription not found: {}", id);
                    return new RuntimeException("Subscription not found");
                });

        SubscriptionResponse response = mapToResponse(subscription);

        return response;
    }

    public List<SubscriptionResponse> getSubscriptionsByCategory(String category) {
        logger.info("Getting active subscriptions by category: {}", category);

        List<Subscription> subscriptions = subscriptionRepository.findByCategoryAndIsActive(category, true);
        List<SubscriptionResponse> response = subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return response;
    }

    public List<String> getAllCategories() {
        logger.info("Getting all active categories");

        List<String> categories = subscriptionRepository.findAllActiveCategories();

        return categories;
    }

    public List<SubscriptionResponse> searchSubscriptions(String name) {
        logger.info("Searching active subscriptions by name: {}", name);

        List<Subscription> subscriptions = subscriptionRepository
                .findByNameContainingIgnoreCaseAndIsActive(name);

        return subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Admin methods
    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        logger.info("Admin creating new subscription: {}", request.getName());

        if (subscriptionRepository.existsByName(request.getName())) {
            logger.warn("Subscription already exists: {}", request.getName());
            throw new RuntimeException("Subscription with this name already exists");
        }

        Subscription subscription = new Subscription(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getCurrency(),
                request.getCategory(),
                request.getBillingPeriod()
        );

        subscription.setWebsiteUrl(request.getWebsiteUrl());
        subscription.setLogoUrl(request.getLogoUrl());

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        logger.info("Successfully created subscription with ID: {}", savedSubscription.getId());

        return mapToResponse(savedSubscription);
    }

    public Page<SubscriptionResponse> getAllSubscriptionsForAdmin(Pageable pageable, Boolean isActive) {
        logger.info("Admin getting all subscriptions with pagination, isActive filter: {}", isActive);

        Page<Subscription> subscriptions;
        if (isActive != null) {
            subscriptions = subscriptionRepository.findByIsActive(isActive, pageable);
        } else {
            subscriptions = subscriptionRepository.findAll(pageable);
        }

        return subscriptions.map(this::mapToResponse);
    }

    public SubscriptionResponse getSubscriptionByIdForAdmin(Long id) {
        logger.info("Admin getting subscription by ID: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Subscription not found for admin: {}", id);
                    return new RuntimeException("Subscription not found");
                });

        return mapToResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse updateSubscription(Long id, SubscriptionRequest request) {
        logger.info("Admin updating subscription: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Check if name is being changed and if new name already exists
        if (!subscription.getName().equals(request.getName()) &&
                subscriptionRepository.existsByName(request.getName())) {
            throw new RuntimeException("Subscription with this name already exists");
        }

        subscription.setName(request.getName());
        subscription.setDescription(request.getDescription());
        subscription.setPrice(request.getPrice());
        subscription.setCurrency(request.getCurrency());
        subscription.setCategory(request.getCategory());
        subscription.setBillingPeriod(request.getBillingPeriod());
        subscription.setWebsiteUrl(request.getWebsiteUrl());
        subscription.setLogoUrl(request.getLogoUrl());

        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        logger.info("Successfully updated subscription: {}", id);

        return mapToResponse(updatedSubscription);
    }

    @Transactional
    public void deleteSubscription(Long id) {
        logger.info("Admin deleting subscription: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);

        logger.info("Successfully deleted subscription: {}", id);
    }

    @Transactional
    public SubscriptionResponse activateSubscription(Long id) {
        logger.info("Admin activating subscription: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setIsActive(true);
        Subscription activatedSubscription = subscriptionRepository.save(subscription);

        logger.info("Successfully activated subscription: {}", id);

        return mapToResponse(activatedSubscription);
    }

    public Page<SubscriptionResponse> searchSubscriptionsForAdmin(String searchTerm, Pageable pageable) {
        logger.info("Admin searching subscriptions with term: {}", searchTerm);

        return subscriptionRepository.searchSubscriptions(searchTerm, pageable)
                .map(this::mapToResponse);
    }

    public Map<String, Object> getSubscriptionStatistics() {
        logger.info("Admin getting subscription statistics");

        Map<String, Object> statistics = new HashMap<>();

        try {
            Long activeSubscriptions = subscriptionRepository.countActiveSubscriptions();
            Long inactiveSubscriptions = subscriptionRepository.countInactiveSubscriptions();
            Long activeCategories = subscriptionRepository.countActiveCategories();
            List<Object[]> categoryStats = subscriptionRepository.getSubscriptionCountByCategory();

            statistics.put("activeSubscriptions", activeSubscriptions != null ? activeSubscriptions : 0L);
            statistics.put("inactiveSubscriptions", inactiveSubscriptions != null ? inactiveSubscriptions : 0L);
            statistics.put("totalSubscriptions", (activeSubscriptions != null ? activeSubscriptions : 0L) +
                    (inactiveSubscriptions != null ? inactiveSubscriptions : 0L));
            statistics.put("activeCategories", activeCategories != null ? activeCategories : 0L);

            // Convert category statistics
            Map<String, Long> categoryCount = new HashMap<>();
            for (Object[] row : categoryStats) {
                categoryCount.put((String) row[0], ((Number) row[1]).longValue());
            }
            statistics.put("subscriptionsByCategory", categoryCount);

            logger.info("Generated subscription statistics: Active={}, Total Categories={}",
                    activeSubscriptions, activeCategories);

        } catch (Exception e) {
            logger.error("Failed to generate statistics", e);
            throw new RuntimeException("Failed to generate statistics");
        }

        return statistics;
    }

    // Helper methods
    private SubscriptionResponse mapToResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getName(),
                subscription.getDescription(),
                subscription.getPrice(),
                subscription.getCurrency(),
                subscription.getCategory(),
                subscription.getBillingPeriod(),
                subscription.getWebsiteUrl(),
                subscription.getLogoUrl(),
                subscription.getIsActive()
        );
    }
}