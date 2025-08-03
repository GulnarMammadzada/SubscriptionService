package com.example.subscriptionservice.controller;

import com.example.subscriptionservice.dto.SubscriptionRequest;
import com.example.subscriptionservice.dto.SubscriptionResponse;
import com.example.subscriptionservice.service.SubscriptionService;
import com.example.subscriptionservice.util.UserContextUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserContextUtil userContextUtil;

    // Public/User endpoints
    @GetMapping("/available")
    public ResponseEntity<?> getAllAvailableSubscriptions() {
        try {
            logger.info("Get all available subscriptions request received");
            List<SubscriptionResponse> subscriptions = subscriptionService.getAllSubscriptions();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscriptions", subscriptions);
            response.put("count", subscriptions.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get all available subscriptions", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/available/{id}")
    public ResponseEntity<?> getAvailableSubscriptionById(@PathVariable Long id) {
        try {
            logger.info("Get available subscription by ID request received: {}", id);
            SubscriptionResponse subscription = subscriptionService.getSubscriptionById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscription", subscription);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get available subscription by ID: {}", id, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/available/category/{category}")
    public ResponseEntity<?> getAvailableSubscriptionsByCategory(@PathVariable String category) {
        try {
            logger.info("Get available subscriptions by category request received: {}", category);
            List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByCategory(category);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscriptions", subscriptions);
            response.put("category", category);
            response.put("count", subscriptions.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get available subscriptions by category: {}", category, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/available/categories")
    public ResponseEntity<?> getAllAvailableCategories() {
        try {
            logger.info("Get all available categories request received");
            List<String> categories = subscriptionService.getAllCategories();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("categories", categories);
            response.put("count", categories.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get all available categories", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/available/search")
    public ResponseEntity<?> searchAvailableSubscriptions(@RequestParam String name) {
        try {
            logger.info("Search available subscriptions request received: {}", name);
            List<SubscriptionResponse> subscriptions = subscriptionService.searchSubscriptions(name);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscriptions", subscriptions);
            response.put("searchTerm", name);
            response.put("count", subscriptions.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to search available subscriptions: {}", name, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Admin endpoints
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
        try {
            String currentUser = userContextUtil.getCurrentUsername();
            logger.info("Admin create subscription request received by: {} for: {}", currentUser, request.getName());

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

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllSubscriptionsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Boolean isActive) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<SubscriptionResponse> subscriptions = subscriptionService.getAllSubscriptionsForAdmin(pageable, isActive);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscriptions", subscriptions.getContent());
            response.put("currentPage", subscriptions.getNumber());
            response.put("totalItems", subscriptions.getTotalElements());
            response.put("totalPages", subscriptions.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get all subscriptions for admin", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSubscriptionByIdForAdmin(@PathVariable Long id) {
        try {
            logger.info("Admin get subscription by ID request received: {}", id);
            SubscriptionResponse subscription = subscriptionService.getSubscriptionByIdForAdmin(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscription", subscription);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get subscription by ID for admin: {}", id, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSubscription(@PathVariable Long id,
                                                @Valid @RequestBody SubscriptionRequest request) {
        try {
            String currentUser = userContextUtil.getCurrentUsername();
            logger.info("Admin update subscription request received by: {} for ID: {}", currentUser, id);

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

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSubscription(@PathVariable Long id) {
        try {
            String currentUser = userContextUtil.getCurrentUsername();
            logger.info("Admin delete subscription request received by: {} for ID: {}", currentUser, id);

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

    @PostMapping("/admin/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateSubscription(@PathVariable Long id) {
        try {
            String currentUser = userContextUtil.getCurrentUsername();
            logger.info("Admin activate subscription request received by: {} for ID: {}", currentUser, id);

            SubscriptionResponse subscription = subscriptionService.activateSubscription(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Subscription activated successfully");
            response.put("subscription", subscription);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to activate subscription ID: {}", id, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSubscriptionStatistics() {
        try {
            logger.info("Admin get subscription statistics request received");
            Map<String, Object> statistics = subscriptionService.getSubscriptionStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get subscription statistics", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchSubscriptionsForAdmin(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SubscriptionResponse> subscriptions = subscriptionService.searchSubscriptionsForAdmin(searchTerm, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscriptions", subscriptions.getContent());
            response.put("currentPage", subscriptions.getNumber());
            response.put("totalItems", subscriptions.getTotalElements());
            response.put("totalPages", subscriptions.getTotalPages());
            response.put("searchTerm", searchTerm);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to search subscriptions for admin with term: {}", searchTerm, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}