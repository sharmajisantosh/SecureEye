package com.example.secureEye.Fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.secureEye.Adapter.SpinnerAdapter;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SaveDefaultValueForAll;
import com.example.secureEye.Utils.SessionManager;
import com.example.secureEye.Utils.SharedPrefManager;
import com.example.secureEye.Utils.TypefaceSpan;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class AddNewUser extends Fragment {

    private static final String TAG = "AddNewUser";

    private CollectionReference geofenceRef, adminProfileRef, userProfileRef, userAdminLinkRef;
    private StorageReference profilePicRef;
    private List<String> geofenceList;
    private List<UserProfile> adminList;
    private ArrayAdapter<String> geofenceAdapter;
    private SpinnerAdapter spinnerAdapter;
    private CircleImageView userProfilePic;
    private String userGeozone;
    private String userAdminMailId, userAdminUid;
    private Spinner geofenceSpinner, adminSpinner;
    private FirebaseAuth mAuth;
    private EditText etUserName, etPassword, etMobile;
    private Button btnAddNewUser;
    private ProgressDialog progressDialog1;
    private CountryCodePicker countryCode;
    private AwesomeValidation validation;
    private String phoneNumber;
    private String simpleNumber;
    private Uri choosenImage;
    private String profilePicUrl = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_new_user, container, false);
        mAuth = FirebaseAuth.getInstance();
        SpannableString str = new SpannableString("New User");
        str.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        adminProfileRef = Constant_URLS.ADMIN_PROFILE_REF;
        geofenceRef = Constant_URLS.GEOFENCE_LIST;
        userProfileRef = Constant_URLS.USER_PROFILE_REF;
        userAdminLinkRef = Constant_URLS.USER_ADMIN_LINK_REF;
        profilePicRef = Constant_URLS.PROFILE_PIC_STORAGE_REF;

        etUserName = view.findViewById(R.id.newUserName);
        etPassword = view.findViewById(R.id.newUserPassword);
        etMobile = view.findViewById(R.id.newUserMobile);
        countryCode = view.findViewById(R.id.newUserCountryCode);
        btnAddNewUser = view.findViewById(R.id.btnAddNewUser);
        geofenceSpinner = view.findViewById(R.id.newUserGeozoneSpinner);
        adminSpinner = view.findViewById(R.id.newUserAdminSpinner);
        userProfilePic = view.findViewById(R.id.userProfilePic);

        etUserName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.roboto_light);
        countryCode.setTypeFace(typeface);
        countryCode.registerCarrierNumberEditText(etMobile);

        geofenceList = new ArrayList<>();
        adminList = new ArrayList<>();

        setupGeofenceSpinner();

        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(getContext(), AddNewUser.this);
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
                    ((CircleImageView) getView().findViewById(R.id.userProfilePic)).setImageURI(result.getUri());
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
                    List<DocumentSnapshot> geoList = task.getResult().getDocuments();
                    geofenceList.add("Select");
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
                        adminList.add(new UserProfile("Select"));
                        for (int i = 0; i < geoList.size(); i++) {
                            adminList.add(geoList.get(i).toObject(UserProfile.class));
                            spinnerAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getActivity(), "No Admin found", Toast.LENGTH_SHORT).show();
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
                if (position != 0) {
                    userAdminMailId = adminList.get(position).getEmail();
                    userAdminUid = adminList.get(position).getUid();
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

                if (SessionManager.isNetworkAvaliable(getActivity())) {
                    if (userGeozone != null && userAdminMailId != null && choosenImage != null) {
                        if (validation.validate()) {
                            phoneNumber = countryCode.getFullNumberWithPlus();
                            simpleNumber = etMobile.getText().toString();
                            simpleNumber = simpleNumber.replaceAll("\\s+", "");

                            progressDialog1 = new ProgressDialog(getActivity());
                            progressDialog1.setCancelable(false);
                            progressDialog1.setMessage("Adding new User ...");
                            progressDialog1.show();

                            AddNewUserOnServer();

                        }
                    } else {
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

                            DocumentReference newUserRef = userProfileRef.document(mAuth.getCurrentUser().getUid());

                            UserProfile userProfile = new UserProfile();
                            userProfile.setName(etUserName.getText().toString());
                            userProfile.setEmail(phoneNumber + "@skyspirit.com");
                            userProfile.setFullPhoneNumber(phoneNumber);
                            userProfile.setPassword(etPassword.getText().toString());
                            userProfile.setGeoZone(userGeozone);
                            userProfile.setRole("User");
                            userProfile.setDeviceToken("");
                            userProfile.setUid(mAuth.getUid());
                            userProfile.setUserAdminMail(userAdminMailId);
                            userProfile.setProfilePic(profilePicUrl);

                            newUserRef.set(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        mAuth.signOut();
                                        String email = SharedPrefManager.getInstance(getActivity()).getUserEmail();
                                        String password = SharedPrefManager.getInstance(getActivity()).getUserPassword();
                                        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {
                                                progressDialog1.dismiss();
                                                Toast.makeText(getActivity(), "User Added Successfully.", Toast.LENGTH_SHORT).show();
                                                AdminNavDashboard navFrag1 = new AdminNavDashboard();
                                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                                ft.replace(R.id.NavFrameLayout, navFrag1);
                                                ft.commit();
                                            }
                                        });
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog1.dismiss();
                                    Toast.makeText(getActivity(), "Some error occured.", Toast.LENGTH_SHORT).show();
                                }
                            });

                            HashMap<String, Object> newUserAdminLink = new HashMap<>();
                            newUserAdminLink.put("userUid", mAuth.getCurrentUser().getUid());
                            newUserAdminLink.put("userAdminMailId", userAdminMailId);
                            newUserAdminLink.put("userName", etUserName.getText().toString());
                            newUserAdminLink.put("userMailId", phoneNumber + "@skyspirit.com");
                            userAdminLinkRef.document(userGeozone).collection(userAdminUid).document(mAuth.getCurrentUser().getUid()).set(newUserAdminLink).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                        }
                    });
                }
            });
        }
    }
}
