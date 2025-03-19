package com.modern.chating.modal;

public class Status {
    public String user, sender;
    public String avatar;
    public String fileUrl;
    public String jam;
    public String tanggal;
    public boolean isView;

    public Status(String user, String sender, String avatar, String fileUrl, String jam, String tanggal, boolean isView) {
        this.user = user;
        this.fileUrl = fileUrl;
        this.jam = jam;
        this.tanggal = tanggal;
        this.avatar = avatar;
        this.isView = isView;
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isView() {
        return isView;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getJam() {
        return jam;
    }

    public String getUser() {
        return user;
    }

    public String toString() {
        return "Status{" +
                "user='" + user + '\'' +
                ", sender='" + sender + '\'' +

                ", avatar='" + avatar + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", jam='" + jam + '\'' +
                ", tanggal='" + tanggal + '\'' +
                ", isView=" + isView +
                '}';

    }
}
