package com.example.secureEye.Fragment;


import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.secureEye.Interface.OTP_Verify_Result;
import com.example.secureEye.R;
import com.example.secureEye.Utils.OTPVerification;
import com.example.secureEye.Utils.SessionManager;
import com.example.secureEye.Model.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForgotPassword_Fragment extends Fragment {

    private String TAG = "Forgot_Password";
    private CountryCodePicker countryCode;
    private EditText etUserName, etMobile;
    private Button btnGetOtp;
    private AwesomeValidation validation;
    private String phoneNumber;
    private String userName;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;


    public ForgotPassword_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password_, container, false);
        countryCode = view.findViewById(R.id.forgotCountryCode);
        mAuth = FirebaseAuth.getInstance();

        etUserName = view.findViewById(R.id.forgotUserName);
        etMobile = view.findViewById(R.id.forgotMobile);
        btnGetOtp = view.findViewById(R.id.btnFragGetOtp);

        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.roboto_medium);
        countryCode.setTypeFace(typeface);
        countryCode.registerCarrierNumberEditText(etMobile);
        etUserName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        validation = new AwesomeValidation(ValidationStyle.BASIC);
        validation.addValidation(getActivity(), R.id.forgotUserName, "[a-zA-Z\\s]+", R.string.nameError);
        validation.addValidation(getActivity(), R.id.forgotMobile, "\\d{5}[\\s]\\d{5}", R.string.mobileError);

        btnGetOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SessionManager.isNetworkAvaliable(getActivity())) {
                    if (validation.validate()) {
                        phoneNumber = countryCode.getFullNumberWithPlus();
                        userName = etUserName.getText().toString();
                        //validateUser();
                        sendOtpOnPhone();
                    }
                } else {

                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void validateUser() {


    }

    private void sendOtpOnPhone() {
        OTPVerification otpVerification = new OTPVerification(getActivity());
        otpVerification.sendOTP(phoneNumber, new OTP_Verify_Result() {

            @Override
            public void onSuccess(@NonNull Task<AuthResult> task) {
                showNewPasswordDialog();
                Log.d(TAG, "inside onSuccess");
            }

            @Override
            public void onFailed(String result) {
                Toast.makeText(getActivity(), "OTP Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNewPasswordDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View mView = getLayoutInflater().inflate(R.layout.new_password_dialog, null);
        final EditText newPass = (EditText) mView.findViewById(R.id.newPass);
        final EditText reNewPass = (EditText) mView.findViewById(R.id.reNewPass);
        validation.addValidation(getActivity(), R.id.newPass, "((?=.*\\d)(?=.*[a-z])(?=.*[@#$%]).{6,20})", R.string.passwordError);
        validation.addValidation(getActivity(), R.id.reNewPass, "((?=.*\\d)(?=.*[a-z])(?=.*[@#$%]).{6,20})", R.string.passwordError);
        Button btnSetPass = (Button) mView.findViewById(R.id.btnSetPass);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        btnSetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newPass.getText().toString().equals(reNewPass.getText().toString())) {
                    if (validation.validate()) {
                        dialog.dismiss();
                        //setNewPasswordOnServer(newPass.getText().toString());
                        updatePassword(newPass);

                    }
                } else {
                    Toast.makeText(getActivity(), "Password not matched", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    private void updatePassword(EditText newPass) {
        String newPassword=newPass.getText().toString();
        String email=phoneNumber+"@skyspirit.com";
        databaseReference= FirebaseDatabase.getInstance().getReference("UserProfile");
        Query query=databaseReference.orderByChild("email").equalTo(email);
        Log.d("forgotpassword", "inside update password");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot user:dataSnapshot.getChildren()){
                        String fireUserId=user.getKey();
                        UserProfile userProfile=user.getValue(UserProfile.class);
                        userProfile.setPassword(newPassword);
                        databaseReference.child("userProfile").child(fireUserId).setValue(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getActivity(), "Password updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setNewPasswordOnServer(final String newPassword) {
    }
}
