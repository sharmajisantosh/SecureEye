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

import android.os.Handler;
import android.text.Editable;
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
import com.example.secureEye.Activity.MainActivity;
import com.example.secureEye.Adapter.SpinnerAdapter;
import com.example.secureEye.Model.Tracking;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class UpdateUser extends Fragment {

    private static final String TAG = "UpdateUser";
    private FirebaseAuth mAuth;
    private CollectionReference geofenceRef, adminProfileRef, userProfileRef, userAdminLinkRef;
    private StorageReference profilePicRef;
    private Spinner userListSpinner, adminSpinner, geofenceSpinner;
    private List<String> userListName, userListId, geofenceList;
    private ArrayAdapter<String> userNameSpinnerAdapter, geofenceAdapter;
    private CircleImageView userProfilePic;
    private EditText etNewUserName, etMobile;
    private CountryCodePicker countryCode;
    private Button btnUpdateUser;
    private List<UserProfile> adminList;
    private SpinnerAdapter spinnerAdapter;
    private String newUserAdminMailId, newUserAdminUid, newUserGeozone, newUserName, newPhoneNumber;
    private ProgressDialog progressDialog1;
    private AwesomeValidation validation;
    private String oldUserAdminMail, oldUserAdminUid, oldGeoZone, oldUserName, oldPhoneNumber, olUserEmail, userPassword, userUid;
    private Uri choosenImage;
    private Handler handler = new Handler();
    private Runnable r;
    private int count = 0, counter = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_user, container, false);
        mAuth = FirebaseAuth.getInstance();
        SpannableString str = new SpannableString("Update User");
        str.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        adminProfileRef = Constant_URLS.ADMIN_PROFILE_REF;
        geofenceRef = Constant_URLS.GEOFENCE_LIST;
        userProfileRef = Constant_URLS.USER_PROFILE_REF;
        userAdminLinkRef = Constant_URLS.USER_ADMIN_LINK_REF;
        profilePicRef = Constant_URLS.PROFILE_PIC_STORAGE_REF;

        userListSpinner = view.findViewById(R.id.modifyUserNameSpinner);
        userProfilePic = view.findViewById(R.id.modifyUserProfilePic);
        etNewUserName = view.findViewById(R.id.modifyUserName);
        etMobile = view.findViewById(R.id.modifyUserMobile);
        countryCode = view.findViewById(R.id.modifyUserCountryCode);
        btnUpdateUser = view.findViewById(R.id.btnUpdateUser);
        geofenceSpinner = view.findViewById(R.id.modifyUserGeozoneSpinner);
        adminSpinner = view.findViewById(R.id.modifyUserAdminSpinner);

        userListName = new ArrayList<>();
        userListId = new ArrayList<>();
        geofenceList = new ArrayList<>();
        adminList = new ArrayList<>();

        etNewUserName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.roboto_light);
        countryCode.setTypeFace(typeface);
        countryCode.registerCarrierNumberEditText(etMobile);

        String adminGeoZone = SharedPrefManager.getInstance(getActivity()).getUserGeoZone();
        setupUserNameListSpinner(adminGeoZone);
        setupGeofenceSpinner();

        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(getContext(), UpdateUser.this);
            }
        });

        return view;
    }

    private void updateAllData() {

        if (SessionManager.isNetworkAvaliable(getActivity())) {

            if (validation.validate()) {

                progressDialog1 = new ProgressDialog(getActivity());
                progressDialog1.setCancelable(false);
                progressDialog1.setMessage("Updating User ...");
                progressDialog1.show();

                newUserName = etNewUserName.getText().toString();
                newPhoneNumber = countryCode.getFullNumberWithPlus();

                if (!oldPhoneNumber.equalsIgnoreCase(newPhoneNumber)) {
                    Log.d(TAG, "inside old number: old= " + oldPhoneNumber + "   new= " + newPhoneNumber);
                    count++;
                    updatePhoneAndEmail(newPhoneNumber);

                }
                if (!oldUserName.equalsIgnoreCase(newUserName)) {
                    Log.d(TAG, "inside else old name: old= " + oldUserName + "   new= " + newUserName);
                    count++;
                    updatedNameOnServer();
                }

                if (!oldUserAdminMail.equalsIgnoreCase(newUserAdminMailId)) {
                    Log.d(TAG, "inside old admin mail: old= " + oldUserAdminMail + "   new= " + newUserAdminMailId);
                    count++;
                    updateUserAdminLink();
                }

                if (choosenImage != null) {
                    Log.d(TAG, "inside new image: old= " + choosenImage.toString());
                    count++;
                    updateProfilePic();
                }


                Log.d(TAG, "new name  " + newUserName);
                Log.d(TAG, "new phone  " + newPhoneNumber);
                Log.d(TAG, "new adminmail  " + newUserAdminMailId);
                Log.d(TAG, "new geozone  " + newUserGeozone);
                Log.d(TAG, "new adminUid  " + newUserAdminUid);
                Log.d(TAG, "new userUid  " + userUid);

                handler = new Handler();
                r = new Runnable() {
                    @Override
                    public void run() {
                        handler.postDelayed(this, 2000);
                        if (counter == count) {
                            progressDialog1.dismiss();
                            Toast.makeText(getActivity(), "User Updated", Toast.LENGTH_SHORT).show();

                            AdminNavDashboard navFrag1 = new AdminNavDashboard();
                            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.NavFrameLayout, navFrag1);
                            ft.commit();

                            handler.removeCallbacks(this);
                        } else {
                            Log.d(TAG, "inside handler: " + counter + "/" + count);
                        }
                    }
                };
                handler.postDelayed(r, 2000);

            } else {
                Toast.makeText(getActivity(), "Fill all fields", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatedNameOnServer() {
        userProfileRef.document(userUid).update("name", newUserName).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "updated userProfileRef");
                userAdminLinkRef.document(newUserGeozone).collection(newUserAdminUid).document(userUid).update("userName",newUserName).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        counter++;
                    }
                });
            }
        });
    }

    private void updateProfilePic() {

        StorageReference newProfilePic = profilePicRef.child(userUid).child("profilePic.jpg");
        newProfilePic.putFile(choosenImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                newProfilePic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String profilePicUrl = uri.toString();
                        userProfileRef.document(userUid).update("profilePic", profilePicUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                counter++;
                            }
                        });
                    }
                });
            }
        });

    }

    private void updateUserAdminLink() {
        HashMap<String, Object> newUserAdminLink = new HashMap<>();
        newUserAdminLink.put("userUid", userUid);
        newUserAdminLink.put("userAdminMailId", newUserAdminMailId);
        newUserAdminLink.put("userName", newUserName);
        newUserAdminLink.put("userMailId", newPhoneNumber + "@skyspirit.com");
        userAdminLinkRef.document(newUserGeozone).collection(newUserAdminUid).document(userUid).set(newUserAdminLink).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "updated user admin link");
                userAdminLinkRef.document(oldGeoZone).collection(oldUserAdminUid).document(userUid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "delete old user admin link");
                        HashMap<String, Object> updatedData = new HashMap<>();
                        updatedData.put("geoZone", newUserGeozone);
                        updatedData.put("userAdminMail", newUserAdminMailId);
                        userProfileRef.document(userUid).update(updatedData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "updated userProfileRef");
                                counter++;
                            }
                        });
                    }
                });
            }
        });
    }

    private void updatePhoneAndEmail(String newPhoneNumber) {

        mAuth.signOut();
        mAuth.signInWithEmailAndPassword(olUserEmail, userPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseUser user1 = mAuth.getCurrentUser();
                String userEmail=newPhoneNumber + "@skyspirit.com";
                user1.updateEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mAuth.signOut();
                            String email = SharedPrefManager.getInstance(getActivity()).getUserEmail();
                            String password = SharedPrefManager.getInstance(getActivity()).getUserPassword();
                            mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    HashMap<String, Object> updatedData = new HashMap<>();
                                    updatedData.put("email", userEmail);
                                    updatedData.put("fullPhoneNumber", newPhoneNumber);
                                    userProfileRef.document(userUid).update(updatedData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            counter++;
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                                    progressDialog1.dismiss();
                                    handler.removeCallbacks(r);
                                    adminLoginAgain();
                                }
                            });
                        }else {
                            Toast.makeText(getActivity(), "Phone Number already exists", Toast.LENGTH_SHORT).show();
                            progressDialog1.dismiss();
                            handler.removeCallbacks(r);
                            adminLoginAgain();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Phone Number already exist.", Toast.LENGTH_SHORT).show();
                        progressDialog1.dismiss();
                        handler.removeCallbacks(r);
                        adminLoginAgain();
                    }
                });
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        validation = new AwesomeValidation(ValidationStyle.BASIC);
        validation.addValidation(getActivity(), R.id.modifyUserName, "[a-zA-Z\\s]+", R.string.nameError);
        validation.addValidation(getActivity(), R.id.modifyUserMobile, "\\d{5}[\\s]\\d{5}", R.string.mobileError);

        btnUpdateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateAllData();
            }
        });
    }




    private void setupUserNameListSpinner(String adminGeoZone) {
        userAdminLinkRef.document(adminGeoZone).collection(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    userListName.clear();
                    userListId.clear();
                    userNameSpinnerAdapter.notifyDataSetChanged();
                    List<DocumentSnapshot> userList = task.getResult().getDocuments();
                    if (userList.size() > 0) {
                        userListName.add("Select");
                        userListId.add("Select");
                        for (int i = 0; i < userList.size(); i++) {
                            userListName.add(userList.get(i).get("userName").toString());
                            userListId.add(userList.get(i).get("userUid").toString());
                            userNameSpinnerAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getActivity(), "You have no users", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        userNameSpinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.geofence_spinner_item, R.id.tvGeofenceName, userListName);
        userListSpinner.setAdapter(userNameSpinnerAdapter);
        userNameSpinnerAdapter.notifyDataSetChanged();

        userListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "spinner item " + userListName.get(position));
                Log.d(TAG, "onItemSelected: " + userListName.get(position));
                Log.d(TAG, "onItemSelected: " + userListId.get(position));
                loadUsersProfile(userListId.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadUsersProfile(String userId) {

        final ProgressDialog progressDialog1 = new ProgressDialog(getActivity());
        progressDialog1.setCancelable(false);
        progressDialog1.setMessage("Please wait...");
        //progressDialog1.show();
        userProfileRef.document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                    Picasso.get().load(userProfile.getProfilePic()).into(userProfilePic);
                    etNewUserName.setText(userProfile.getName());

                    oldPhoneNumber = userProfile.getFullPhoneNumber();
                    oldUserName = userProfile.getName();
                    olUserEmail = userProfile.getEmail();
                    oldGeoZone = userProfile.getGeoZone();
                    oldUserAdminMail = userProfile.getUserAdminMail();
                    oldUserAdminUid = mAuth.getUid();
                    userPassword = userProfile.getPassword();
                    userUid = userProfile.getUid();


                    String phoneNumber = getPhoneNumber(userProfile.getFullPhoneNumber());
                    String cCode = getCountryCode(userProfile.getFullPhoneNumber());

                    countryCode.setCountryForNameCode("IN");
                    etMobile.setText(phoneNumber);

                    geofenceAdapter.notifyDataSetChanged();

                    for (int i = 0; i < geofenceList.size(); i++) {
                        if (oldGeoZone.equalsIgnoreCase(geofenceList.get(i))) {
                            geofenceSpinner.setSelection(i);
                            break;
                        }
                    }

                    progressDialog1.dismiss();

                }
            }
        });
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
                        //geofenceAdapter.notifyDataSetChanged();
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
                    newUserGeozone = geofenceList.get(position);
                } else {
                    adminList.clear();
                    spinnerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadAdminList(String newUserGeoZone) {
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
                            if (geoList.get(i).toObject(UserProfile.class).getEmail().equalsIgnoreCase(oldUserAdminMail)) {
                                adminSpinner.setSelection(i + 1);
                            }
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
                Log.d(TAG, "spinner item " + adminList.get(position));
                if (position != 0) {
                    newUserAdminMailId = adminList.get(position).getEmail();
                    newUserAdminUid = adminList.get(position).getUid();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                choosenImage = result.getUri();
                if (choosenImage != null) {
                    ((CircleImageView) getView().findViewById(R.id.modifyUserProfilePic)).setImageURI(result.getUri());
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(getActivity(), "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void adminLoginAgain() {
        String email = SharedPrefManager.getInstance(getActivity()).getUserEmail();
        String password = SharedPrefManager.getInstance(getActivity()).getUserPassword();
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

            }
        });
    }

    private String getCountryCode(String phoneNumber) {
        String onlyCCode = "";
        if (phoneNumber.startsWith("+")) {
            if (phoneNumber.length() == 13) {
                onlyCCode = phoneNumber.substring(0, 3);
            } else if (phoneNumber.length() == 14) {
                onlyCCode = phoneNumber.substring(0, 4);
            }
        }
        return onlyCCode;
    }

    private String getPhoneNumber(String phoneNumber) {
        String onlyPhoneNumber = "";
        if (phoneNumber.startsWith("+")) {
            if (phoneNumber.length() == 13) {
                onlyPhoneNumber = phoneNumber.substring(3);
            } else if (phoneNumber.length() == 14) {
                onlyPhoneNumber = phoneNumber.substring(4);
            }
        }
        return onlyPhoneNumber;
    }
}
