package com.example.chatstats2;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private LocalDateTime dateTime;
    private String sender;
    private String content;

    Message(LocalDateTime dateTime, String sender, String content) {
        this.dateTime = dateTime;
        this.sender = sender;
        this.content = content;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Message parseMessage(String messageSegment, String dateTimePattern) {
        String[] dateTimeStringAndRest = messageSegment.split(" - ", 2);
        if (dateTimeStringAndRest.length < 2)
            return null;
        String dateTimeString = dateTimeStringAndRest[0].replaceAll(" ", " ");
        String[] senderAndContent = dateTimeStringAndRest[1].split(": ", 2);
        if (senderAndContent.length < 2)
            return null;
        String sender = senderAndContent[0];
        String content = senderAndContent[1];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimePattern);
        return new Message(LocalDateTime.parse(dateTimeString, formatter), sender, content);
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @NonNull
    @Override
    public String toString() {
        return sender + " : " + dateTime.toString().replace("T", ", ") + "<br>" + content;
    }
}
