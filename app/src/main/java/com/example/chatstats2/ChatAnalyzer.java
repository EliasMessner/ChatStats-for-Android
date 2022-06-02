package com.example.chatstats2;

import static com.example.chatstats2.Util.wordTokenize;

import android.os.Build;
import androidx.annotation.RequiresApi;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ChatAnalyzer {

    Chat chat;

    public ChatAnalyzer(Chat chat) {
        this.chat = chat;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Map<LocalDate, Long> getMessageCountPerDate() {
        Map<LocalDate, Long> result = new HashMap<>();
        for (Message message : chat.getMessageList()) {
            result.merge(message.getDateTime().toLocalDate(), 1L, Long::sum);
        }
        for (int i = 0; i < this.getDuration(ChronoUnit.DAYS); i++) {
            LocalDate key = chat.getMessageList().get(0).getDateTime().toLocalDate().plus(i, ChronoUnit.DAYS);
            result.putIfAbsent(key, 0L);
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Map<DayOfWeek, Long> getMessageCountPerWeekDay() {
        Map<DayOfWeek, Long> result = new HashMap<>();
        for (Message message : chat.getMessageList()) {
            result.merge(message.getDateTime().getDayOfWeek(), 1L, Long::sum);
        }
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            result.putIfAbsent(dayOfWeek, 0L);
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Map<Integer, Long> getMessageCountPerHour() {
        Map<Integer, Long> result = new HashMap<>();
        for (Message message : chat.getMessageList()) {
            result.merge(message.getDateTime().getHour(), 1L, Long::sum);
        }
        for (int hour = 0; hour < 24; hour++) {
            result.putIfAbsent(hour, 0L);
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Map<String, Long> getUserParticipationByMessages() {
        return getUserParticipation(message -> 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Map<String, Long> getUserParticipationByWords() {
        return getUserParticipation(message -> wordTokenize(message.getContent()).length);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Map<String, Long> getUserParticipation(Function<Message, Integer> op) {
        Map<String, Long> result = new HashMap<>();
        chat.getMessageList().forEach(message ->
                result.merge(message.getSender(), (long) op.apply(message), Long::sum));
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public long getDuration(ChronoUnit timeUnit) {
        return chat.getMessageList().get(0).getDateTime()
                .until(
                        chat.getMessageList().get(chat.getMessageList().size() - 1).getDateTime(),
                        timeUnit);
    }

}