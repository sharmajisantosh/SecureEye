package com.example.secureEye.Fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.secureEye.Activity.AdminNavigationDashboard;
import com.example.secureEye.Activity.MapsTracking;
import com.example.secureEye.Adapter.OnlineAdapter;
import com.example.secureEye.Adapter.SpinnerAdapter;
import com.example.secureEye.Interface.CheckboxCheckedListner;
import com.example.secureEye.Model.User;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SessionManager;
import com.example.secureEye.Utils.TypefaceSpan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.List;

public class AddNewUser extends Fragment {

    private static final String TAG = "AddNewUser";

    private CollectionReference geofenceRef, adminProfileRef, userProfileRef;
    private List<String> geofenceList;
    private List<UserProfile> adminList;
    private ArrayAdapter<String> geofenceAdapter;
    private SpinnerAdapter spinnerAdapter;
    private String userGeozone;
    private String cCode;
    private String userAdminMailId;
    private Spinner geofenceSpinner, adminSpinner;
    private FirebaseAuth mAuth;
    private EditText etUserName, etPassword, etMobile;
    private Button btnAddNewUser;
    private ProgressDialog progressDialog1;
    private CountryCodePicker countryCode;
    private AwesomeValidation validation;
    private String phoneNumber;
    private String simpleNumber;
    private int check=0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_new_guard, container, false);
        mAuth = FirebaseAuth.getInstance();
        SpannableString str = new SpannableString("New Guard");
        str.setSpan(new TypefaceSpan(getActivity(),TypefaceSpan.fontName),0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        adminProfileRef = Constant_URLS.ADMIN_PROFILE_REF;
        geofenceRef = Constant_URLS.GEOFENCE_LIST;
        userProfileRef=Constant_URLS.USER_PROFILE_REF;

        etUserName = view.findViewById(R.id.newUserName);
        etPassword = view.findViewById(R.id.newUserPassword);
        etMobile = view.findViewById(R.id.newUserMobile);
        countryCode = view.findViewById(R.id.newUserCountryCode);
        btnAddNewUser = view.findViewById(R.id.btnAddNewUser);
        geofenceSpinner = view.findViewById(R.id.newUserGeozoneSpinner);
        adminSpinner = view.findViewById(R.id.newUserAdminSpinner);

        etUserName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.roboto_medium);
        countryCode.setTypeFace(typeface);
        countryCode.registerCarrierNumberEditText(etMobile);

        geofenceList = new ArrayList<>();
        adminList = new ArrayList<>();

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
                if (++check>1) {
                    loadAdminList(geofenceList.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void loadAdminList(String newUserGeoZone) {
        userGeozone = newUserGeoZone;
        adminProfileRef.whereEqualTo("geoZone", newUserGeoZone).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    adminList.clear();
                    spinnerAdapter.notifyDataSetChanged();
                    List<DocumentSnapshot> geoList = task.getResult().getDocuments();
                    if (geoList.size() > 0) {
                        for (int i = 0; i < geoList.size(); i++) {
                            adminList.add(geoList.get(i).toObject(UserProfile.class));
                            spinnerAdapter.notifyDataSetChanged();
                        }
                    } else {
                    }
                }
            }
        });

        spinnerAdapter = new SpinnerAdapter(getActivity(), adminList);
        adminSpinner.setAdapter(spinnerAdapter);
        spinnerAdapter.notifyDataSetChanged();

        adminSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "spinner item " + geofenceList.get(position));
                if (++check>1) {
                    userAdminMailId = adminList.get(position).getEmail();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        validation = new AwesomeValidation(ValidationStyle.BASIC);
        validation.addValidation(getActivity(), R.id.newUserName, "[a-zA-Z\\s]+", R.string.nameError);
        validation.addValidation(getActivity(), R.id.newUserPassword, "((?=.*\\d)(?=.*[a-z])(?=.*[@#$%]).{6,20})", R.string.passwordError);
        validation.addValidation(getActivity(), R.id.newUserMobile, "\\d{5}[\\s]\\d{5}", R.string.mobileError);

        btnAddNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SessionManager.isNetworkAvaliable(getActivity())){
                    if (userGeozone!=null && userAdminMailId!=null){
                        if (validation.validate()){
                            phoneNumber = countryCode.getFullNumberWithPlus();
                            simpleNumber = etMobile.getText().toString();
                            simpleNumber = simpleNumber.replaceAll("\\s+", "");
                            cCode = countryCode.getSelectedCountryCode();

                            progressDialog1 = new ProgressDialog(getActivity());
                            progressDialog1.setCancelable(false);
                            progressDialog1.setMessage("Adding new User ...");
                            progressDialog1.show();

                            AddNewUserOnServer();

                        }
                    }else {
                        Toast.makeText(getActivity(), "No admin found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });



    }

    private void AddNewUserOnServer() {
        String email = phoneNumber + "@skyspirit.com";
        String pass = etPassword.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final FirebaseUser userInfo = task.getResult().getUser();
                    UserProfileChangeRequest chngName = new UserProfileChangeRequest.Builder()
                            .setDisplayName(etUserName.getText().toString()).build();
                    userInfo.updateProfile(chngName);

                    addNewUserToFirebaseDatabase(task);
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog1.dismiss();
                Toast.makeText(getActivity(), "Some error occured.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewUserToFirebaseDatabase(Task<AuthResult> task) {
        if (task.isSuccessful()) {

            DocumentReference newUserRef = userProfileRef.document(mAuth.getCurrentUser().getUid());

            UserProfile userProfile = new UserProfile();
            userProfile.setName(etUserName.getText().toString());
            userProfile.setEmail(phoneNumber + "@skyspirit.com");
            userProfile.setFullPhoneNumber(phoneNumber);
            userProfile.setPassword(etPassword.getText().toString());
            userProfile.setGeoZone(userGeozone);
            userProfile.setRole("User");
            userProfile.setDeviceToken("");
            userProfile.setUserAdminMail(userAdminMailId);
            newUserRef.set(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressDialog1.dismiss();
                        Toast.makeText(getActivity(), "User Added Successfully.", Toast.LENGTH_SHORT).show();
                        AdminNavDashboard navFrag1 = new AdminNavDashboard();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.NavFrameLayout, navFrag1);
                        ft.commit();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog1.dismiss();
                    Toast.makeText(getActivity(), "Some error occured.", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }


}
