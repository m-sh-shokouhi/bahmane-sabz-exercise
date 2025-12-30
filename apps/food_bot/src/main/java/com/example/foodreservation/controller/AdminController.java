package com.example.foodreservation.controller;

import com.example.foodreservation.dto.FoodRequest;
import com.example.foodreservation.dto.OrderStatus;
import com.example.foodreservation.model.Food;
import com.example.foodreservation.model.Order;
import com.example.foodreservation.repository.FoodRepository;
import com.example.foodreservation.repository.OrderRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final OrderRepository orderRepository;
    private final FoodRepository foodRepository;

    public AdminController(OrderRepository orderRepository, FoodRepository foodRepository) {
        this.orderRepository = orderRepository;
        this.foodRepository = foodRepository;
    }

    @PostMapping("/foods")
    public Food addFood(@RequestBody FoodRequest request) {
        Food newFood = new Food();
        newFood.setName(request.getName());
        newFood.setPrice(request.getPrice());
        return foodRepository.save(newFood);
    }

    @DeleteMapping("/foods/{id}")
    public String deleteFood(@PathVariable Long id) {
        return foodRepository.findById(id).map(food -> {
            food.setActive(false);
            foodRepository.save(food);
            return "Food deactivated successfully.";
        }).orElse("Food not found!");
    }

    @PutMapping("/orders/{orderId}/status")
    public Order updateStatus(@PathVariable Long orderId, @RequestParam OrderStatus newStatus) {
        return orderRepository.findById(orderId).map(order -> {
            order.setStatus(newStatus);
            return orderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @GetMapping("/orders")
    public List<Order> getOrdersReport(
            @RequestParam(required = false) Long telegramId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return orderRepository.filterOrders(telegramId, status, from, to);
    }
}