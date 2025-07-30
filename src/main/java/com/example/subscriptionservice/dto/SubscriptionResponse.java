package com.example.subscriptionservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String category;
    private String billingPeriod;
    private String websiteUrl;
    private String logoUrl;
    private Boolean isActive;

    public SubscriptionResponse() {
    }

    public SubscriptionResponse(Long id, String name, String description, BigDecimal price,
                                String currency, String category, String billingPeriod,
                                String websiteUrl, String logoUrl, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.category = category;
        this.billingPeriod = billingPeriod;
        this.websiteUrl = websiteUrl;
        this.logoUrl = logoUrl;
        this.isActive = isActive;
    }
}
