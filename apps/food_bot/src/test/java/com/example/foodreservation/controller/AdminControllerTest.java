package com.example.foodreservation.controller;

import com.example.foodreservation.dto.FoodRequest;
import com.example.foodreservation.model.Food;
import com.example.foodreservation.model.Order;
import com.example.foodreservation.dto.OrderStatus;
import com.example.foodreservation.model.User;
import com.example.foodreservation.repository.FoodRepository;
import com.example.foodreservation.repository.OrderRepository;
import com.example.foodreservation.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private User userAli;
    private User userReza;
    private Food pizza;
    private Food burger;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        foodRepository.deleteAll();

        userAli = new User();
        userAli.setTelegramId(100L);
        userAli.setUsername("ali");
        userRepository.save(userAli);

        userReza = new User();
        userReza.setTelegramId(200L);
        userReza.setUsername("reza");
        userRepository.save(userReza);

        pizza = new Food();
        pizza.setName("Pizza");
        pizza.setPrice(BigDecimal.valueOf(250000));
        foodRepository.save(pizza);

        burger = new Food();
        burger.setName("Burger");
        burger.setPrice(BigDecimal.valueOf(180000));
        foodRepository.save(burger);

        Order order1 = new Order();
        order1.setUser(userAli);
        order1.setFood(pizza);
        order1.setStatus(OrderStatus.REGISTERED);
        order1.setOrderedAt(LocalDateTime.now());
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUser(userReza);
        order2.setFood(burger);
        order2.setStatus(OrderStatus.READY);
        order2.setOrderedAt(LocalDateTime.now().minusDays(1));
        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setUser(userAli);
        order3.setFood(burger);
        order3.setStatus(OrderStatus.DELIVERED);
        order3.setOrderedAt(LocalDateTime.now().minusDays(2));
        orderRepository.save(order3);
    }

    @Test
    void testAddFood() throws Exception {
        FoodRequest request = new FoodRequest();
        request.setName("New Salad");
        request.setPrice(BigDecimal.valueOf(50000));

        mockMvc.perform(post("/api/admin/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Salad")));
    }

    @Test
    void testDeleteFood() throws Exception {
        mockMvc.perform(delete("/api/admin/foods/" + pizza.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("successfully")));
    }

    @Test
    void testUpdateOrderStatus() throws Exception {
        Long orderId = orderRepository.findAll().get(0).getId();

        mockMvc.perform(put("/api/admin/orders/" + orderId + "/status")
                        .param("newStatus", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    @Test
    void testReport_GetAllOrders() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void testReport_FilterByStatus() throws Exception {
        mockMvc.perform(get("/api/admin/orders")
                        .param("status", "READY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("READY")));
    }

    @Test
    void testReport_FilterByUser() throws Exception {
        mockMvc.perform(get("/api/admin/orders")
                        .param("telegramId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].user.telegramId", is(100)));
    }

    @Test
    void testReport_FilterByDateRange() throws Exception {
        String fromDate = LocalDateTime.now().minusHours(1).toString();
        String toDate = LocalDateTime.now().plusHours(1).toString();

        mockMvc.perform(get("/api/admin/orders")
                        .param("from", fromDate)
                        .param("to", toDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testReport_CombinedFilter() throws Exception {
        mockMvc.perform(get("/api/admin/orders")
                        .param("telegramId", "100")
                        .param("status", "DELIVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("DELIVERED")))
                .andExpect(jsonPath("$[0].user.username", is("ali")));
    }

    @Test
    void testReport_NoResult() throws Exception {
        mockMvc.perform(get("/api/admin/orders")
                        .param("status", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}