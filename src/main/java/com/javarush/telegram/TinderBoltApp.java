package com.javarush.telegram;


import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "test_AI7_bot";
    public static final String TELEGRAM_BOT_TOKEN = "7844590527:AAFRqnWDexr0lmKAMZLTQ3-uF2WnhLHoTcE";
    public static final String OPEN_AI_TOKEN = "6MZuruLWYMt7BFAYy33hJFkblB3TrOQSkF7WUgsEFs26dToB";

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();
    private UserInfo me;
    private UserInfo she;
    private int questionCount;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {

        String message = getMessageText();


        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu("Начало", "/strat",
                    "генерация Tinder-профля \uD83D\uDE0E", "/profile",
                    "сообщение для знакомства \uD83E\uDD70", "/opener",
                    "переписка от вашего имени \uD83D\uDE08", "/message",
                    "переписка со звездами \uD83D\uDD25", "/date",
                    "задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
            return;
        }

        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (currentMode == DialogMode.GPT && !isMessageCommand()) {
            String prompt = loadPrompt("gpt");
            Message msg = sendTextMessage("");
            String answer = chatGPT.sendMessage(prompt, message);
            updateTextMessage(msg, answer);
            return;
        }

        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде", "date_grande",
                    "Марго Роби", "date_robbie",
                    "Зендея", "date_zendeya",
                    "Райн Гослинг", "date_gosling",
                    "Том Харди", "date_hardy");
            return;
        }

        if (currentMode == DialogMode.DATE && !isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage(" Отличный выбор! " +
                        "Твоя задача пригласить девушку/парня на свидание  ❤\uFE0F за 5 сообщений.");

                String prompt = loadPrompt(query);
                chatGPT.setPrompt(prompt);
                return;
            }

            Message msg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
            String answer = chatGPT.addMessage(message);
            updateTextMessage(msg, answer);

            sendTextMessage(answer);
            return;
        }

        if (message.equals("/massage")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }

        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);

                Message msg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
                String answer = chatGPT.sendMessage(prompt, userChatHistory);
                updateTextMessage(msg, answer);
                return;
            }

            list.add(message);
            return;
        }

        if (message.equals("/profile")) {
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");

            me = new UserInfo();
            questionCount = 0;
            sendTextMessage("Сколько Вам лет?");
            return;
        }

        if (currentMode == DialogMode.PROFILE && !isMessageCommand()) {
            switch (questionCount) {
                case 1:
                    me.age = message;
                    questionCount = 2;
                    sendTextMessage("Кем вы работаете?");
                    return;
                case 2:
                    me.occupation = message;
                    questionCount = 3;
                    sendTextMessage("У вас есть хобби?");
                    return;
                case 3:
                    me.hobby = message;
                    questionCount = 4;
                    sendTextMessage("Что вам не нравится в людях?");
                    return;
                case 4:
                    me.annoys = message;
                    questionCount = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    me.goals = message;

                    String aboutMyself = me.toString();
                    String prompt = loadPrompt("profile");

                    Message msg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
                    String answer = chatGPT.sendMessage(prompt, aboutMyself);
                    updateTextMessage(msg, answer);

                    return;
            }
            return;

        }

        if (message.equals("/opener")) {
            currentMode = DialogMode.OPENER;
            sendTextMessage("opener");
            she = new UserInfo();
            questionCount = 1;
            sendTextMessage("Имя девушки");

            return;
        }

        if (currentMode == DialogMode.OPENER && !isMessageCommand()) {
            switch (questionCount) {
                case 1:
                    she.name = message;
                    questionCount = 2;
                    sendTextMessage("Сколько ей лет");
                    return;

                case 2:
                    she.age = message;
                    questionCount = 3;
                    sendTextMessage("Какие у нее хобби?");
                    return;

                case 3:
                    she.hobby = message;
                    questionCount = 4;
                    sendTextMessage("Кем работает");
                    return;
                case 4:
                    she.occupation = message;
                    questionCount = 5;
                    sendTextMessage("Цель знакомства");
                    return;
                case 5:
                    she.goals = message;

                    String aboutFriend = message;
                    String prompt = loadPrompt("opener");
                    Message msg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
                    String answer = chatGPT.sendMessage(prompt, aboutFriend);
                    updateTextMessage(msg, answer);
                    return;
            }

            return;
        }

        sendTextMessage("Привет!");
        sendTextMessage("*Как дела?*");

        sendTextButtonsMessage("Выберите режим работы: ",
                "Старт", "start",
                "Стоп", "stop");
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}

