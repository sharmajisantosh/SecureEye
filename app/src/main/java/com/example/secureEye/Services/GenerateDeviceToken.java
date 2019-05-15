package com.example.secureEye.Services;

import android.util.Log;
import android.widget.Toast;

import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SharedPrefManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.messaging.FirebaseMessagingService;

public class GenerateDeviceToken extends FirebaseMessagingService {

    private static final String TAG = "GenerateDeviceToken";
    private FirebaseAuth mAuth;
    CollectionReference userProfileRef = Constant_URLS.USER_PROFILE_REF;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + token);

        //calling the method store token and passing token
        storeToken(token);
        saveToServer(token);

    }

    private void saveToServer(String token) {
        userProfileRef.document(mAuth.getCurrentUser().getUid()).update("deviceToken",token).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "new Token found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storeToken(String token) {
        //we will save the token in sharedpreferences later
        SharedPrefManager.getInstance(getApplicationContext()).saveDeviceToken(token);
    }
}
