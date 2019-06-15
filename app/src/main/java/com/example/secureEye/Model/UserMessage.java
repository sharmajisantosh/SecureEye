package com.example.secureEye.Model;

import java.io.Serializable;

public class UserMessage implements Serializable {
    private String message;
    private String imgUrl;
    private String audUrl;
    private String vidUrl;
    private String fromId;
    private String fromName;
    private String toAdminDevice;
    private String adminMail;
    private String timeStamp;
    private boolean read;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getAudUrl() {
        return audUrl;
    }

    public void setAudUrl(String audUrl) {
        this.audUrl = audUrl;
    }

    public String getVidUrl() {
        return vidUrl;
    }

    public void setVidUrl(String vidUrl) {
        this.vidUrl = vidUrl;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToAdminDevice() {
        return toAdminDevice;
    }

    public void setToAdminDevice(String toAdminDevice) {
        this.toAdminDevice = toAdminDevice;
    }

    public String getAdminMail() {
        return adminMail;
    }

    public void setAdminMail(String adminMail) {
        this.adminMail = adminMail;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

}
