package com.example.secureEye.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.secureEye.Fragment.AdminNavDashboard;
import com.example.secureEye.Fragment.NavFrag2;
import com.example.secureEye.Model.User;
import com.example.secureEye.R;
import com.example.secureEye.Services.AppController;
import com.example.secureEye.Services.DeviceStatusService;
import com.example.secureEye.Services.LocationUpdatesService;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SharedPrefManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdminNavigationDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "AdminNavigationDashboar";
    private CollectionReference onlineRef, offlineRef, userProfileRef;
    private FirebaseAuth mAuth;
    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        onlineRef = Constant_URLS.ONLINE_REF;
        offlineRef = Constant_URLS.OFFLINE_REF;
        userProfileRef = Constant_URLS.USER_PROFILE_REF;

        if (!checkPermissions()) {
            requestPermissions();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView tv = headerView.findViewById(R.id.nameText);
        tv.setText(mAuth.getCurrentUser().getDisplayName());
        TextView tv1 = headerView.findViewById(R.id.phoneText);
        tv1.setText(mAuth.getCurrentUser().getEmail());
        CircleImageView img = headerView.findViewById(R.id.navImage);
        //img.setImageBitmap(profilePic);

        AdminNavDashboard navFrag1 = new AdminNavDashboard();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.NavFrameLayout, navFrag1);
        ft.commit();

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dash) {
            AdminNavDashboard navFrag1 = new AdminNavDashboard();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            ft.replace(R.id.NavFrameLayout, navFrag1);
            ft.commit();
        } else if (id == R.id.nav_gallery) {
            NavFrag2 navFrag2 = new NavFrag2();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            ft.replace(R.id.NavFrameLayout, navFrag2);
            ft.commit();
        } else if (id == R.id.nav_loc_update) {

            LocationUpdatesService mService = AppController.getInstance().mService;
            if (mService != null)
                mService.removeLocationUpdates();

        } else if (id == R.id.nav_manage) {
            /*if (!checkPermissions()) {
                requestPermissions();
            } else {
                LocationUpdatesService  mService=AppController.getInstance().mService;
                ServiceConnection mServiceConnection=AppController.getInstance().mServiceConnection;
                if (mServiceConnection!=null)
                bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
            }*/

        } else if (id == R.id.nav_edit_profile) {
           /* NavEditAdminProfile navProfile = new NavEditAdminProfile();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.NavFrameLayout, navProfile);
            ft.commit();*/

        } else if (id == R.id.nav_logout) {

            AdminLogout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void AdminLogout() {
        final ProgressDialog progressDialog1 = new ProgressDialog(this);
        progressDialog1.setCancelable(false);
        progressDialog1.setMessage("Logging off ...");
        progressDialog1.show();

        onlineRef.document(mAuth.getCurrentUser().getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String userGeoZone= SharedPrefManager.getInstance(AdminNavigationDashboard.this).getUserGeoZone();
                    offlineRef.document(mAuth.getCurrentUser().getUid())
                            .set(new User(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getUid(),userGeoZone, "Offline")).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                LocationUpdatesService mService = AppController.getInstance().mService;
                                mService.removeLocationUpdates();

                                mAuth.signOut();
                                Intent i = new Intent(AdminNavigationDashboard.this, MainActivity.class);
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
                            ActivityCompat.requestPermissions(AdminNavigationDashboard.this,
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
            ActivityCompat.requestPermissions(AdminNavigationDashboard.this,
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

    @Override
    protected void onResume() {
        super.onResume();
        LocationUpdatesService mService = AppController.getInstance().mService;
        if (mService != null)
            mService.requestLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*LocationUpdatesService mService=AppController.getInstance().mService;
        if (mService!=null)
            mService.removeLocationUpdates();*/


    }

   /* @Override
    protected void onDestroy() {
        LocationUpdatesService mService = AppController.getInstance().mService;
        if (mService != null)
            mService.removeLocationUpdates();
        super.onDestroy();
    }*/

    @Override
    public void onBackPressed() {

        if (getFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getFragmentManager().popBackStack();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            finishAffinity();
            finish();
        } else {
            super.onBackPressed();
            finishAffinity();
            finish();
        }


    }

}
