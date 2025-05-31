package com.example.gamemology.ai.adapter;

public class ChatMessage {
    private String content;
    private boolean isUserMessage;
    private long timestamp;

    public ChatMessage(String content, boolean isUserMessage) {
        this.content = content;
        this.isUserMessage = isUserMessage;
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent() {
        return content;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }
}