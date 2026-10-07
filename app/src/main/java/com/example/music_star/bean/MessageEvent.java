package com.example.music_star.bean;

public class MessageEvent {
    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    public MessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
