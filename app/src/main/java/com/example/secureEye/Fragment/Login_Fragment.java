package com.example.secureEye.Fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.secureEye.Activity.AdminNavigationDashboard;
import com.example.secureEye.Activity.MainActivity;
import com.example.secureEye.Activity.UserNavigationDashboard;
import com.example.secureEye.Interface.StartMyActivity;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SaveDefaultValueForAll;
import com.example.secureEye.Utils.SessionManager;
import com.example.secureEye.Utils.SharedPrefManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hbb20.CountryCodePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class Login_Fragment extends Fragment {

    private String TAG = "Login_Fragment";
    private TextView forgotPwd;
    private FragmentManager fragmentManager;
    private EditText etUserName, etMobile, etPassword;
    private CollectionReference userProfileRef, adminProfileRef;
    private Button btnLogin;
    private CountryCodePicker countryCode;
    private AwesomeValidation validation;
    private FirebaseAuth mAuth;
    private String loginType;


    public Login_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        forgotPwd = view.findViewById(R.id.forgotPwd);
        fragmentManager = getActivity().getSupportFragmentManager();

        etUserName = view.findViewById(R.id.loginUserName);
        etPassword = view.findViewById(R.id.loginPassword);
        etMobile = view.findViewById(R.id.loginMobile);
        btnLogin = view.findViewById(R.id.btnFragLogin);
        countryCode = view.findViewById(R.id.loginCountryCode);

        mAuth = FirebaseAuth.getInstance();
        userProfileRef = Constant_URLS.USER_PROFILE_REF;
        adminProfileRef = Constant_URLS.ADMIN_PROFILE_REF;

        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.roboto_medium);
        countryCode.setTypeFace(typeface);
        countryCode.registerCarrierNumberEditText(etMobile);

        etUserName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        forgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.frameContainer, new ForgotPassword_Fragment()).commit();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        validation = new AwesomeValidation(ValidationStyle.BASIC);
        validation.addValidation(getActivity(), R.id.loginUserName, "[a-zA-Z\\s]+", R.string.nameError);
        validation.addValidation(getActivity(), R.id.loginPassword, "((?=.*\\d)(?=.*[a-z]).{6,20})", R.string.passwordError);
        validation.addValidation(getActivity(), R.id.loginMobile, "\\d{5}[\\s]\\d{5}", R.string.mobileError);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SessionManager.isNetworkAvaliable(getActivity())) {
                    if (validation.validate()) {
                        //ValidateUser();
                        //sendOtpOnPhone();
                        signInWithEmailAndPassword();
                    }
                } else {
                    Log.d(TAG, "No network found");
                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signInWithEmailAndPassword() {
        String phoneNumber = countryCode.getFullNumberWithPlus();
        String email = phoneNumber + "@skyspirit.com";
        String password = etPassword.getText().toString();

        final ProgressDialog progressDialog1 = new ProgressDialog(getActivity());
        progressDialog1.setCancelable(false);
        progressDialog1.setMessage("Logging in ...");
        progressDialog1.show();
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                userProfileRef.whereEqualTo("email", mAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: user");
                            SaveDefaultValueForAll.saveDefaults(getActivity(), "Others", new StartMyActivity() {
                                @Override
                                public void startThisActivity(String userType) {
                                    Intent intent = new Intent(getActivity(), UserNavigationDashboard.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    progressDialog1.dismiss();
                                }
                            });

                        }else {
                            adminProfileRef.whereEqualTo("email", mAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        Log.d(TAG, "onSuccess: admin");
                                        SaveDefaultValueForAll.saveDefaults(getActivity(), "Admin", new StartMyActivity() {
                                            @Override
                                            public void startThisActivity(String userType) {
                                                Intent intent = new Intent(getActivity(), AdminNavigationDashboard.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                progressDialog1.dismiss();
                                            }
                                        });

                                    }else {
                                        progressDialog1.dismiss();
                                        Toast.makeText(getActivity(), "no user found", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onSuccess: admin");
                                    progressDialog1.dismiss();
                                    Toast.makeText(getActivity(), "no user found", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();

                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: user on failure");
                        progressDialog1.dismiss();
                        Toast.makeText(getActivity(), "no user found", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog1.dismiss();
                Toast.makeText(getActivity(), "User not found or wrong password", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
