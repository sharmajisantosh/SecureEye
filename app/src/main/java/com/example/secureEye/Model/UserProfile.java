package com.example.secureEye.Model;

public class UserProfile {
    private String name;
    private String fullPhoneNumber;
    private String email;
    private String password;
    private String geoZone;
    private String role;
    private String deviceToken;
    private String userAdminMail;

    public UserProfile() {
    }

    public UserProfile(String name, String fullPhoneNumber, String email, String password, String geoZone, String role, String deviceToken, String userAdminMail) {
        this.name = name;
        this.fullPhoneNumber = fullPhoneNumber;
        this.email = email;
        this.password = password;
        this.geoZone = geoZone;
        this.role = role;
        this.deviceToken = deviceToken;
        this.userAdminMail = userAdminMail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullPhoneNumber() {
        return fullPhoneNumber;
    }

    public void setFullPhoneNumber(String fullPhoneNumber) { this.fullPhoneNumber = fullPhoneNumber; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGeoZone() {
        return geoZone;
    }

    public void setGeoZone(String geoZone) {
        this.geoZone = geoZone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getUserAdminMail() {
        return userAdminMail;
    }

    public void setUserAdminMail(String userAdminMail) {
        this.userAdminMail = userAdminMail;
    }
}
