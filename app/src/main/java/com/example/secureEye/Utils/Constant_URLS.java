package com.example.secureEye.Utils;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Constant_URLS {


    public static final CollectionReference ONLINE_REF = FirebaseFirestore.getInstance().collection("Online");
    public static final CollectionReference OFFLINE_REF = FirebaseFirestore.getInstance().collection("offLine");
    public static final CollectionReference USER_PROFILE_REF = FirebaseFirestore.getInstance().collection("UserProfile");
    public static final CollectionReference LOCATIONS_REF = FirebaseFirestore.getInstance().collection("Locations");
    public static final CollectionReference LOCATIONS_HISTORY = FirebaseFirestore.getInstance().collection("Location_History");
    public static final CollectionReference GEOFENCE_LIST = FirebaseFirestore.getInstance().collection("Geofence_List");
    public static final CollectionReference NOTIFICATION_DATA = FirebaseFirestore.getInstance().collection("Notifications");
    public static final CollectionReference ADMIN_PROFILE_REF = FirebaseFirestore.getInstance().collection("AdminProfile");
    public static final CollectionReference DEVICE_STATUS_BATTERY = FirebaseFirestore.getInstance().collection("DeviceStatus");

}
