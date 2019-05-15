package com.example.secureEye.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import static com.example.secureEye.Services.GeofenceTransitionIntentService.KEY_LATITUDE;
import static com.example.secureEye.Services.GeofenceTransitionIntentService.KEY_LONGITUDE;

public class Add_New_Geofence extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback {


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSION_LOCATION = 34839;

    private GoogleMap mMap;
    private List<Marker> mMarkers = new ArrayList<>();
    private List<LatLng> mLatLngList = new ArrayList<>();
    private Polygon mPolygon;
    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton btnAddGeofence;
    private AwesomeValidation validation;
    private CollectionReference geofenceList;
    private AutocompleteSupportFragment autocompleteFragment;
    private Button btnSatelliteView;
    private int btnBackground=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__new__geofence);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }
        PlacesClient placesClient = Places.createClient(this);
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.d(TAG, "onPlaceSelected: "+place.getName());
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        btnSatelliteView=findViewById(R.id.btnSateliteView);
        btnAddGeofence = findViewById(R.id.btnAddGeofence);
        validation = new AwesomeValidation(ValidationStyle.BASIC);
        geofenceList = Constant_URLS.GEOFENCE_LIST;


        if (mGoogleApiClient == null) {
            mGoogleApiClient = createGoogleApiClient();
        }
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        btnAddGeofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMarkers.size() <= 2) {
                    Toast.makeText(Add_New_Geofence.this, "Make geofence first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                showGeofenceDialog();
            }
        });

        btnSatelliteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMap!=null){
                    if (btnBackground==0){
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        btnSatelliteView.setBackgroundResource(R.drawable.default_view);
                        btnBackground++;
                    }else {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        btnSatelliteView.setBackgroundResource(R.drawable.satellite_view);
                        btnBackground--;
                    }
                }
            }
        });
    }

    private void showGeofenceDialog() {
        android.app.AlertDialog.Builder mBuilder = new android.app.AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.add_new_geofence_dialog, null);
        final EditText geofenceName = (EditText) mView.findViewById(R.id.etGeofenceName);
        validation.addValidation(this, R.id.etGeofenceName, "[a-zA-Z\\s]+", R.string.fenceNameError);
        Button btnSaveGeofence = (Button) mView.findViewById(R.id.btnSaveGeofence);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setTitle("New Geofence");
        dialog.show();
        btnSaveGeofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validation.validate()) {
                    Map<String, Object> newGeofence = new HashMap<>();
                    for (int i = 0; i < mMarkers.size(); i++) {
                        newGeofence.put(KEY_LATITUDE + i, mMarkers.get(i).getPosition().latitude);
                        newGeofence.put(KEY_LONGITUDE + i, mMarkers.get(i).getPosition().longitude);
                    }
                    geofenceList.document(geofenceName.getText().toString()).set(newGeofence).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Add_New_Geofence.this, "Geofence saved.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            mMap.clear();
                            mMarkers.clear();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                } else {
                    finish();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        //startActivity(new Intent(this, GeofenceEditorActivity.class));
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_LOCATION);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
        if (mLatLngList != null) {
            for (LatLng latLng : mLatLngList) {
                mMarkers.add(mMap.addMarker(new MarkerOptions().position(latLng).draggable(true)));
            }
            redraw();
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMarkers.add(mMap.addMarker(new MarkerOptions().position(latLng).draggable(true)));
        mMap.setOnMarkerDragListener(this);
        update();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        update();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed()");
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.d(TAG, "onResult()");
    }

    private void update() {
        if (mMarkers.size() <= 2) {
            return;
        }
        redraw();
    }

    private void redraw() {
        if (mMarkers.size() <= 2) {
            return;
        }
        if (mPolygon != null) {
            mPolygon.remove();
        }
        mPolygon = mMap.addPolygon(new PolygonOptions().addAll(convertMarkersToLatlng(mMarkers))
                .strokeColor(Color.RED).fillColor(Color.TRANSPARENT));
    }

    private GoogleApiClient createGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private static ArrayList<LatLng> convertMarkersToLatlng(List<Marker> markers) {
        ArrayList<LatLng> points = new ArrayList<>();
        for (Marker marker : markers) {
            points.add(marker.getPosition());
        }
        return points;
    }
}
