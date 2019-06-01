package com.example.secureEye.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.secureEye.Activity.AdminNavigationDashboard;
import com.example.secureEye.Activity.MainActivity;
import com.example.secureEye.Activity.UserNavigationDashboard;
import com.example.secureEye.Interface.StartMyActivity;
import com.example.secureEye.Model.User;
import com.example.secureEye.Model.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import javax.annotation.Nullable;

public class SaveDefaultValueForAll {
    private static String deviceToken;
    private static final String TAG = "SaveDefaultValueForAll";
    private static FirebaseAuth mAuth;
    private static String userGeoZone;
    private static String userRole;
    private static String userAdminMail;
    private static String userSelfDeviceTokenId;
    private static String userProfilePicUrl;
    private static String userAdminId;
    private static String userUid;
    private static CollectionReference onlineRef, offlineRef, userProfileRef, adminProfileRef;




    public static void saveDefaults(Context context, String loginType, String email, String password, StartMyActivity startMyActivity) {
        mAuth = FirebaseAuth.getInstance();
        onlineRef = Constant_URLS.ONLINE_REF;
        offlineRef = Constant_URLS.OFFLINE_REF;
        userProfileRef = Constant_URLS.USER_PROFILE_REF;
        adminProfileRef=Constant_URLS.ADMIN_PROFILE_REF;
        getSelfDeviceToken(context);

        if (loginType.equalsIgnoreCase("Admin")){


            SharedPrefManager.getInstance(context).saveUserType(loginType);
            Log.d(TAG, "in admin onComplete: " + deviceToken);
            adminProfileRef.document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                        userGeoZone = userProfile.getGeoZone();
                        userRole = userProfile.getRole();
                        userAdminMail = userProfile.getUserAdminMail();
                        userSelfDeviceTokenId = userProfile.getDeviceToken();
                        userProfilePicUrl=userProfile.getProfilePic();
                        userUid=userProfile.getUid();

                        SharedPrefManager.getInstance(context).saveUserGeoZone(userGeoZone);
                        SharedPrefManager.getInstance(context).saveUserRole(userRole);
                        SharedPrefManager.getInstance(context).saveUserAdminMail(userAdminMail);
                        SharedPrefManager.getInstance(context).saveUserProfilePic(userProfilePicUrl);
                        SharedPrefManager.getInstance(context).saveUserEmail(email);
                        SharedPrefManager.getInstance(context).saveUserPassword(password);
                        SharedPrefManager.getInstance(context).saveUserUid(userUid);

                        offlineRef.document(mAuth.getCurrentUser().getUid()).delete();
                        onlineRef.document(mAuth.getCurrentUser().getUid())
                                .set(new User(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getUid(), userGeoZone, "Online")).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (deviceToken.equals(userSelfDeviceTokenId)) {
                                    startMyActivity.startThisActivity("Admin");
                                } else {
                                    adminProfileRef.document(mAuth.getCurrentUser().getUid()).update("deviceToken", deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            startMyActivity.startThisActivity("Admin");
                                        }
                                    });
                                }
                            }
                        });
                    }else {
                        startMyActivity.startThisActivity("NoUser");
                        Log.d(TAG, "admin no user");
                    }
                }
            });
        }else {
            SharedPrefManager.getInstance(context).saveUserType(loginType);
            Log.d(TAG, "onComplete: "+deviceToken);
            userProfileRef.document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                        userGeoZone = userProfile.getGeoZone();
                        userRole = userProfile.getRole();
                        userAdminMail = userProfile.getUserAdminMail();
                        userSelfDeviceTokenId = userProfile.getDeviceToken();
                        userProfilePicUrl=userProfile.getProfilePic();

                        SharedPrefManager.getInstance(context).saveUserGeoZone(userGeoZone);
                        SharedPrefManager.getInstance(context).saveUserRole(userRole);
                        SharedPrefManager.getInstance(context).saveUserAdminMail(userAdminMail);
                        SharedPrefManager.getInstance(context).saveUserProfilePic(userProfilePicUrl);
                        SharedPrefManager.getInstance(context).saveUserEmail(email);
                        SharedPrefManager.getInstance(context).saveUserPassword(password);

                        offlineRef.document(mAuth.getCurrentUser().getUid()).delete();
                        onlineRef.document(mAuth.getCurrentUser().getUid())
                                .set(new User(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getUid(), userGeoZone, "Online")).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: others");
                                adminProfileRef.whereEqualTo("email",userAdminMail).addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                        if (!queryDocumentSnapshots.isEmpty()){
                                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                userAdminId=snapshot.getId();
                                                SharedPrefManager.getInstance(context).saveUserAdminId(userAdminId);
                                                startMyActivity.startThisActivity("Others");
                                            }
                                        }
                                    }
                                });
                            }
                        });
                        if (deviceToken!=null) {
                            if (deviceToken.equals(userSelfDeviceTokenId)) {

                            } else {
                                userProfileRef.document(mAuth.getCurrentUser().getUid()).update("deviceToken", deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
                            }
                        }else {
                            getSelfDeviceToken(context);
                        }
                    }else {
                        startMyActivity.startThisActivity("NoUser");
                        Log.d(TAG, "other no user");
                    }
                }
            });

        }
    }

    public static String getSelfDeviceToken(Context context) {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                deviceToken = instanceIdResult.getToken();
                SharedPrefManager.getInstance(context).saveSelfDeviceToken(deviceToken);
            }
        });
        return deviceToken;
    }
}
