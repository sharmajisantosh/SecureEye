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
    private static final String USER_ADMIN_ID="UserAdminId";
    private static final String USER_PROFILE_PIC="UserProfilePic";
    private static final String USER_EMAIL="UserEmail";
    private static final String USER_PASSWORD="UserPassword";
    private static final String USER_UID="UserUid";

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
    public boolean saveSelfDeviceToken(String token){
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
    public boolean saveUserAdminId(String userType){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ADMIN_ID, userType);
        editor.apply();
        return true;
    }

    //this method will fetch the role from shared preferences
    public String getUserAdminId(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(USER_ADMIN_ID, null);
    }


    public boolean saveUserProfilePic(String profilePic){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_PROFILE_PIC, profilePic);
        editor.apply();
        return true;
    }

    public String getUserProfilePic(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(USER_PROFILE_PIC, null);
    }

    public void saveUserEmail(String email) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_EMAIL, email);
        editor.apply();
    }

    public String getUserEmail(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(USER_EMAIL, null);
    }

    public void saveUserPassword(String password) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_PASSWORD, password);
        editor.apply();
    }

    public String getUserPassword(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(USER_PASSWORD, null);
    }

    public void saveUserUid(String uId) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_UID, uId);
        editor.apply();
    }

    public String getUserUid(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(USER_UID, null);
    }
}
