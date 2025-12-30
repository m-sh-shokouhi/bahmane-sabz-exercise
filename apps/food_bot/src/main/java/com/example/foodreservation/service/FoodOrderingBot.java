package com.example.foodreservation.service;

import com.example.foodreservation.model.Food;
import com.example.foodreservation.model.Order;
import com.example.foodreservation.model.User;
import com.example.foodreservation.repository.FoodRepository;
import com.example.foodreservation.repository.OrderRepository;
import com.example.foodreservation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Service
public class FoodOrderingBot extends TelegramLongPollingBot {

    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final OrderRepository orderRepository;

    private final String botUsername;

    public FoodOrderingBot(
            UserRepository userRepository,
            FoodRepository foodRepository,
            OrderRepository orderRepository,
            @Value("${bot.token}") String botToken,
            @Value("${bot.username}") String botUsername
    ) {
        super(botToken);

        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
        this.orderRepository = orderRepository;
        this.botUsername = botUsername;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            org.telegram.telegrambots.meta.api.objects.User telegramUser = update.getMessage().getFrom();

            if (messageText.equals("/start")) {
                registerUser(telegramUser);
                sendMenu(chatId);
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Long telegramUserId = update.getCallbackQuery().getFrom().getId();

            handleOrder(chatId, telegramUserId, callbackData);
        }
    }

    private void registerUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        if (userRepository.findByTelegramId(telegramUser.getId()).isEmpty()) {
            User newUser = new User();
            newUser.setTelegramId(telegramUser.getId());
            newUser.setFirstName(telegramUser.getFirstName());
            newUser.setUsername(telegramUser.getUserName());
            userRepository.save(newUser);
        }
    }

    private void sendMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("منو:\nلطفاً غذای خود را انتخاب کنید:");

        List<Food> foods = foodRepository.findByIsActiveTrue();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> allRows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        for (Food food : foods) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            String priceStr = (food.getPrice() != null) ? food.getPrice().toString() : "0";
            button.setText(food.getName() + " (" + priceStr + ")");
            button.setCallbackData("food_" + food.getId());

            currentRow.add(button);

            if (currentRow.size() == 2) {
                allRows.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }

        if (!currentRow.isEmpty()) {
            allRows.add(currentRow);
        }

        markup.setKeyboard(allRows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleOrder(Long chatId, Long telegramUserId, String data) {
        if (data.startsWith("food_")) {
            Long foodId = Long.parseLong(data.split("_")[1]);

            User user = userRepository.findByTelegramId(telegramUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Food food = foodRepository.findById(foodId)
                    .orElseThrow(() -> new RuntimeException("Food not found"));

            Order order = new Order();
            order.setUser(user);
            order.setFood(food);
            orderRepository.save(order);

            SendMessage msg = new SendMessage();
            msg.setChatId(chatId.toString());
            msg.setText("سفارش شما ثبت شد: " + food.getName());
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}