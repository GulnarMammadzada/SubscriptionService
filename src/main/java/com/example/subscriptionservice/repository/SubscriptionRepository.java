package com.example.subscriptionservice.repository;


import com.example.subscriptionservice.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByIsActive(Boolean isActive);

    List<Subscription> findByCategory(String category);

    List<Subscription> findByCategoryAndIsActive(String category, Boolean isActive);

    Optional<Subscription> findByNameAndIsActive(String name, Boolean isActive);

    boolean existsByName(String name);

    @Query("SELECT DISTINCT s.category FROM Subscription s WHERE s.isActive = true")
    List<String> findAllActiveCategories();

    @Query("SELECT s FROM Subscription s WHERE s.name LIKE %:name% AND s.isActive = true")
    List<Subscription> findByNameContainingIgnoreCaseAndIsActive(@Param("name") String name);

    @Query("SELECT s FROM Subscription s WHERE s.category = :category AND s.isActive = true ORDER BY s.price ASC")
    List<Subscription> findByCategoryOrderByPriceAsc(@Param("category") String category);
}
