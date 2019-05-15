package com.example.secureEye.Services;

import android.Manifest;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.secureEye.Activity.AdminNavigationDashboard;
import com.example.secureEye.Activity.MainActivity;
import com.example.secureEye.BuildConfig;
import com.example.secureEye.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AppController extends Application implements LifecycleObserver, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "AppController";
    public LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;
    private MyReceiver myReceiver;
    private FirebaseAuth mAuth;

    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            Log.d(TAG, "onServiceConnected: ");
            mService.requestLocationUpdates();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Log.d("AppController", "Foreground");
        if(mAuth.getCurrentUser()!=null)
        isAppInBackground(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log.d("AppController", "Background");
        if(mAuth.getCurrentUser()!=null)
        isAppInBackground(true);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void connectListener() {
        Log.d("AppController", "Resume");
        if(mAuth.getCurrentUser()!=null)
        isAppInResume(true);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void disconnectListener() {
        Log.d("AppController", "Pause");
        if(mAuth.getCurrentUser()!=null)
        isAppInResume(false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    // Adding some callbacks for test and log
    public interface ValueChangeListener {
        void onChanged(Boolean value);
    }

    private ValueChangeListener visibilityChangeListener;

    public void setOnVisibilityChangeListener(ValueChangeListener listener) {
        this.visibilityChangeListener = listener;
    }

    public void isAppInBackground(Boolean isBackground) {
        if (null != visibilityChangeListener) {
            visibilityChangeListener.onChanged(isBackground);
        }
        Log.d(TAG, "isAppInBackground: " + isBackground);
        Log.d(TAG, "lOCATION uPDATE REQUEST  " + LocationHelper.requestingLocationUpdates(this));
        //if (LocationHelper.requestingLocationUpdates(this)) {
            if (isBackground) {
                //in background
                Log.d(TAG, "isBound: " + mBound);
                if (mBound) {
                    // Unbind from the service. This signals to the service that this activity is no longer
                    // in the foreground, and the service can respond by promoting itself to a foreground
                    // service.
                    unbindService(mServiceConnection);
                    mBound = false;
                }
                PreferenceManager.getDefaultSharedPreferences(this)
                        .unregisterOnSharedPreferenceChangeListener(this);
            } else {
                //in onStart
                Log.d(TAG, "App restarted: " + isBackground);
                PreferenceManager.getDefaultSharedPreferences(this)
                        .registerOnSharedPreferenceChangeListener(this);

                // Bind to the service. If the service is in foreground mode, this signals to the service
                // that since this activity is in the foreground, the service can exit foreground mode.
                bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                        Context.BIND_AUTO_CREATE);

            }
        //}
    }

    private void isAppInResume(boolean isResume) {
        Log.d(TAG, "lOCATION uPDATE REQUEST inresume " + LocationHelper.requestingLocationUpdates(this));
        //if (LocationHelper.requestingLocationUpdates(this)) {
            if (isResume) {
                //in resume
                Log.d(TAG, "AppResumes: " + isResume);
                LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                        new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
            } else {
                //in pause
                Log.d(TAG, "AppPauses: " + isResume);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
            }
        //}
    }

    private static AppController mInstance;

    public static AppController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(AppController.this);

        mAuth=FirebaseAuth.getInstance();
        myReceiver = new MyReceiver();

        mInstance = this;

        // addObserver
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                //Toast.makeText(mInstance, "new location: " + LocationHelper.getLocationText(location), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
