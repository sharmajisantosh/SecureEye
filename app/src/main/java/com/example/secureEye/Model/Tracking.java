package com.example.secureEye.Model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Tracking {

    private String uid;
    private String lat;
    private String lon;
    private String dispName;
    private String timeStamp;

    public Tracking(){

    }

    public Tracking( String uid, String lat, String lon, String dispName, String timeStamp) {
        this.uid = uid;
        this.lat = lat;
        this.lon = lon;
        this.dispName=dispName;
        this.timeStamp = timeStamp;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public void setDispNamen(String dispName) {
        this.dispName = dispName;
    }

    public String getDispName() {
        return dispName;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
