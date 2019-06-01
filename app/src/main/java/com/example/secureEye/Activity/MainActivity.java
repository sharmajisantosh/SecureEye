package com.example.secureEye.Activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.secureEye.Fragment.AdminSignup_Fragment;
import com.example.secureEye.Fragment.Login_Fragment;
import com.example.secureEye.Interface.StartMyActivity;
import com.example.secureEye.Model.User;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Services.AppController;
import com.example.secureEye.Services.LocationUpdatesService;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SaveDefaultValueForAll;
import com.example.secureEye.Utils.SharedPrefManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Button btnLogin, btnAdminSignup;
    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;
    private StartMyActivity startMyActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(MainActivity.this);

        btnLogin = findViewById(R.id.btnLogin);
        btnAdminSignup = findViewById(R.id.btnSignUp);
        fragmentManager = getSupportFragmentManager();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        String loginType = SharedPrefManager.getInstance(MainActivity.this).getUserRole();
        String email = SharedPrefManager.getInstance(MainActivity.this).getUserEmail();
        String pass = SharedPrefManager.getInstance(MainActivity.this).getUserPassword();
        if (user != null&&loginType!=null) {

            final ProgressDialog progressDialog1 = new ProgressDialog(MainActivity.this);
            progressDialog1.setCancelable(false);
            progressDialog1.setMessage("Please wait...");
            progressDialog1.show();

            SaveDefaultValueForAll.saveDefaults(MainActivity.this, loginType,email,pass, new StartMyActivity() {
                @Override
                public void startThisActivity(String userType) {
                    if (userType.equalsIgnoreCase("Admin")){
                        AppController.getInstance().isAppInBackground(false);
                        Intent intent = new Intent(MainActivity.this, AdminNavigationDashboard.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        progressDialog1.dismiss();
                    }else if (userType.equalsIgnoreCase("Others")){
                        AppController.getInstance().isAppInBackground(false);
                        Intent intent = new Intent(MainActivity.this, UserNavigationDashboard.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        progressDialog1.dismiss();
                    }else {
                        Log.d(TAG, "startThisActivity: user"+ user.getDisplayName());
                        Log.d(TAG, "startThisActivity: logintype "+loginType);
                        progressDialog1.dismiss();
                        fragmentManager.beginTransaction().replace(R.id.frameContainer, new Login_Fragment()).commit();
                    }
                }
            });
        }

        btnLogin.setBackgroundTintList(getResources().getColorStateList(R.color.red));
        btnAdminSignup.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
        Log.d(TAG, "login called ");
        fragmentManager.beginTransaction().replace(R.id.frameContainer, new Login_Fragment()).commit();

        btnLogin.setOnClickListener(this);
        btnAdminSignup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnLogin:
                btnLogin.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                btnAdminSignup.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                btnLogin.setClickable(false);
                btnAdminSignup.setClickable(true);
                fragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.frameContainer, new Login_Fragment()).commit();
                break;

            case R.id.btnSignUp:
                btnLogin.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                btnAdminSignup.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                btnLogin.setClickable(true);
                btnAdminSignup.setClickable(false);
                fragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.frameContainer, new AdminSignup_Fragment()).commit();
                break;
        }

    }

    @Override
    public void onBackPressed() {

        Fragment currentFrag = fragmentManager.findFragmentById(R.id.frameContainer);
        if (currentFrag instanceof Login_Fragment) {
            finish();
        } else {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.frameContainer, new Login_Fragment()).commit();
        }
    }

    @Override
    protected void onStop() {
       /* LocationUpdatesService mService = AppController.getInstance().mService;
        if (mService != null)
            mService.removeLocationUpdates();*/
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationUpdatesService mService = AppController.getInstance().mService;
        if (mService != null)
            mService.removeLocationUpdates();
    }
}
