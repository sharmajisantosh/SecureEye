package com.example.secureEye.Services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.secureEye.Activity.UserNavigationDashboard;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SharedPrefManager;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class GeofenceTransitionIntentService extends IntentService {
    private static final String TAG = GeofenceTransitionIntentService.class.getSimpleName();
    public static final String KEY_LATITUDE = "key_lat";
    public static final String KEY_LONGITUDE = "key_long";
    private FirebaseAuth mAuth;
    private CollectionReference notificationRef;
    private String userAdminMail;
    private String userAdminDeviceToken;

    public GeofenceTransitionIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
        notificationRef = Constant_URLS.NOTIFICATION_DATA;
        userAdminMail= SharedPrefManager.getInstance(getApplicationContext()).getUserAdminMail();
        Log.d(TAG, "user admin mail" + userAdminMail);

        userAdminDeviceToken= SharedPrefManager.getInstance(getApplicationContext()).getUserAdminDeviceTokenId();
        Log.d(TAG, "user admin mail" + userAdminDeviceToken);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "inside onHandleIntent ");

        if (intent == null) {
            Log.d(TAG, "intent is null.");
            return;
        }

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            Log.e(TAG, "Error code: " + event.getErrorCode());
            return;
        }

        Log.d(TAG, "onHandleIntent: ");
        int transition = event.getGeofenceTransition();
        switch (transition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.d(TAG, "Entered the geofence.");
                saveNotificationToServer("Entered the geofence.");
                //startService(intent);
                break;

            /*case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "Dwelling on the geofence.");
                //startService(intent);
                break;
*/
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(TAG, "Exited the geofence.");
                saveNotificationToServer("Exited the geofence.");
                break;

            default:
                Log.d(TAG, "Invalid transition: " + transition);
                break;
        }
    }

    private void saveNotificationToServer(String msg) {

        Map<String, Object> notificationMessage = new HashMap<>();
        notificationMessage.put("message", msg);
        notificationMessage.put("from_id", mAuth.getCurrentUser().getUid());
        notificationMessage.put("from_name", mAuth.getCurrentUser().getDisplayName());
        notificationMessage.put("to_admin", userAdminDeviceToken);
        notificationMessage.put("admin_mail", userAdminMail);

        Log.d(TAG, "saveNotificationToServer: " + userAdminDeviceToken);

        notificationRef.document(String.valueOf(System.currentTimeMillis())).set(notificationMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Notification sent", Toast.LENGTH_SHORT).show();
            }
        });
    }
}



