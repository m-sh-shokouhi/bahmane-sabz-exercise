package com.example.foodreservation.repository;

import com.example.foodreservation.dto.OrderStatus;
import com.example.foodreservation.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE " +
           "(:telegramId IS NULL OR o.user.telegramId = :telegramId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:fromDate IS NULL OR o.orderedAt >= :fromDate) AND " +
           "(:toDate IS NULL OR o.orderedAt <= :toDate) " +
           "ORDER BY o.orderedAt DESC")
    List<Order> filterOrders(
            @Param("telegramId") Long telegramId,
            @Param("status") OrderStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}

