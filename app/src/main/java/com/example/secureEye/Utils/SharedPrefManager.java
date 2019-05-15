package com.example.secureEye.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "FCMSharedPref";
    private static final String TAG_TOKEN = "tagtoken";
    private static final String ROLE="Role";
    private static final String GEOZONE="Geozone";
    private static final String USER_ADMIN_MAIL="UserAdminMail";
    private static final String USER_TYPE="UserType";
    private static final String USER_ADMIN_DEVICE_TOKEN="UserAdminDeviceTokenId";

    private static SharedPrefManager mInstance;
    private static Context mCtx;

    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    //this method will save the device token to shared preferences
    public boolean saveDeviceToken(String token){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TAG_TOKEN, token);
        editor.apply();
        return true;
    }

    //this method will fetch the device token from shared preferences
    public String getDeviceToken(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(TAG_TOKEN, null);
    }

    //this method will save the role to shared preferences
    public boolean saveUserRole(String role){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ROLE, role);
        editor.apply();
        return true;
    }

    //this method will fetch the role from shared preferences
    public String getUserRole(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(ROLE, null);
    }

    //this method will save the role to shared preferences
    public boolean saveUserGeoZone(String geoZone){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(GEOZONE, geoZone);
        editor.apply();
        return true;
    }

    //this method will fetch the role from shared preferences
    public String getUserGeoZone(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(GEOZONE, null);
    }

    //this method will save the role to shared preferences
    public boolean saveUserAdminMail(String userAdminMail){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ADMIN_MAIL, userAdminMail);
        editor.apply();
        return true;
    }

    //this method will fetch the role from shared preferences
    public String getUserAdminMail(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(USER_ADMIN_MAIL, null);
    }

    //this method will save the role to shared preferences
    public boolean saveUserType(String userType){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_TYPE, userType);
        editor.apply();
        return true;
    }

    //this method will fetch the role from shared preferences
    public String getUserType(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(USER_TYPE, null);
    }

    //this method will save the role to shared preferences
    public boolean saveUserAdminDeviceTokenId(String userType){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ADMIN_DEVICE_TOKEN, userType);
        editor.apply();
        return true;
    }

    //this method will fetch the role from shared preferences
    public String getUserAdminDeviceTokenId(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(USER_ADMIN_DEVICE_TOKEN, null);
    }




}
