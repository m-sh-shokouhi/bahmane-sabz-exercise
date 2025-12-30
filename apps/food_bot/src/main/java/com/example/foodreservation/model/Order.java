package com.example.foodreservation.model;

import com.example.foodreservation.dto.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('REGISTERED', 'REJECTED', 'READY', 'DELIVERED') DEFAULT 'REGISTERED'")
    private OrderStatus status = OrderStatus.REGISTERED;

    private LocalDateTime orderedAt;

    @PrePersist
    protected void onCreate() {
        if (orderedAt == null) {
            orderedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = OrderStatus.REGISTERED;
        }
    }
}