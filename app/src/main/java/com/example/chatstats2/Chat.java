package com.example.chatstats2;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Chat {

    private List<Message> messageList;

    public Chat() {
        this.messageList = new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Chat(List<Message> messageList) {
        this.messageList = messageList;
        sortMessages();
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addMessage(Message message) {
        this.messageList.add(message);
        sortMessages();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addMessages(Collection<Message> messages) {
        this.messageList.addAll(messages);
        sortMessages();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sortMessages() {
        Collections.sort(messageList, Comparator.comparing(Message::getDateTime));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Chat parseChat(String chatFileContent, String dateTimePattern, String dateTimeLookAhead) {
        String[] messageSegments = chatFileContent.split(dateTimeLookAhead);
        if (messageSegments.length == 0) {
            throw new IllegalArgumentException(String.format("Unable to split messages using dateTimePattern '%s' and separator '%s'", dateTimePattern, dateTimeLookAhead));
        }
        List<Message> messages = new ArrayList<>();
        for (String messageSegment : messageSegments) {
            Message message = Message.parseMessage(messageSegment, dateTimePattern);
            if (message == null)
                continue;
            messages.add(message);
        }
        return new Chat(messages);
    }

}
