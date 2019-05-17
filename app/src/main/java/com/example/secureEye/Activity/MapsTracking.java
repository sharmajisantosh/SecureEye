package com.example.secureEye.Activity;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.secureEye.Adapter.OfflineUserAdapter;
import com.example.secureEye.Adapter.OnlineAdapter;
import com.example.secureEye.Interface.CheckboxCheckedListner;
import com.example.secureEye.Model.Tracking;
import com.example.secureEye.Model.User;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Services.AppController;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SharedPrefManager;
import com.example.secureEye.Utils.TypefaceSpan;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.secureEye.Services.GeofenceTransitionIntentService.KEY_LATITUDE;
import static com.example.secureEye.Services.GeofenceTransitionIntentService.KEY_LONGITUDE;

public class MapsTracking extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "MapsTracking";
    public static GoogleMap mMap;
    private OfflineUserAdapter offlineAdapter;
    private OnlineAdapter onlineAdapter;
    private Spinner geofenceSpinner;
    private ArrayAdapter<String> geofenceAdapter;
    private List<User> offlineUserList;
    private List<User> onlineUserList;
    private List<User> checkedList;
    private List<String> geofenceList;
    private RecyclerView offlineUsers;
    private BottomSheetBehavior bottomSheetBehavior;
    private CoordinatorLayout coordinatorLayoutBottomSheet;
    private FirebaseAuth mAuth;

    private CollectionReference locationsRef, userProfileRef, onlineRef, offlineRef, geofenceRef;
    private DatabaseReference isOnlineRef;
    private ListenerRegistration listenerRegistration;
    private ListView onlineListView;
    private Button btnShowOnMap;
    private String geofenceSelected;
    private List<Marker> mMarkers = new ArrayList<>();
    HashMap<String, Marker> hashMapMarker=new HashMap<>();
    private Polygon mPolygon;
    private List<LatLng> mPolygonPoints = new ArrayList<>();
    private String adminDeviceToken;
    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SpannableString str = new SpannableString("Track User");
        str.setSpan(new TypefaceSpan(this,TypefaceSpan.fontName),0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ActionBar actionBar=getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(str);

        offlineUsers = findViewById(R.id.recycleOfflineList);
        onlineListView = findViewById(R.id.onlineListView);
        btnShowOnMap = findViewById(R.id.btnShowOnMap1);
        coordinatorLayoutBottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(coordinatorLayoutBottomSheet);
        geofenceSpinner = findViewById(R.id.onlineGeofenceList);
        offlineUserList = new ArrayList<>();
        onlineUserList = new ArrayList<>();
        checkedList = new ArrayList<>();
        geofenceList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        btnShowOnMap.setOnClickListener(this);

        userProfileRef = Constant_URLS.USER_PROFILE_REF;
        locationsRef = Constant_URLS.LOCATIONS_REF;
        onlineRef = Constant_URLS.ONLINE_REF;
        offlineRef = Constant_URLS.OFFLINE_REF;
        geofenceRef = Constant_URLS.GEOFENCE_LIST;
        isOnlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");

        //setUpSystem();
        setAdminDeviceToken();
        loadOfflineUsers();
        loadGeofenceList();
        //loadOnlineUsers();


        /*AppController.getInstance().setOnVisibilityChangeListener(new AppController.ValueChangeListener() {
            @Override
            public void onChanged(Boolean value) {
                Log.d(TAG, "in background  " + String.valueOf(value));
                Toast.makeText(MapsTracking.this, "in Background MapTracking", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    private void setAdminDeviceToken() {
        userProfileRef.whereEqualTo("email", "+919268710331@skyspirit.com").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        UserProfile user = snapshot.toObject(UserProfile.class);
                        Log.d(TAG, "" + user.getName());
                        adminDeviceToken = user.getDeviceToken();
                        Log.d(TAG, "adminDeviceToken: " + adminDeviceToken);
                    }
                }
            }
        });
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

        geofenceAdapter = new ArrayAdapter<String>(MapsTracking.this, R.layout.geofence_spinner_item, R.id.tvGeofenceName, geofenceList);
        geofenceSpinner.setAdapter(geofenceAdapter);
        geofenceAdapter.notifyDataSetChanged();

        geofenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "spinner item " + geofenceList.get(position));
                loadOnlineUsers(geofenceList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void loadOfflineUsers() {

        offlineRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    offlineUserList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        User user = snapshot.toObject(User.class);
                        offlineUserList.add(user);
                        offlineAdapter.notifyDataSetChanged();
                        Log.d(TAG, "inside loadofflineuser : " + user.getDispName());
                    }
                }
            }
        });

        final LinearLayoutManager layoutManager = new LinearLayoutManager(MapsTracking.this, RecyclerView.VERTICAL, false);
        offlineUsers.setLayoutManager(layoutManager);
        offlineUsers.addItemDecoration(new DividerItemDecoration(MapsTracking.this, DividerItemDecoration.VERTICAL));

        offlineAdapter = new OfflineUserAdapter(offlineUserList);
        offlineUsers.setAdapter(offlineAdapter);
        offlineAdapter.notifyDataSetChanged();

    }

    private void loadOnlineUsers(String geofenceName) {
        geofenceSelected = geofenceName;

        onlineRef.whereEqualTo("geoZone", geofenceName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    onlineUserList.clear();
                    checkedList.clear();
                    onlineAdapter.notifyDataSetChanged();
                    List<DocumentSnapshot> geoList = task.getResult().getDocuments();
                    if (geoList.size() > 0) {
                        User user = new User("All", "All", "All", "Online");
                        onlineUserList.add(user);
                        for (int i = 0; i < geoList.size(); i++) {
                            onlineUserList.add(geoList.get(i).toObject(User.class));
                            onlineAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(MapsTracking.this, "No online user found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        onlineAdapter = new OnlineAdapter(this, android.R.layout.simple_list_item_multiple_choice, onlineUserList);
        onlineListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        onlineListView.setAdapter(onlineAdapter);
        onlineAdapter.setCheckedListener(new CheckboxCheckedListner() {
            @Override
            public void getCheckboxCheckedListner(int position, boolean isChecked) {
                if (isChecked) {
                    if (onlineUserList.get(position).getDispName().equals("All")) {

                    } else
                        checkedList.add(onlineUserList.get(position));
                    Log.d(TAG, "is selected: " + onlineUserList.get(position).getDispName() + "   " + isChecked);
                } else {
                    if (onlineUserList.get(position).getDispName().equals("All")) {

                    } else
                        checkedList.remove(onlineUserList.get(position));
                    Log.d(TAG, "is removed: " + onlineUserList.get(position).getDispName() + "   " + isChecked);
                }
            }

            @Override
            public void selectAll(int position, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < onlineListView.getChildCount(); i++) {
                        RelativeLayout row = (RelativeLayout) onlineListView.getChildAt(i);
                        CheckBox cb = row.findViewById(R.id.checkbox1);
                        cb.setChecked(true);
                    }
                } else {
                    for (int i = 0; i < onlineListView.getChildCount(); i++) {
                        RelativeLayout row = (RelativeLayout) onlineListView.getChildAt(i);
                        CheckBox cb = row.findViewById(R.id.checkbox1);
                        cb.setChecked(false);
                    }
                }
            }
        });
        onlineAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {//see on map button onClick
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        if (checkedList.size() > 0)
            loadLocationForThisId();
    }

    private void loadLocationForThisId() {
        mMap.clear();
        mMarkers.clear();
        if (mPolygon != null) {
            mPolygon.remove();
        }
        mPolygonPoints.clear();
        geofenceRef.document(geofenceSelected).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                        Marker marker = mMap.addMarker(new MarkerOptions().position(latLngs));
                        marker.setVisible(false);
                        mMarkers.add(marker);
                        mPolygonPoints.add(latLngs);
                    }
                    if (mMarkers.size() > 2) {
                        mPolygon = mMap.addPolygon(new PolygonOptions().addAll(convertMarkersToLatlng(mMarkers))
                                .strokeColor(Color.RED).fillColor(Color.TRANSPARENT));
                    }
                }
            }
        });

        Query query = locationsRef;
        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {

                    for (i = 0; i < checkedList.size(); i++) {
                        Log.d(TAG, "onEventList: " + checkedList.get(i).getDispName()+" size= "+checkedList.size());
                        locationsRef.document(checkedList.get(i).getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {

                                    Tracking tracking = documentSnapshot.toObject(Tracking.class);
                                    //Add marker for user location
                                    LatLng otherUserLocation = new LatLng(Double.parseDouble(tracking.getLat()), Double.parseDouble(tracking.getLon()));
                                    if (hashMapMarker.size()>0) {
                                        Marker thisMarker = hashMapMarker.get(tracking.getDispName());
                                        thisMarker.remove();
                                        hashMapMarker.remove(tracking.getDispName());
                                    }

                                    //location for other user
                                    Location otherUser = new Location("");
                                    otherUser.setLatitude(Double.parseDouble(tracking.getLat()));
                                    otherUser.setLongitude(Double.parseDouble(tracking.getLon()));

                                    LatLng nearestPoint = findNearestPoint(otherUserLocation, mPolygonPoints);
                                    double dist = SphericalUtil.computeDistanceBetween(otherUserLocation, nearestPoint);//in meters
                                    //float abc = (float) (dist / 1000);

                                    MarkerOptions options = new MarkerOptions()
                                            .title(tracking.getDispName())
                                            .position(otherUserLocation)
                                            .snippet("Distance " + new DecimalFormat("#.#").format((dist / 1000)) + " KM")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                    Marker marker=mMap.addMarker(options);
                                    hashMapMarker.put(tracking.getDispName(),marker);
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(otherUserLocation, 18.0f));
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void setUpSystem() {
        isOnlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Boolean.class)) {

                    userProfileRef.document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot user = task.getResult();
                                user.toObject(UserProfile.class);

                                String role = (String) user.get("role").toString();

                                if (role.equals("Admin")) {
                                    offlineRef.document(mAuth.getCurrentUser().getUid()).delete();
                                } else {
                                    String geoZone = SharedPrefManager.getInstance(MapsTracking.this).getUserGeoZone();
                                    onlineRef.document(mAuth.getCurrentUser().getUid())
                                            .set(new User(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getUid(), geoZone, "Online"));
                                    offlineRef.document(mAuth.getCurrentUser().getUid()).delete();
                                }
                            }
                        }
                    });

                 /*   onlineRef.document("All")
                            .set(new User("All", "All","All", "Online"));*/

                    //onlineAdapter.notifyDataSetChanged();
                    //offlineAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        onlineRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        User user = snapshot.toObject(User.class);
                        Log.d(TAG, "" + user.getDispName() + " is " + user.getStatus());
                    }
                }
            }
        });
    }

    private LatLng findNearestPoint(LatLng test, List<LatLng> target) {
        double distance = -1;
        LatLng minimumDistancePoint = test;

        if (test == null || target == null) {
            return minimumDistancePoint;
        }

        for (int i = 0; i < target.size(); i++) {
            LatLng point = target.get(i);

            int segmentPoint = i + 1;
            if (segmentPoint >= target.size()) {
                segmentPoint = 0;
            }

            double currentDistance = PolyUtil.distanceToLine(test, point, target.get(segmentPoint));
            if (distance == -1 || currentDistance < distance) {
                distance = currentDistance;
                minimumDistancePoint = findNearestPoint(test, point, target.get(segmentPoint));
            }
        }

        return minimumDistancePoint;
    }

    private LatLng findNearestPoint(final LatLng p, final LatLng start, final LatLng end) {
        if (start.equals(end)) {
            return start;
        }

        final double s0lat = Math.toRadians(p.latitude);
        final double s0lng = Math.toRadians(p.longitude);
        final double s1lat = Math.toRadians(start.latitude);
        final double s1lng = Math.toRadians(start.longitude);
        final double s2lat = Math.toRadians(end.latitude);
        final double s2lng = Math.toRadians(end.longitude);

        double s2s1lat = s2lat - s1lat;
        double s2s1lng = s2lng - s1lng;
        final double u = ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
                / (s2s1lat * s2s1lat + s2s1lng * s2s1lng);
        if (u <= 0) {
            return start;
        }
        if (u >= 1) {
            return end;
        }

        return new LatLng(start.latitude + (u * (end.latitude - start.latitude)),
                start.longitude + (u * (end.longitude - start.longitude)));


    }

    private static ArrayList<LatLng> convertMarkersToLatlng(List<Marker> markers) {
        ArrayList<LatLng> points = new ArrayList<>();
        for (Marker marker : markers) {
            points.add(marker.getPosition());
        }
        return points;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*onlineRef.document(mAuth.getCurrentUser().getUid())
                .set(new User(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), FirebaseAuth.getInstance().getCurrentUser().getUid(), "Online"));` `
*/
    }

    @Override
    protected void onStop() {
        /*offline.document(mAuth.getCurrentUser().getUid())
                .set(new User(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), FirebaseAuth.getInstance().getCurrentUser().getUid(), "Offline"));
        onlineRef.document(mAuth.getCurrentUser().getUid()).delete();*/

       /* LocationUpdatesService mService = AppController.getInstance().mService;
        if (mService != null)
            mService.removeLocationUpdates();
*/
        if (listenerRegistration != null)
            listenerRegistration.remove();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpSystem();
        /*onlineRef.document(mAuth.getCurrentUser().getUid())
                .set(new User(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), FirebaseAuth.getInstance().getCurrentUser().getUid(), "Offline"));
        offline.document(mAuth.getCurrentUser().getUid()).delete();*/
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*offline.document(mAuth.getCurrentUser().getUid())
                .set(new User(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), FirebaseAuth.getInstance().getCurrentUser().getUid(), "Offline"));
        onlineRef.document(mAuth.getCurrentUser().getUid()).delete();*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
