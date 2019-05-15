package com.example.secureEye.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.secureEye.Interface.OTP_Verify_Result;
import com.example.secureEye.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OTPVerification {

    private String TAG="OTPVerification";
    private AwesomeValidation validation;
    private FirebaseAuth mAuth;
    private String codeSent;
    private ProgressDialog progressDialog;
    private Activity activity;
    private EditText recieveOtp;
    private OTP_Verify_Result otp_verify_result;

    public OTPVerification(Activity activity){
        FirebaseApp.initializeApp(activity);
        mAuth = FirebaseAuth.getInstance();
        this.activity=activity;
        validation = new AwesomeValidation(ValidationStyle.BASIC);
    }

    public void sendOTP(String phoneNumber, final OTP_Verify_Result otp_verify_result){

        Log.d(TAG, "phone number= " + phoneNumber);
        this.otp_verify_result=otp_verify_result;

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                activity,
                mCallbacks);


        AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
        View mView = activity.getLayoutInflater().inflate(R.layout.otp_verify_dialog, null);
        recieveOtp = (EditText) mView.findViewById(R.id.otpVerify);
        validation.addValidation(activity, R.id.otpVerify, "^[0-9]{10}$", R.string.otpError);
        Button btnOtpVerify = (Button) mView.findViewById(R.id.btnOtpVerify);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        btnOtpVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validation.validate()) {
                    String code = recieveOtp.getText().toString();
                    dialog.dismiss();
                    verifyCode(code);

                }else {
                    dialog.dismiss();
                }

            }
        });
    }

    private void verifyCode(String code) {

        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Verifying Number...");
        progressDialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);

        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            //here you can open new activity
                            otp_verify_result.onSuccess(task);


                        } else {
                            progressDialog.dismiss();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                otp_verify_result.onFailed("failed");
                            }
                        }
                    }
                });
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            String code=phoneAuthCredential.getSmsCode();
            if (code!=null){
                recieveOtp.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(activity, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            codeSent = s;
        }
    };

}
