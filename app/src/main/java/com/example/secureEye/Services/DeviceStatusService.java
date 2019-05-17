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

import com.example.secureEye.Utils.Constant_URLS;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;

public class DeviceStatusService extends Service {
    private BatteryReceiver batteryReceiver;
    private CollectionReference locationsRef = Constant_URLS.LOCATIONS_REF;
    private FirebaseAuth mAuth;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("myLog", "Service: onCreate");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("myLog", "Service: onStartCommand");
        /*if (Build.VERSION.SDK_INT <= 19) {
            onTaskRemoved(intent);
        }*/
        //% BATTERY LISTENER
        batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

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
            int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int level = -1;
            if (rawlevel >= 0 && scale > 0) {
                level = (rawlevel * 100) / scale;
            }
            boolean hasMobileNetwork=isMobileAvailable(getApplicationContext());
            Log.d("deviceStatus", "onReceive: "+ hasMobileNetwork);
            Log.d("myLog", "Battery Level Remaining: " + level + "%");
            Toast.makeText(context, "Battery: " + level + "% has network= "+hasMobileNetwork, Toast.LENGTH_SHORT).show();

        }
    }

    public static Boolean isMobileAvailable(Context appcontext) {
        TelephonyManager tel = (TelephonyManager) appcontext.getSystemService(Context.TELEPHONY_SERVICE);
        return ((tel.getNetworkOperator() != null && tel.getNetworkOperator().equals("")) ? false : true);
    }
}
