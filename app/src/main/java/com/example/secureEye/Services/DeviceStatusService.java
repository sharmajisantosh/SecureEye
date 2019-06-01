package com.example.secureEye.Services;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.util.LogTime;
import com.example.secureEye.Activity.UserNavigationDashboard;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SessionManager;
import com.example.secureEye.Utils.SharedPrefManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceStatusService extends Service {

    private static final String TAG = "DeviceStatusService";
    private BatteryReceiver batteryReceiver;
    private CollectionReference notificationData = Constant_URLS.NOTIFICATION_DATA;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String msg = "";
    private String userAdminMail;
    private String userAdminDeviceToken;
    private List<Object> networkList;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("myLog", "Service: onCreate");

        //% BATTERY LISTENER
        networkList=new ArrayList<>();
        batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("myLog", "Service: onStartCommand");
        /*if (Build.VERSION.SDK_INT <= 19) {
            onTaskRemoved(intent);
        }*/

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        Log.d("myLog", "Service: onDestroy");
        unregisterReceiver(batteryReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            userAdminMail = SharedPrefManager.getInstance(getApplicationContext()).getUserAdminMail();
            userAdminDeviceToken = UserNavigationDashboard.userAdminDeviceToken;

            int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int level = -1;
            if (rawlevel >= 0 && scale > 0) {
                level = (rawlevel * 100) / scale;
            }
            boolean hasMobileNetwork = isMobileAvailable(getApplicationContext());
            Log.d("deviceStatus", "onReceive: " + hasMobileNetwork);
            Log.d("myLog", "Battery Level Remaining: " + level + "%");
            //Toast.makeText(context, "Battery: " + level + "% has network= "+hasMobileNetwork, Toast.LENGTH_SHORT).show();

            if (level < 10) {
                if (!msg.equalsIgnoreCase("Battery below 10%")) {
                    msg = "Battery below 10%";
                    if (userAdminDeviceToken!=null)
                    saveNotification(msg);
                }
            }

            if (!hasMobileNetwork && !msg.equalsIgnoreCase("No Network")) {
                msg = "No Network";
                Log.d(TAG, "hasmobilenetwork "+hasMobileNetwork);

                if (SessionManager.isNetworkAvaliable(getApplicationContext())) {
                    Log.d(TAG, "internet available ");
                    if (userAdminDeviceToken!=null)
                    saveNotification(msg);

                }else {
                    Log.d(TAG, "internet not available ");
                    if (userAdminDeviceToken!=null)
                    saveNotificationInList(msg);
                }
            }else {
                if (SessionManager.isNetworkAvaliable(getApplicationContext())) {
                    Log.d(TAG, "internet available in else");
                    if (networkList.size() > 0) {
                        for (int i = 0; i < networkList.size(); i++) {
                            sendNotoficationToServer(networkList.get(i));
                            Log.d(TAG, "networklist " + networkList.size());
                        }
                        networkList.clear();
                        msg="abcd";
                    }
                }
            }
        }
    }

    private void saveNotificationInList(String msg) {

        Map<String, Object> notificationMessage = new HashMap<>();
        notificationMessage.put("message", msg);
        notificationMessage.put("from_id", mAuth.getCurrentUser().getUid());
        notificationMessage.put("from_name", mAuth.getCurrentUser().getDisplayName());
        notificationMessage.put("to_admin", userAdminDeviceToken);
        notificationMessage.put("admin_mail", userAdminMail);
        notificationMessage.put("timeStamp", LocationHelper.getGMTTime());
        networkList.add(notificationMessage);
        Log.d(TAG, "saveNotificationInList: noti saved network");
    }

    private void saveNotification(String msg) {

        Map<String, Object> notificationMessage = new HashMap<>();
        notificationMessage.put("message", msg);
        notificationMessage.put("from_id", mAuth.getCurrentUser().getUid());
        notificationMessage.put("from_name", mAuth.getCurrentUser().getDisplayName());
        notificationMessage.put("to_admin", userAdminDeviceToken);
        notificationMessage.put("admin_mail", userAdminMail);
        notificationMessage.put("timeStamp", LocationHelper.getGMTTime());

        sendNotoficationToServer(notificationMessage);
    }

    private void sendNotoficationToServer(Object notificationMessage) {
        Log.d(TAG, "saveDeviceNotificationToServer: " + userAdminDeviceToken);

        notificationData.document("Device_Status_Notification").collection(LocationHelper.getDate())
                .document(String.valueOf(System.currentTimeMillis())).set(notificationMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "device Notification sent", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static Boolean isMobileAvailable(Context appcontext) {
        TelephonyManager tel = (TelephonyManager) appcontext.getSystemService(Context.TELEPHONY_SERVICE);
        return ((tel.getNetworkOperator() != null && tel.getNetworkOperator().equals("")) ? false : true);
    }
}
