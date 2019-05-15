package com.example.secureEye.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.secureEye.Interface.UpdatedVerifiedPassword;
import com.example.secureEye.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UpdatePassword {
    private String TAG="UpdateMobile";
    private Activity activity;
    private AwesomeValidation validation;
    private EditText etOldPass, etNewPass;
    private String currentUserName, currentFullMobile;

    public UpdatePassword(String currentUserName, String currentFullMobile, Activity activity){
        this.activity=activity;
        validation=new AwesomeValidation(ValidationStyle.BASIC);
        this.currentUserName=currentUserName;
        this.currentFullMobile=currentFullMobile;
    }

    public void updatePassword(final UpdatedVerifiedPassword updatedVerifiedPassword){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
        View mView = activity.getLayoutInflater().inflate(R.layout.update_password_dialog, null);
        etOldPass = mView.findViewById(R.id.updateOldPass);
        etNewPass =  mView.findViewById(R.id.updateNewPass);

        validation.addValidation(activity, R.id.newPass, "((?=.*\\d)(?=.*[a-z])(?=.*[@#$%]).{6,20})", R.string.passwordError);
        validation.addValidation(activity, R.id.reNewPass, "((?=.*\\d)(?=.*[a-z])(?=.*[@#$%]).{6,20})", R.string.passwordError);

        Button btnPassVerify = (Button) mView.findViewById(R.id.btnUpdatePass);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        btnPassVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validation.validate()){
                    dialog.dismiss();
                }else {
                    Toast.makeText(activity, "Some error occured.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

            }
        });

    }

}
