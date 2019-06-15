package com.example.secureEye.Fragment;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.secureEye.R;
import com.example.secureEye.Utils.TypefaceSpan;
import com.google.firebase.auth.FirebaseAuth;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class AdminNavDashboard extends Fragment implements View.OnClickListener {

    private static final String TAG = "AdminNavDashboard";
    private CardView btnGuardManager;
    private CardView btnGPSManager;
    private CardView btnDutySchedular;
    private CardView btnIncidentReport;
    private CardView btnMessaging;
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
        btnMessaging=view.findViewById(R.id.btnMessaging);

        btnGuardManager.setOnClickListener(this);
        btnGPSManager.setOnClickListener(this);
        btnDutySchedular.setOnClickListener(this);
        btnIncidentReport.setOnClickListener(this);
        btnMessaging.setOnClickListener(this);

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
                AdminUserDutyList adminUserDutyList = new AdminUserDutyList();
                FragmentTransaction ft3 = getActivity().getSupportFragmentManager().beginTransaction();
                ft3.addToBackStack("adminUserDutyList");
                ft3.replace(R.id.NavFrameLayout, adminUserDutyList);
                ft3.commit();

            case R.id.btnIncidentReport:
                break;

            case R.id.btnMessaging:
                AdminViewMessagesList adminViewMessage = new AdminViewMessagesList();
                FragmentTransaction ft2 = getActivity().getSupportFragmentManager().beginTransaction();
                ft2.addToBackStack("userMessaging");
                ft2.replace(R.id.NavFrameLayout, adminViewMessage);
                ft2.commit();
                break;

        }
    }
}
