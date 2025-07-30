package com.example.subscriptionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionRequest {
    @NotBlank(message = "Subscription name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    private String currency = "AZN";

    @NotBlank(message = "Category is required")
    private String category;

    private String billingPeriod = "MONTHLY";
    private String websiteUrl;
    private String logoUrl;

    public SubscriptionRequest() {
    }

    public SubscriptionRequest(String name, String description, BigDecimal price,
                               String currency, String category, String billingPeriod) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.category = category;
        this.billingPeriod = billingPeriod;
    }
}
