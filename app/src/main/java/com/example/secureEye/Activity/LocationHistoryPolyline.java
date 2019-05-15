package com.example.secureEye.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.secureEye.Adapter.GeofenceAllUserAdapter;
import com.example.secureEye.Model.User;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class LocationHistoryPolyline extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "LocationHistoryPolyline";
    private BottomSheetBehavior bottomSheetBehavior;
    private CoordinatorLayout coordinatorLayoutBottomSheet;
    private Spinner geofenceSpinner, userListSpinner;
    public static GoogleMap mMap;
    private FirebaseAuth mAuth;
    private ArrayAdapter<String> geofenceAdapter;
    private List<String> geofenceList;
    private List<UserProfile> userList;
    private GeofenceAllUserAdapter userAdapter;
    private CollectionReference locationsRef, userProfileRef, onlineRef, offlineRef, geofenceRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_history_polyline);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapLocHist);
        mapFragment.getMapAsync(this);

        coordinatorLayoutBottomSheet = findViewById(R.id.bottomSheetLocHist);
        bottomSheetBehavior = BottomSheetBehavior.from(coordinatorLayoutBottomSheet);
        geofenceSpinner = findViewById(R.id.locHistGeofenceList);
        userListSpinner = findViewById(R.id.allUserList);

        geofenceRef = Constant_URLS.GEOFENCE_LIST;
        userProfileRef = Constant_URLS.USER_PROFILE_REF;
        mAuth = FirebaseAuth.getInstance();
        geofenceList = new ArrayList<>();
        userList = new ArrayList<>();

        loadGeofenceList();
    }

    private void loadGeofenceList() {

        geofenceRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    geofenceList.clear();
                    List<DocumentSnapshot> geoList = task.getResult().getDocuments();
                    for (int i = 0; i < geoList.size(); i++) {
                        geofenceList.add(geoList.get(i).getId());
                        Log.d(TAG, "inside geofence list : " + geoList.get(0).getId());
                        geofenceAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        geofenceAdapter = new ArrayAdapter<String>(LocationHistoryPolyline.this, R.layout.geofence_spinner_item, R.id.tvGeofenceName, geofenceList);
        geofenceSpinner.setAdapter(geofenceAdapter);
        geofenceAdapter.notifyDataSetChanged();

        geofenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "spinner item " + geofenceList.get(position));
                loadAllUsers(geofenceList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void loadAllUsers(String geofenceName) {

        userProfileRef.whereEqualTo("geoZone", geofenceName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    userList.clear();
                    List<DocumentSnapshot> uList = task.getResult().getDocuments();
                    if (uList.size() > 0) {
                        for (int i = 0; i < uList.size(); i++) {
                            userList.add(uList.get(i).toObject(UserProfile.class));
                            uList.get(i).getId();
                            userAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(LocationHistoryPolyline.this, "No user found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        userAdapter = new GeofenceAllUserAdapter(LocationHistoryPolyline.this, R.layout.support_simple_spinner_dropdown_item, userList);
        userListSpinner.setAdapter(userAdapter);
        userListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UserProfile userProfile=userAdapter.getItem(position);
                Log.d(TAG, "onItemSelected: "+userProfile.getName()+"  id : ");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }
}
