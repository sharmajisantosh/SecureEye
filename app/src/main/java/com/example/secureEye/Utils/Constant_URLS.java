package com.example.secureEye.Utils;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Constant_URLS {


    public static final CollectionReference ONLINE_REF = FirebaseFirestore.getInstance().collection("Online");
    public static final CollectionReference OFFLINE_REF = FirebaseFirestore.getInstance().collection("Offline");
    public static final CollectionReference USER_PROFILE_REF = FirebaseFirestore.getInstance().collection("UserProfile");
    public static final CollectionReference LOCATIONS_REF = FirebaseFirestore.getInstance().collection("Locations");
    public static final CollectionReference LOCATIONS_HISTORY = FirebaseFirestore.getInstance().collection("LocationHistory");
    public static final CollectionReference GEOFENCE_LIST = FirebaseFirestore.getInstance().collection("Geofence_List");
    public static final CollectionReference NOTIFICATION_DATA = FirebaseFirestore.getInstance().collection("Notifications");
    public static final CollectionReference ADMIN_PROFILE_REF = FirebaseFirestore.getInstance().collection("AdminProfile");
    public static final CollectionReference USER_ADMIN_LINK_REF = FirebaseFirestore.getInstance().collection("UserAdminLink");
    public static final StorageReference IMAGE_STORAGE_REF = FirebaseStorage.getInstance().getReference().child("Images");
    public static final StorageReference AUDIO_STORAGE_REF = FirebaseStorage.getInstance().getReference().child("Audio");
    public static final StorageReference VIDEO_STORAGE_REF = FirebaseStorage.getInstance().getReference().child("Video");
    public static final StorageReference PROFILE_PIC_STORAGE_REF = FirebaseStorage.getInstance().getReference().child("ProfilePic");

}
