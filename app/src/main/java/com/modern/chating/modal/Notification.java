package com.modern.chating.modal;

public class Notification {
    public String title;
    public String sender;
    public String category;
    public String message;
    public String time;

    public String avatar;

    public Notification(String title, String sender, String category, String message, String time, String avatar) {
        this.title = title;
        this.sender = sender;
        this.category = category;
        this.message = message;
        this.time = time;
        this.avatar = avatar;
    }

    public String getSender() {
        return sender;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }
}
