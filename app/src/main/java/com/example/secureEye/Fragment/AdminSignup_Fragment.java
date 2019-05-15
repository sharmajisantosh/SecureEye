package com.example.secureEye.Fragment;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.res.ResourcesCompat;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.secureEye.Activity.AdminNavigationDashboard;
import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SessionManager;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.Utils.SharedPrefManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.List;

import static com.example.secureEye.Utils.ImageConversion.decodeUri;

public class AdminSignup_Fragment extends Fragment {

    private String TAG = "AdminSignup_Fragment";

    private EditText etUserName, etPassword, etMobile;
    private Spinner geofenceSpinner, adminSpinner;
    private Button btnRegister;
    private ProgressDialog progressDialog1;
    private CountryCodePicker countryCode;
    private AwesomeValidation validation;
    private String phoneNumber;
    private String simpleNumber;
    private String cCode;
    private Uri choosenImage;
    private Bitmap bp;
    private FirebaseAuth mAuth;
    private String deviceToken = "";
    private CollectionReference geofenceRef, adminProfileRef;
    private List<String> geofenceList;
    private List<UserProfile> adminList;
    private ArrayAdapter<String> geofenceAdapter;
    private String adminGeozone;


    public AdminSignup_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_signup, container, false);

        mAuth = FirebaseAuth.getInstance();

        adminProfileRef = Constant_URLS.ADMIN_PROFILE_REF;
        geofenceRef = Constant_URLS.GEOFENCE_LIST;

        etUserName = view.findViewById(R.id.signupUserName);
        etPassword = view.findViewById(R.id.signupPassword);
        etMobile = view.findViewById(R.id.signupMobile);
        countryCode = view.findViewById(R.id.countryCode);
        btnRegister = view.findViewById(R.id.btnFragRegister);
        geofenceSpinner = view.findViewById(R.id.signupGeozoneSpinner);
        adminSpinner = view.findViewById(R.id.signupGeozoneSpinner);

        geofenceList = new ArrayList<>();
        adminList = new ArrayList<>();

        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.roboto_medium);
        countryCode.setTypeFace(typeface);
        countryCode.registerCarrierNumberEditText(etMobile);
        etUserName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        choosenImage = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.default_user)
                + '/' + getResources().getResourceTypeName(R.drawable.default_user)
                + '/' + getResources().getResourceEntryName(R.drawable.default_user));
        bp = decodeUri(choosenImage, 200, getActivity());

        setupGeofenceSpinner();


        return view;
    }

    private void setupGeofenceSpinner() {
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

        geofenceAdapter = new ArrayAdapter<String>(getActivity(), R.layout.geofence_spinner_item, R.id.tvGeofenceName, geofenceList);
        geofenceSpinner.setAdapter(geofenceAdapter);
        geofenceAdapter.notifyDataSetChanged();

        geofenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "spinner item " + geofenceList.get(position));
                //loadOnlineUsers(geofenceList.get(position));
                adminGeozone = geofenceList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

   /* private void loadOnlineUsers(String geofenceName) {
        userProfileRef.whereEqualTo("role","Admin").whereEqualTo("geoZone",geofenceName).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    adminList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        UserProfile user = snapshot.toObject(UserProfile.class);
                        adminList.add(user);
                        offlineAdapter.notifyDataSetChanged();
                        Log.d(TAG, "inside loadofflineuser : " + user.getDispName());
                    }
                }
            }
        });
    }*/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        validation = new AwesomeValidation(ValidationStyle.BASIC);
        validation.addValidation(getActivity(), R.id.signupUserName, "[a-zA-Z\\s]+", R.string.nameError);
        validation.addValidation(getActivity(), R.id.signupPassword, "((?=.*\\d)(?=.*[a-z])(?=.*[@#$%]).{6,20})", R.string.passwordError);
        validation.addValidation(getActivity(), R.id.signupMobile, "\\d{5}[\\s]\\d{5}", R.string.mobileError);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (SessionManager.isNetworkAvaliable(getActivity())) {
                    if (adminGeozone != null) {
                        if (validation.validate()) {
                            phoneNumber = countryCode.getFullNumberWithPlus();
                            simpleNumber = etMobile.getText().toString();
                            simpleNumber = simpleNumber.replaceAll("\\s+", "");
                            cCode = countryCode.getSelectedCountryCode();

                            //sendOtpOnPhone();
                            progressDialog1 = new ProgressDialog(getActivity());
                            progressDialog1.setCancelable(false);
                            progressDialog1.setMessage("Registering ...");
                            progressDialog1.show();

                            signinWithEmailPass();
                        }
                    }else {
                        Toast.makeText(getActivity(), "Select GeoZone for admin.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "No network found");
                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void signinWithEmailPass() {
        String email = phoneNumber + "@skyspirit.com";
        String pass = etPassword.getText().toString();
        getDeviceToken();
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    final FirebaseUser userInfo = task.getResult().getUser();
                    UserProfileChangeRequest chngName = new UserProfileChangeRequest.Builder()
                            .setDisplayName(etUserName.getText().toString()).build();
                    userInfo.updateProfile(chngName);

                    addUserInfoToFirebaseDatabase(task);
                    /*UpdatePhoneNumber updatePhoneNumber = new UpdatePhoneNumber(getActivity());
                    updatePhoneNumber.sendOTP(phoneNumber, new UpdatePhoneResult() {
                        @Override
                        public void onSuccess(@NonNull Task<Void> task1) {
                            if (task1.isSuccessful()) {
                                addUserInfoToFirebaseDatabase(task);
                            }
                        }

                        @Override
                        public void onFailed(String result) {

                        }
                    });*/
                }
            }
        });
    }

    private void addUserInfoToFirebaseDatabase(Task<AuthResult> task) {
        if (task.isSuccessful()) {

            DocumentReference newUserRef = adminProfileRef.document(mAuth.getCurrentUser().getUid());

            UserProfile userProfile = new UserProfile();
            userProfile.setName(etUserName.getText().toString());
            userProfile.setEmail(phoneNumber + "@skyspirit.com");
            userProfile.setFullPhoneNumber(phoneNumber);
            userProfile.setPassword(etPassword.getText().toString());
            userProfile.setGeoZone(adminGeozone);
            userProfile.setRole("Admin");
            userProfile.setDeviceToken(deviceToken);
            userProfile.setUserAdminMail("");
            newUserRef.set(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressDialog1.dismiss();
                        Intent intent = new Intent(getActivity(), AdminNavigationDashboard.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        getActivity().startActivity(intent);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "Some error occured.", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    public String getDeviceToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(getActivity(), new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                deviceToken = instanceIdResult.getToken();
                Log.d(TAG, " else Token= " + deviceToken);
                SharedPrefManager.getInstance(getActivity()).saveDeviceToken(deviceToken);
            }
        });
        return deviceToken;
    }
}
