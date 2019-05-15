package com.example.secureEye.Model;

import android.hardware.usb.UsbRequest;

public class User {
    private String dispName, uid, status, geoZone;

    public User(){
    }

    public User(String dispName,String uid, String geoZone, String status ) {
        this.uid = uid;
        this.status = status;
        this.dispName=dispName;
        this.geoZone=geoZone;
    }

    public User(String dispName) {
        this.dispName=dispName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDispName() {
        return dispName;
    }

    public void setDispName(String dispName) {
        this.dispName = dispName;
    }

    public String getGeoZone() {
        return geoZone;
    }

    public void setGeoZone(String geoZone) {
        this.geoZone = geoZone;
    }

}
