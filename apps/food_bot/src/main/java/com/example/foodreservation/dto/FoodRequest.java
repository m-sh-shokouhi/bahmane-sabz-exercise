package com.example.foodreservation.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FoodRequest {
    private String name;
    private BigDecimal price;
}
