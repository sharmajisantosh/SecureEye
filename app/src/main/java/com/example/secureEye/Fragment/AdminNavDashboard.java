package com.example.secureEye.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.secureEye.Activity.Add_New_Geofence;
import com.example.secureEye.Activity.MapsTracking;
import com.example.secureEye.R;
import com.example.secureEye.Utils.TypefaceSpan;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class AdminNavDashboard extends Fragment implements View.OnClickListener {

    private CardView btnGuardManager;
    private CardView btnGPSManager;
    private CardView btnDutySchedular;
    private CardView btnIncidentReport;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nav_dashboard, container, false);
        SpannableString str = new SpannableString("Admin");
        str.setSpan(new TypefaceSpan(getActivity(),TypefaceSpan.fontName),0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        btnGuardManager=view.findViewById(R.id.btnGuardManager);
        btnGPSManager=view.findViewById(R.id.btnGPSManager);
        btnDutySchedular=view.findViewById(R.id.btnDutySchedular);
        btnIncidentReport=view.findViewById(R.id.btnIncidentReport);

        btnGuardManager.setOnClickListener(this);
        btnGPSManager.setOnClickListener(this);
        btnDutySchedular.setOnClickListener(this);
        btnIncidentReport.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnGuardManager:
                GuardManager guardManager = new GuardManager();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.addToBackStack(null);
                ft.replace(R.id.NavFrameLayout, guardManager);
                ft.commit();
                break;

            case R.id.btnGPSManager:
                GPS_Options gps_options = new GPS_Options();
                FragmentTransaction ft1 = getActivity().getSupportFragmentManager().beginTransaction();
                ft1.addToBackStack(null);
                ft1.replace(R.id.NavFrameLayout, gps_options);
                ft1.commit();
                break;

            case R.id.btnDutySchedular:
                break;

            case R.id.btnIncidentReport:
                Intent intent1=new Intent(getActivity(), Add_New_Geofence.class);
                startActivity(intent1);

                break;

        }
    }
}
