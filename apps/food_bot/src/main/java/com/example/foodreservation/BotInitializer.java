package com.example.foodreservation;

import com.example.foodreservation.service.FoodOrderingBot;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotInitializer {

    private final FoodOrderingBot bot;

    public BotInitializer(FoodOrderingBot bot) {
        this.bot = bot;
    }

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(bot);
            System.out.println("ربات با موفقیت استارت شد و آماده دریافت پیام است.");
        } catch (TelegramApiException e) {
            System.err.println("خطا در ثبت ربات" + e.getMessage());
        }
    }
}