package com.example.foodreservation.repository;

import com.example.foodreservation.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByIsActiveTrue();
}
