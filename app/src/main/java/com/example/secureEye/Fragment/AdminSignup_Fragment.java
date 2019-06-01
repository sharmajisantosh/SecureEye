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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.secureEye.Activity.AdminNavigationDashboard;
import com.example.secureEye.Interface.StartMyActivity;
import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SaveDefaultValueForAll;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.example.secureEye.Utils.ImageConversion.decodeUri;

public class AdminSignup_Fragment extends Fragment {

    private String TAG = "AdminSignup_Fragment";
    private EditText etUserName, etPassword, etMobile;
    private Spinner geofenceSpinner;
    private Button btnRegister;
    private ProgressDialog progressDialog1;
    private CountryCodePicker countryCode;
    private AwesomeValidation validation;
    private CircleImageView adminProfilePic;
    private String phoneNumber;
    private String simpleNumber;
    private FirebaseAuth mAuth;
    private String deviceToken = "";
    private CollectionReference geofenceRef, adminProfileRef;
    private StorageReference profilePicRef;
    private List<String> geofenceList;
    private ArrayAdapter<String> geofenceAdapter;
    private String adminGeozone;
    private Uri choosenImage;
    private String email;
    private String pass ;
    private String profilePicUrl = "";


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
        profilePicRef = Constant_URLS.PROFILE_PIC_STORAGE_REF;

        etUserName = view.findViewById(R.id.signupUserName);
        etPassword = view.findViewById(R.id.signupPassword);
        etMobile = view.findViewById(R.id.signupMobile);
        countryCode = view.findViewById(R.id.countryCode);
        btnRegister = view.findViewById(R.id.btnFragRegister);
        geofenceSpinner = view.findViewById(R.id.signupGeozoneSpinner);
        adminProfilePic = view.findViewById(R.id.adminProfilePic);

        geofenceList = new ArrayList<>();

        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.roboto_medium);
        countryCode.setTypeFace(typeface);
        countryCode.registerCarrierNumberEditText(etMobile);
        etUserName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        setupGeofenceSpinner();

        adminProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(getContext(), AdminSignup_Fragment.this);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                choosenImage = result.getUri();
                if (choosenImage != null) {
                    ((CircleImageView) getView().findViewById(R.id.adminProfilePic)).setImageURI(result.getUri());
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(getActivity(), "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void setupGeofenceSpinner() {
        geofenceRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    geofenceList.clear();
                    geofenceList.add("Select");
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
                if (!geofenceList.get(position).equalsIgnoreCase("Select")) {
                    adminGeozone = geofenceList.get(position);
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
        validation.addValidation(getActivity(), R.id.signupUserName, "[a-zA-Z\\s]+", R.string.nameError);
        validation.addValidation(getActivity(), R.id.signupPassword, "((?=.*\\d)(?=.*[a-z])(?=.*[@#$%]).{6,20})", R.string.passwordError);
        validation.addValidation(getActivity(), R.id.signupMobile, "\\d{5}[\\s]\\d{5}", R.string.mobileError);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (SessionManager.isNetworkAvaliable(getActivity())) {
                    if (adminGeozone != null && choosenImage != null) {
                        if (validation.validate()) {
                            phoneNumber = countryCode.getFullNumberWithPlus();
                            simpleNumber = etMobile.getText().toString();
                            simpleNumber = simpleNumber.replaceAll("\\s+", "");

                            //sendOtpOnPhone();
                            progressDialog1 = new ProgressDialog(getActivity());
                            progressDialog1.setCancelable(false);
                            progressDialog1.setMessage("Registering ...");
                            progressDialog1.show();

                            signinWithEmailPass();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Select GeoZone or profile pic for admin.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "No network found");
                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void signinWithEmailPass() {
        email = phoneNumber + "@skyspirit.com";
        pass = etPassword.getText().toString();
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

            StorageReference newProfilePic = profilePicRef.child(mAuth.getCurrentUser().getUid()).child("profilePic.jpg");
            newProfilePic.putFile(choosenImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    newProfilePic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            profilePicUrl = uri.toString();
                            final FirebaseUser userInfo = mAuth.getCurrentUser();
                            UserProfileChangeRequest chngProfilePic = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(uri).build();

                            userInfo.updateProfile(chngProfilePic);
                            SaveOtherData();

                            Log.d(TAG, "onSuccess: image url is " + uri.toString());
                        }

                        private void SaveOtherData() {

                            DocumentReference newUserRef = adminProfileRef.document(mAuth.getCurrentUser().getUid());
                            UserProfile userProfile = new UserProfile();
                            userProfile.setName(etUserName.getText().toString());
                            userProfile.setEmail(email);
                            userProfile.setFullPhoneNumber(phoneNumber);
                            userProfile.setPassword(pass);
                            userProfile.setGeoZone(adminGeozone);
                            userProfile.setRole("Admin");
                            userProfile.setDeviceToken(deviceToken);
                            userProfile.setUserAdminMail("");
                            userProfile.setUid(mAuth.getUid());
                            userProfile.setProfilePic(profilePicUrl);

                            newUserRef.set(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog1.dismiss();
                                        SaveDefaultValueForAll.saveDefaults(getActivity(), "Admin",email,pass, new StartMyActivity() {
                                            @Override
                                            public void startThisActivity(String userType) {
                                                Intent intent = new Intent(getActivity(), AdminNavigationDashboard.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                progressDialog1.dismiss();
                                            }
                                        });
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Some error occured.", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    });
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
                SharedPrefManager.getInstance(getActivity()).saveSelfDeviceToken(deviceToken);
            }
        });
        return deviceToken;
    }
}
