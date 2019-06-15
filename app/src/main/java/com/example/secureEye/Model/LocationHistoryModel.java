package com.example.secureEye.Model;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.StringReader;
import java.util.Date;

public class LocationHistoryModel {

    public String lat;
    public String lon;
    public String deviceId;
    public String timeStamp;

    public LocationHistoryModel() {
    }
    public LocationHistoryModel(String lat, String lon, String timeStamp) {
        this.lat = lat;
        this.lon = lon;
        this.timeStamp = timeStamp;
    }
    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

}
