package com.example.secureEye.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.secureEye.Interface.OTP_Verify_Result;
import com.example.secureEye.Interface.UpdatedVerifiedNumber;
import com.example.secureEye.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.hbb20.CountryCodePicker;

import androidx.annotation.NonNull;

public class UpdateMobile {

    private String TAG="UpdateMobile";
    private Activity activity;
    private AwesomeValidation validation;
    private CountryCodePicker countryCode;
    private String phoneNumber;
    private EditText mobile;

    public UpdateMobile(Activity activity){
        this.activity=activity;
        validation=new AwesomeValidation(ValidationStyle.BASIC);
    }

    public void showNewMobileDialog(final UpdatedVerifiedNumber updatedVerifiedNumber){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
        View mView = activity.getLayoutInflater().inflate(R.layout.update_mobile_dialog, null);
        mobile = mView.findViewById(R.id.updateMobile);
        countryCode =  mView.findViewById(R.id.updateCountryCode);
        validation.addValidation(activity, R.id.updateMobile, "^[0-9]{10}$", R.string.mobileError);

        countryCode.registerCarrierNumberEditText(mobile);

        Button btnOtpVerify = (Button) mView.findViewById(R.id.btnUpdateGetOtp);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        btnOtpVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validation.validate()){
                    phoneNumber=countryCode.getFullNumberWithPlus();
                    sendOtpOnPhone(phoneNumber, updatedVerifiedNumber);
                    dialog.dismiss();
                }else {
                    Toast.makeText(activity, "Some error occured.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

            }
        });

    }

    private void sendOtpOnPhone(final String phoneNumber, final UpdatedVerifiedNumber updatedVerifiedNumber) {

        OTPVerification otpVerification=new OTPVerification(activity);
        otpVerification.sendOTP(phoneNumber, new OTP_Verify_Result() {


            @Override
            public void onSuccess(@NonNull Task<AuthResult> task) {
                String simplePhone=mobile.getText().toString();
                simplePhone=simplePhone.replaceAll("\\s+", "");
                updatedVerifiedNumber.onVerified(phoneNumber, countryCode.getSelectedCountryCode(), simplePhone);
                Log.d(TAG,"inside onSuccess");
            }

            @Override
            public void onFailed(String result) {
                updatedVerifiedNumber.onFailed("Failed");
            }
        });

    }
}
