package com.example.subscriptionservice.service;

import com.example.subscriptionservice.dto.SubscriptionRequest;
import com.example.subscriptionservice.dto.SubscriptionResponse;
import com.example.subscriptionservice.entity.Subscription;
import com.example.subscriptionservice.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SUBSCRIPTION_CACHE_PREFIX = "subscription:";
    private static final String SUBSCRIPTIONS_CACHE_KEY = "subscriptions:all";
    private static final String CATEGORIES_CACHE_KEY = "categories:all";
    private static final long CACHE_TTL = 3600; // 1 hour

    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        logger.info("Creating new subscription: {}", request.getName());

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

        // Clear cache
        clearCache();

        return mapToResponse(savedSubscription);
    }

    public List<SubscriptionResponse> getAllSubscriptions() {
        logger.info("Getting all subscriptions");

        // Try to get from cache first
        @SuppressWarnings("unchecked")
        List<SubscriptionResponse> cachedSubscriptions =
                (List<SubscriptionResponse>) redisTemplate.opsForValue().get(SUBSCRIPTIONS_CACHE_KEY);

        if (cachedSubscriptions != null) {
            logger.info("Retrieved subscriptions from cache");
            return cachedSubscriptions;
        }

        // Get from database
        List<Subscription> subscriptions = subscriptionRepository.findByIsActive(true);
        List<SubscriptionResponse> response = subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Cache the result
        redisTemplate.opsForValue().set(SUBSCRIPTIONS_CACHE_KEY, response, CACHE_TTL, TimeUnit.SECONDS);
        logger.info("Cached {} subscriptions", response.size());

        return response;
    }

    public SubscriptionResponse getSubscriptionById(Long id) {
        logger.info("Getting subscription by ID: {}", id);

        // Try to get from cache first
        String cacheKey = SUBSCRIPTION_CACHE_PREFIX + id;
        SubscriptionResponse cachedSubscription =
                (SubscriptionResponse) redisTemplate.opsForValue().get(cacheKey);

        if (cachedSubscription != null) {
            logger.info("Retrieved subscription from cache: {}", id);
            return cachedSubscription;
        }

        Subscription subscription = subscriptionRepository.findById(id)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> {
                    logger.warn("Subscription not found: {}", id);
                    return new RuntimeException("Subscription not found");
                });

        SubscriptionResponse response = mapToResponse(subscription);

        // Cache the result
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL, TimeUnit.SECONDS);

        return response;
    }

    public List<SubscriptionResponse> getSubscriptionsByCategory(String category) {
        logger.info("Getting subscriptions by category: {}", category);

        String cacheKey = "subscriptions:category:" + category;
        @SuppressWarnings("unchecked")
        List<SubscriptionResponse> cachedSubscriptions =
                (List<SubscriptionResponse>) redisTemplate.opsForValue().get(cacheKey);

        if (cachedSubscriptions != null) {
            logger.info("Retrieved subscriptions from cache for category: {}", category);
            return cachedSubscriptions;
        }

        List<Subscription> subscriptions = subscriptionRepository.findByCategoryAndIsActive(category, true);
        List<SubscriptionResponse> response = subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Cache the result
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL, TimeUnit.SECONDS);

        return response;
    }

    public List<String> getAllCategories() {
        logger.info("Getting all categories");

        // Try to get from cache first
        @SuppressWarnings("unchecked")
        List<String> cachedCategories =
                (List<String>) redisTemplate.opsForValue().get(CATEGORIES_CACHE_KEY);

        if (cachedCategories != null) {
            logger.info("Retrieved categories from cache");
            return cachedCategories;
        }

        List<String> categories = subscriptionRepository.findAllActiveCategories();

        // Cache the result
        redisTemplate.opsForValue().set(CATEGORIES_CACHE_KEY, categories, CACHE_TTL, TimeUnit.SECONDS);

        return categories;
    }

    public List<SubscriptionResponse> searchSubscriptions(String name) {
        logger.info("Searching subscriptions by name: {}", name);

        List<Subscription> subscriptions = subscriptionRepository
                .findByNameContainingIgnoreCaseAndIsActive(name);

        return subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SubscriptionResponse updateSubscription(Long id, SubscriptionRequest request) {
        logger.info("Updating subscription: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .filter(s -> s.getIsActive())
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

        // Clear cache
        clearCache();

        return mapToResponse(updatedSubscription);
    }

    public void deleteSubscription(Long id) {
        logger.info("Deleting subscription: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);

        logger.info("Successfully deleted subscription: {}", id);

        // Clear cache
        clearCache();
    }

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

    private void clearCache() {
        try {
            redisTemplate.delete(SUBSCRIPTIONS_CACHE_KEY);
            redisTemplate.delete(CATEGORIES_CACHE_KEY);
            logger.debug("Cleared subscription caches");
        } catch (Exception e) {
            logger.warn("Failed to clear cache", e);
        }
    }
}