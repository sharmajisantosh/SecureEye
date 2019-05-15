package com.example.secureEye.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.secureEye.Activity.MainActivity;

public class SessionManager {
	// Shared Preferences
	SharedPreferences pref;
	
	// Editor for Shared preferences
	Editor editor;
	
	// Context
	Context _context;
	
	// Shared pref mode
	int PRIVATE_MODE = 0;

	private static final String TAG = "SessionManager";
	// Sharedpref file name
	private static final String PREF_NAME = "AndroidHivePref";
	
	// All Shared Preferences Keys
	private static final String IS_LOGIN = "IsLoggedIn";
	
	// User name (make variable public to access from outside)
	public static final String KEY_NAME = "name";
	
	// Phone (make variable public to access from outside)
	public static final String KEY_PHONE = "phone";
	public static final String KEY_C_CODE = "CCode";
	public static final String KEY_SIMPLE_PHONE = "simplePhone";

	// Image (make variable public to access from outside)
	public static final String KEY_IMAGE = "image";
	
	// Constructor
	public SessionManager(Context context){
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Create login session
	 * */
	public void createLoginSession(String name, String cCode, String simpleNumber, String phone, String image){
		// Storing login value as TRUE
		editor.putBoolean(IS_LOGIN, true);
		editor.putString(KEY_NAME, name);
		editor.putString(KEY_PHONE, phone);
		editor.putString(KEY_C_CODE, cCode);
		editor.putString(KEY_SIMPLE_PHONE, simpleNumber);
		editor.putString(KEY_IMAGE, image);

		Log.d(TAG,"session is : "+name+" "+phone+" "+cCode+" "+simpleNumber+" "+image);
		
		// commit changes
		editor.commit();
	}	
	
	/**
	 * Check login method wil check user login status
	 * If false it will redirect user to login page
	 * Else won't do anything
	 * */
	public void checkLogin(){
		// Check login status
		if(!this.isLoggedIn()){
			// user is not logged in redirect him to Login Activity
			Intent i = new Intent(_context, MainActivity.class);
			// Closing all the Activities
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			
			// Add new Flag to start new Activity
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			// Staring Login Activity
			_context.startActivity(i);
		}
		
	}
	
	
	
	/**
	 * Get stored session data
	 * */
	public HashMap<String, String> getUserDetails(){
		HashMap<String, String> user = new HashMap<String, String>();
		// user name
		user.put(KEY_NAME, pref.getString(KEY_NAME, null));
		
		// user phone
		user.put(KEY_PHONE, pref.getString(KEY_PHONE, null));

		// user image
        user.put(KEY_IMAGE, pref.getString(KEY_IMAGE, null));
		
		// return user
		return user;
	}
	
	/**
	 * Clear session details
	 * */
	public void logoutUser(){
		// Clearing all data from Shared Preferences
		editor.clear();
		editor.commit();
		
		// After logout redirect user to Loing Activity
		Intent i = new Intent(_context, MainActivity.class);
		// Closing all the Activities
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		// Add new Flag to start new Activity
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		// Staring Login Activity
		_context.startActivity(i);
	}
	
	/**
	 * Quick check for login
	 * **/
	// Get Login State
	public boolean isLoggedIn(){
		return pref.getBoolean(IS_LOGIN, false);
	}

	public static String getDateTime(){

		Date date1=new Date();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String currTimeString=simpleDateFormat.format(date1);

		return currTimeString;
	}

	public static boolean isNetworkAvaliable(Activity ctx) {
		ConnectivityManager connectivityManager = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if ((connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
				|| (connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState() == NetworkInfo.State.CONNECTED)) {
			return true;
		} else {
			return false;
		}
	}
}
