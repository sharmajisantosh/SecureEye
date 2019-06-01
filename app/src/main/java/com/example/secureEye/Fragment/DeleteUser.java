package com.example.secureEye.Fragment;


import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

import com.example.secureEye.Adapter.SpinnerAdapter;
import com.example.secureEye.Model.User;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeleteUser extends Fragment {

    private static final String TAG = "DeleteUser";
    private FirebaseAuth mAuth;
    private CollectionReference userProfileRef, userAdminLinkRef, onlineUserRef;
    private StorageReference profilePicRef;
    private List<String> userListName, userListId;
    private List<User> onlineUserList;
    private ArrayAdapter<String> userNameSpinnerAdapter;
    private Spinner userListSpinner;
    private CircleImageView userProfilePic;
    private EditText etUserName,etMobile,etGeofence, etAdminName;
    private String userUid, userProfilePicUrl, userEmail, userPassword, userGeofence;
    private Button btnDeleteUser;
    private ProgressDialog progressDialog1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        View view=inflater.inflate(R.layout.fragment_delete_user, container, false);
        SpannableString str = new SpannableString("Delete User");
        str.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        userListSpinner = view.findViewById(R.id.deleteUserNameSpinner);
        userProfilePic = view.findViewById(R.id.deleteUserProfilePic);
        etUserName = view.findViewById(R.id.deleteUserName);
        etMobile = view.findViewById(R.id.deleteUserMobile);
        etGeofence = view.findViewById(R.id.deleteUserGeofence);
        etAdminName = view.findViewById(R.id.deleteUserAdmin);
        btnDeleteUser = view.findViewById(R.id.btnCurrentDeleteUser);

        userProfileRef = Constant_URLS.USER_PROFILE_REF;
        userAdminLinkRef = Constant_URLS.USER_ADMIN_LINK_REF;
        profilePicRef = Constant_URLS.PROFILE_PIC_STORAGE_REF;
        onlineUserRef=Constant_URLS.ONLINE_REF;

        userListName = new ArrayList<>();
        userListId = new ArrayList<>();
        onlineUserList=new ArrayList<>();

        String adminGeoZone = SharedPrefManager.getInstance(getActivity()).getUserGeoZone();
        setupUserNameListSpinner(adminGeoZone);

        btnDeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog1 = new ProgressDialog(getActivity());
                progressDialog1.setCancelable(false);
                progressDialog1.setMessage("Deleting User ...");
                progressDialog1.show();

                onlineUserRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            int count=0;
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                User user = snapshot.toObject(User.class);
                                onlineUserList.add(user);
                                if (user.getUid().equalsIgnoreCase(userUid)){
                                    count=1;
                                    break;
                                }
                            }

                            if (count!=1){
                                deleteSeletectedUser();
                                Log.d(TAG, "user is offline");
                            }else {
                                progressDialog1.dismiss();
                                Toast.makeText(getActivity(), "User is online. Make offline first.", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });

                Log.d(TAG, "onClick: delete user "+userUid);
            }
        });

        return view;
    }

    private void deleteSeletectedUser() {
        String adminId=mAuth.getUid();

        userProfileRef.document(userUid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                profilePicRef=FirebaseStorage.getInstance().getReferenceFromUrl(userProfilePicUrl);
                profilePicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mAuth.signOut();
                        mAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                FirebaseUser user1 = mAuth.getCurrentUser();
                                user1.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mAuth.signOut();
                                            String email = SharedPrefManager.getInstance(getActivity()).getUserEmail();
                                            String password = SharedPrefManager.getInstance(getActivity()).getUserPassword();
                                            mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                                @Override
                                                public void onSuccess(AuthResult authResult) {
                                                    Toast.makeText(getActivity(), "User Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                    AdminNavDashboard navFrag1 = new AdminNavDashboard();
                                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                                    ft.replace(R.id.NavFrameLayout, navFrag1);
                                                    ft.commit();
                                                    progressDialog1.dismiss();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        userAdminLinkRef.document(userGeofence).collection(adminId).document(userUid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

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
                    userUid=userId;
                    UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                    Picasso.get().load(userProfile.getProfilePic()).into(userProfilePic);
                    etUserName.setText(userProfile.getName());
                    etMobile.setText(userProfile.getFullPhoneNumber());
                    etGeofence.setText(userProfile.getGeoZone());
                    userGeofence=userProfile.getGeoZone();
                    etAdminName.setText(mAuth.getCurrentUser().getDisplayName());
                    userProfilePicUrl=userProfile.getProfilePic();
                    userEmail=userProfile.getEmail();
                    userPassword=userProfile.getPassword();

                }
            }
        });
    }


}
