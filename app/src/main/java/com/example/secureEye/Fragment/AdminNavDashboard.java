package com.example.secureEye.Fragment;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.secureEye.R;
import com.example.secureEye.Utils.TypefaceSpan;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class AdminNavDashboard extends Fragment implements View.OnClickListener {

    private static final String TAG = "AdminNavDashboard";
    private CardView btnGuardManager;
    private CardView btnGPSManager;
    private CardView btnDutySchedular;
    private CardView btnIncidentReport;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nav_dashboard, container, false);
        SpannableString str = new SpannableString("Admin");
        str.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        btnGuardManager = view.findViewById(R.id.btnGuardManager);
        btnGPSManager = view.findViewById(R.id.btnGPSManager);
        btnDutySchedular = view.findViewById(R.id.btnDutySchedular);
        btnIncidentReport = view.findViewById(R.id.btnIncidentReport);

        btnGuardManager.setOnClickListener(this);
        btnGPSManager.setOnClickListener(this);
        btnDutySchedular.setOnClickListener(this);
        btnIncidentReport.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGuardManager:
                UserManager userManager = new UserManager();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.addToBackStack("UserManager");
                ft.replace(R.id.NavFrameLayout, userManager);
                ft.commit();
                break;

            case R.id.btnGPSManager:
                GPS_Options gps_options = new GPS_Options();
                FragmentTransaction ft1 = getActivity().getSupportFragmentManager().beginTransaction();
                ft1.addToBackStack("GPS_Options");
                ft1.replace(R.id.NavFrameLayout, gps_options);
                ft1.commit();
                break;

            case R.id.btnDutySchedular:
                //updateAdminMail();
                break;

            case R.id.btnIncidentReport:
                break;

        }
    }

    private void updateAdminMail() {
        Log.d(TAG, "updateAdminMail: clicked");
        FirebaseUser user1 = mAuth.getCurrentUser();
        user1.updateEmail("+918447262786@skyspirit.com").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Updated mail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
