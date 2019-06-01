package com.example.secureEye.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.example.secureEye.Fragment.AdminNavDashboard;
import com.example.secureEye.Fragment.UserNavDashboard;
import com.example.secureEye.Model.User;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Services.AppController;
import com.example.secureEye.Services.LocationHelper;
import com.example.secureEye.Services.LocationUpdatesService;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SharedPrefManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.PolyUtil;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import de.hdodenhof.circleimageview.CircleImageView;

import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.example.secureEye.Activity.AdminNavigationDashboard.REQUEST_PERMISSIONS_REQUEST_CODE;
import static com.example.secureEye.Activity.MapsTracking.KEY_LATITUDE;
import static com.example.secureEye.Activity.MapsTracking.KEY_LONGITUDE;

public class UserNavigationDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static String TAG = "UserNavigationDashboard";
    private CollectionReference onlineRef, offlineRef, geofenceRef, adminProfileRef;
    private FirebaseAuth mAuth;
    private List<LatLng> mPolygonPoints = new ArrayList<>();
    private MyReceiver myReceiver;
    private CollectionReference notificationRef;
    private String userAdminMail;
    private String userAdminId;
    public static String userAdminDeviceToken;
    private boolean doubleBackToExitPressedOnce = false;
    String msg = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_navigation_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        myReceiver = new MyReceiver();

        mAuth = FirebaseAuth.getInstance();
        notificationRef = Constant_URLS.NOTIFICATION_DATA;
        userAdminMail = SharedPrefManager.getInstance(getApplicationContext()).getUserAdminMail();
        userAdminId = SharedPrefManager.getInstance(getApplicationContext()).getUserAdminId();
        onlineRef = Constant_URLS.ONLINE_REF;
        offlineRef = Constant_URLS.OFFLINE_REF;
        geofenceRef = Constant_URLS.GEOFENCE_LIST;
        adminProfileRef = Constant_URLS.ADMIN_PROFILE_REF;

        if (!checkPermissions()) {
            requestPermissions();
        }

        getPolygonPoints();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view1);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView tv = headerView.findViewById(R.id.nameText1);
        tv.setText(mAuth.getCurrentUser().getDisplayName());
        TextView tv1 = headerView.findViewById(R.id.phoneText1);
        tv1.setText(mAuth.getCurrentUser().getEmail());
        CircleImageView profilePic = headerView.findViewById(R.id.navImage1);
        String photoUrl = SharedPrefManager.getInstance(this).getUserProfilePic();
        Picasso.get().load(photoUrl).into(profilePic);

        UserNavDashboard navFrag1 = new UserNavDashboard();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.NavFrameLayout1, navFrag1);
        ft.addToBackStack("UserNavDashboard");
        ft.commit();

    }

    private void getPolygonPoints() {
        String userGeoZone = SharedPrefManager.getInstance(UserNavigationDashboard.this).getUserGeoZone();

        geofenceRef.document(userGeoZone).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot geofence = task.getResult();
                    Map<String, Object> newGeofence = new HashMap<>();
                    newGeofence = geofence.getData();

                    for (int i = 0; i < newGeofence.size() / 2; i++) { //size is showing double the data so divide the size by 2
                        Log.d(TAG, "location is lat: " + newGeofence.get(KEY_LATITUDE + i));
                        Log.d(TAG, "location is lon: " + newGeofence.get(KEY_LONGITUDE + i));
                        LatLng latLngs = new LatLng(Double.parseDouble(newGeofence.get(KEY_LATITUDE + i).toString()), Double.parseDouble(newGeofence.get(KEY_LONGITUDE + i).toString()));
                        mPolygonPoints.add(latLngs);
                    }
                }
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {

                //Toast.makeText(UserNavigationDashboard.this, "new location found is this: " + LocationHelper.getLocationText(location), Toast.LENGTH_SHORT).show();
                if (mPolygonPoints.size() > 0) {
                    if (PolyUtil.containsLocation(location.getLatitude(), location.getLongitude(), mPolygonPoints, true)) {
                        Log.d(TAG, "inside the geofence");
                        if (!msg.equalsIgnoreCase("Entered the geofence.")) {
                            msg = "Entered the geofence.";
                            saveNotificationToServer(msg);
                            //Toast.makeText(context, "inside location", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "outside the geofence");
                        if (!msg.equalsIgnoreCase("Exited the geofence.")) {
                            msg = "Exited the geofence.";
                            saveNotificationToServer(msg);
                            //Toast.makeText(context, "outside location", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    private void saveNotificationToServer(String msg) {

        adminProfileRef.document(userAdminId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserProfile user = documentSnapshot.toObject(UserProfile.class);
                Log.d(TAG, "" + user.getName());
                userAdminDeviceToken = user.getDeviceToken();

                Map<String, Object> notificationMessage = new HashMap<>();
                notificationMessage.put("message", msg);
                notificationMessage.put("from_id", mAuth.getCurrentUser().getUid());
                notificationMessage.put("from_name", mAuth.getCurrentUser().getDisplayName());
                notificationMessage.put("to_admin", userAdminDeviceToken);
                notificationMessage.put("admin_mail", userAdminMail);
                notificationMessage.put("timeStamp", LocationHelper.getGMTTime());

                Log.d(TAG, "saveNotificationToServer: " + userAdminDeviceToken);

                notificationRef.document("GPS_Notification").collection(LocationHelper.getDate())
                        .document(String.valueOf(System.currentTimeMillis())).set(notificationMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(getApplicationContext(), "Notification sent", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        /*if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            Log.d(TAG, "onBackPressed: entry count");
        } else {
            Log.d(TAG, "onBackPressed: else entry");
            super.onBackPressed();
            finishAffinity();
            finish();
        }*/


        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
            // super.onBackPressed();
            // return;
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                finishAffinity();
                finish();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press one more time to exit",
                    Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {

                    doubleBackToExitPressedOnce = false;
                }
            }, 3000);

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_user_dash) {
            UserNavDashboard navFrag1 = new UserNavDashboard();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.NavFrameLayout1, navFrag1);
            ft.addToBackStack("UserNavDashboard");
            ft.commit();
            // Handle the camera action
        } else if (id == R.id.nav_user_gallery) {

        } else if (id == R.id.nav__user_slideshow) {

        } else if (id == R.id.nav_user_manage) {

        } else if (id == R.id.nav_user_profile) {

        } else if (id == R.id.nav_user_logout) {

            userLogout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void userLogout() {
        final ProgressDialog progressDialog1 = new ProgressDialog(this);
        progressDialog1.setCancelable(false);
        progressDialog1.setMessage("Logging off ...");
        progressDialog1.show();

        onlineRef.document(mAuth.getCurrentUser().getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String userGeoZone = SharedPrefManager.getInstance(UserNavigationDashboard.this).getUserGeoZone();
                    offlineRef.document(mAuth.getCurrentUser().getUid())
                            .set(new User(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getUid(), userGeoZone, "Offline")).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                LocationUpdatesService mService = AppController.getInstance().mService;
                                if (mService!=null)
                                mService.removeLocationUpdates();

                                mAuth.signOut();
                                Intent i = new Intent(UserNavigationDashboard.this, MainActivity.class);
                                // Closing all the Activities
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                // Add new Flag to start new Activity
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                progressDialog1.dismiss();

                                // Staring Login Activity
                                startActivity(i);
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }

    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.drawer_layout),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(UserNavigationDashboard.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(UserNavigationDashboard.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        LocationUpdatesService mService = AppController.getInstance().mService;
        if (mService != null)
            mService.requestLocationUpdates();
    }

}
