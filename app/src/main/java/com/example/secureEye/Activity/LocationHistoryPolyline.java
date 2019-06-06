package com.example.secureEye.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secureEye.Adapter.SpinnerAdapter;
import com.example.secureEye.Model.LocationHistoryModel;
import com.example.secureEye.Model.User;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.TypefaceSpan;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LocationHistoryPolyline extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "LocationHistoryPolyline";
    private BottomSheetBehavior bottomSheetBehavior;
    private CoordinatorLayout coordinatorLayoutBottomSheet;
    private final int BOUND_PADDING = 100;
    private Button btnShowHistory, btnResetMap;
    private TextView selectedStartTime, selectedEndTime;
    private Spinner geofenceSpinner, userListSpinner;
    public static GoogleMap mMap;
    private FirebaseAuth mAuth;
    private ArrayAdapter<String> geofenceAdapter;
    private SpinnerAdapter userAdapter;
    private List<String> geofenceList, userIdList;
    private SimpleDateFormat simpleDateFormat;
    private List<LatLng> locationPoints;
    private SimpleDateFormat sdf;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private List<UserProfile> userList;
    private CollectionReference locationHistRef, userProfileRef, onlineRef, offlineRef, geofenceRef;
    private String selectedUserId = null;
    private Date selectedStartDate, selectedEndDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_history_polyline);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapLocHist);
        mapFragment.getMapAsync(this);

        SpannableString str = new SpannableString("Location History");
        str.setSpan(new TypefaceSpan(this, TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(str);

        coordinatorLayoutBottomSheet = findViewById(R.id.bottomSheetLocHist);
        bottomSheetBehavior = BottomSheetBehavior.from(coordinatorLayoutBottomSheet);
        geofenceSpinner = findViewById(R.id.locHistGeofenceList);
        userListSpinner = findViewById(R.id.allUserList);
        btnShowHistory = findViewById(R.id.btnShowOnMap2);
        btnResetMap=findViewById(R.id.btnResetMap);
        selectedStartTime = findViewById(R.id.tvSelectedStartTime);
        selectedEndTime = findViewById(R.id.tvSelectedEndTime);

        geofenceRef = Constant_URLS.GEOFENCE_LIST;
        userProfileRef = Constant_URLS.USER_PROFILE_REF;
        locationHistRef = Constant_URLS.LOCATIONS_HISTORY;
        mAuth = FirebaseAuth.getInstance();
        geofenceList = new ArrayList<>();
        userIdList = new ArrayList<>();
        userList = new ArrayList<>();
        locationPoints = new ArrayList<>();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");

        btnShowHistory.setOnClickListener(this);
        selectedStartTime.setOnClickListener(this);
        selectedEndTime.setOnClickListener(this);
        btnResetMap.setOnClickListener(this);
        selectedEndTime.setEnabled(false);

        loadGeofenceList();
    }

    private void loadGeofenceList() {

        geofenceRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    geofenceList.clear();
                    List<DocumentSnapshot> geoList = task.getResult().getDocuments();
                    if (geoList.size() > 0) {
                        geofenceList.add("Select");
                        for (int i = 0; i < geoList.size(); i++) {
                            geofenceList.add(geoList.get(i).getId());
                            geofenceAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        geofenceAdapter = new ArrayAdapter<String>(LocationHistoryPolyline.this, R.layout.geofence_spinner_item, R.id.tvGeofenceName, geofenceList);
        userAdapter = new SpinnerAdapter(LocationHistoryPolyline.this, userList);
        geofenceSpinner.setAdapter(geofenceAdapter);
        geofenceAdapter.notifyDataSetChanged();

        geofenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "spinner item " + geofenceList.get(position));
                if (geofenceList.get(position).toString().equalsIgnoreCase("Select")) {
                    userList.clear();
                    userIdList.clear();
                    userAdapter.notifyDataSetChanged();
                } else {
                    loadAllUsers(geofenceList.get(position));
                }
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
                    userIdList.clear();
                    userAdapter.notifyDataSetChanged();
                    List<DocumentSnapshot> uList = task.getResult().getDocuments();
                    if (uList.size() > 0) {
                        UserProfile userProfile = new UserProfile("Select");
                        userList.add(userProfile);
                        userIdList.add("Select");
                        for (int i = 0; i < uList.size(); i++) {
                            userList.add(uList.get(i).toObject(UserProfile.class));
                            userIdList.add(uList.get(i).getId());
                            userAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(LocationHistoryPolyline.this, "No user found.", Toast.LENGTH_SHORT).show();
                        userIdList.clear();
                        userList.clear();
                        userAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        userListSpinner.setAdapter(userAdapter);
        userListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UserProfile userProfile = (UserProfile) userAdapter.getItem(position);
                Log.d(TAG, "onItemSelected: " + userProfile.getName() + "  id : " + userIdList.get(position));
                if (!userProfile.getName().equalsIgnoreCase("Select")) {
                    selectedUserId = userIdList.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnShowOnMap2:
                if (selectedStartDate != null && selectedEndDate != null && selectedUserId != null) {
                    showOnMap();
                } else {
                    Toast.makeText(this, "Please fill all values.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tvSelectedStartTime:
                setStartTime();
                break;

            case R.id.tvSelectedEndTime:
                setEndTime();
                break;

            case R.id.btnResetMap:
                resetMap();
                break;
        }
    }

    private void setStartTime() {

        final Date now = new Date();
        final Calendar calendarMin = Calendar.getInstance();
        final Calendar calendarMax = Calendar.getInstance();

        calendarMax.setTime(now); // Set min now
        calendarMin.add(Calendar.YEAR, -1);
        final Date minDate = calendarMin.getTime();
        final Date maxDate = calendarMax.getTime();

        SwitchDateTimeDialogFragment dateTimeDialogFragment=SwitchDateTimeDialogFragment.newInstance(
                "Duty Start date","Ok", "cancel");
        dateTimeDialogFragment.setDefaultDateTime(maxDate);
        dateTimeDialogFragment.setMinimumDateTime(minDate);
        dateTimeDialogFragment.setMaximumDateTime(maxDate);
        dateTimeDialogFragment.startAtCalendarView();

        dateTimeDialogFragment.show(getSupportFragmentManager(),"new Date time");
        dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                selectedStartDate = date;
                selectedStartTime.setText(simpleDateFormat.format(date));
                selectedEndTime.setEnabled(true);
            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });
    }

    private void setEndTime() {
        final Calendar calendarMax = Calendar.getInstance();
        calendarMax.setTime(selectedStartDate);
        calendarMax.set(Calendar.HOUR, 23);
        calendarMax.set(Calendar.MINUTE, 59);
        final Date minDate = selectedStartDate;
        final Date maxDate = calendarMax.getTime();

        SwitchDateTimeDialogFragment dateTimeDialogFragment=SwitchDateTimeDialogFragment.newInstance(
                "Duty End date","Ok", "cancel");
        dateTimeDialogFragment.setDefaultDateTime(minDate);
        dateTimeDialogFragment.setMinimumDateTime(minDate);
        dateTimeDialogFragment.setMaximumDateTime(maxDate);
        dateTimeDialogFragment.startAtCalendarView();

        dateTimeDialogFragment.show(getSupportFragmentManager(),"new Date time");
        dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                selectedStartDate = date;
                selectedEndTime.setText(simpleDateFormat.format(date));

            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });

    }

    private void showOnMap() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        Log.d(TAG, "showOnMap: " + selectedUserId);
        String searchDate = dateFormat.format(selectedStartDate);
        locationHistRef.document(selectedUserId).collection(searchDate)
                .whereGreaterThan("timeStamp", selectedStartDate).whereLessThan("timeStamp", selectedEndDate).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    locationPoints.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        LocationHistoryModel locationHistory = snapshot.toObject(LocationHistoryModel.class);
                        Log.d(TAG, "polyline lat " + locationHistory.getLat() + " lon " + locationHistory.getLon());
                        LatLng latLng = new LatLng(Double.parseDouble(locationHistory.getLat()), Double.parseDouble(locationHistory.getLon()));
                        locationPoints.add(latLng);
                    }

                    if (locationPoints.size() > 1) {
                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.RED);
                        polylineOptions.width(3);
                        polylineOptions.addAll(locationPoints);

                        mMap.clear();
                        mMap.addPolyline(polylineOptions);
                        mMap.addMarker(new MarkerOptions().title("start point").position(locationPoints.get(0)));
                        mMap.addMarker(new MarkerOptions().title("end point").position(locationPoints.get(locationPoints.size()-1)));

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latLng : locationPoints) {
                            builder.include(latLng);
                        }

                        final LatLngBounds bounds = builder.build();

                        //BOUND_PADDING is an int to specify padding of bound.. try 100.
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, BOUND_PADDING);
                        mMap.animateCamera(cu);

                    }
                } else {
                    mMap.clear();
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(1f));
                    Toast.makeText(LocationHistoryPolyline.this, "No data found", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void resetMap() {
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.zoomTo(1f));
        geofenceSpinner.setSelection(0);
        selectedStartTime.setText("Select Start time");
        selectedEndTime.setText("Select End time");
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
