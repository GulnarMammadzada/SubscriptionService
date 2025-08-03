package com.example.subscriptionservice.repository;

import com.example.subscriptionservice.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Existing methods for users
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

    // Admin methods - can see all including inactive
    Page<Subscription> findAll(Pageable pageable);

    Page<Subscription> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE s.name LIKE %:searchTerm% OR s.description LIKE %:searchTerm% OR s.category LIKE %:searchTerm%")
    Page<Subscription> searchSubscriptions(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.isActive = true")
    Long countActiveSubscriptions();

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.isActive = false")
    Long countInactiveSubscriptions();

    @Query("SELECT COUNT(DISTINCT s.category) FROM Subscription s WHERE s.isActive = true")
    Long countActiveCategories();

    @Query("SELECT s.category, COUNT(s) FROM Subscription s WHERE s.isActive = true GROUP BY s.category")
    List<Object[]> getSubscriptionCountByCategory();
}