package com.example.subscriptionservice.controller;

import com.example.subscriptionservice.dto.SubscriptionRequest;
import com.example.subscriptionservice.dto.SubscriptionResponse;
import com.example.subscriptionservice.service.SubscriptionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin(origins = "http://localhost:8080")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<?> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
        try {
            logger.info("Create subscription request received: {}", request.getName());
            SubscriptionResponse subscription = subscriptionService.createSubscription(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Subscription created successfully");
            response.put("subscription", subscription);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Failed to create subscription: {}", request.getName(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSubscriptions() {
        try {
            logger.info("Get all subscriptions request received");
            List<SubscriptionResponse> subscriptions = subscriptionService.getAllSubscriptions();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscriptions", subscriptions);
            response.put("count", subscriptions.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get all subscriptions", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubscriptionById(@PathVariable Long id) {
        try {
            logger.info("Get subscription by ID request received: {}", id);
            SubscriptionResponse subscription = subscriptionService.getSubscriptionById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscription", subscription);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get subscription by ID: {}", id, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getSubscriptionsByCategory(@PathVariable String category) {
        try {
            logger.info("Get subscriptions by category request received: {}", category);
            List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByCategory(category);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscriptions", subscriptions);
            response.put("category", category);
            response.put("count", subscriptions.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get subscriptions by category: {}", category, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            logger.info("Get all categories request received");
            List<String> categories = subscriptionService.getAllCategories();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("categories", categories);
            response.put("count", categories.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get all categories", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchSubscriptions(@RequestParam String name) {
        try {
            logger.info("Search subscriptions request received: {}", name);
            List<SubscriptionResponse> subscriptions = subscriptionService.searchSubscriptions(name);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscriptions", subscriptions);
            response.put("searchTerm", name);
            response.put("count", subscriptions.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to search subscriptions: {}", name, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubscription(@PathVariable Long id,
                                                @Valid @RequestBody SubscriptionRequest request) {
        try {
            logger.info("Update subscription request received for ID: {}", id);
            SubscriptionResponse subscription = subscriptionService.updateSubscription(id, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Subscription updated successfully");
            response.put("subscription", subscription);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update subscription ID: {}", id, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubscription(@PathVariable Long id) {
        try {
            logger.info("Delete subscription request received for ID: {}", id);
            subscriptionService.deleteSubscription(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Subscription deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to delete subscription ID: {}", id, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

}