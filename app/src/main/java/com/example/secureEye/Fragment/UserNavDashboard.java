package com.example.secureEye.Fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.IBinder;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.secureEye.Activity.Add_New_Geofence;
import com.example.secureEye.Activity.MapsTracking;
import com.example.secureEye.R;
import com.example.secureEye.Services.AppController;
import com.example.secureEye.Services.LocationUpdatesService;
import com.example.secureEye.Utils.TypefaceSpan;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserNavDashboard extends Fragment implements View.OnClickListener {

    private static final String TAG = "UserNavDashboard";
    private CardView btnUserMessaging;
    private CardView btnUserTasks;
    private CardView btnUserDutySchedular;
    private CardView btnUserIncidentReport;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_user_nav_dashboard, container, false);

        SpannableString str = new SpannableString("User");
        str.setSpan(new TypefaceSpan(getActivity(),TypefaceSpan.fontName),0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);
        // Inflate the layout for this fragment

        btnUserMessaging=view.findViewById(R.id.btnUserMessaging);
        btnUserTasks=view.findViewById(R.id.btnUserTasks);
        btnUserDutySchedular=view.findViewById(R.id.btnUserDutySchedular);
        btnUserIncidentReport=view.findViewById(R.id.btnUserIncidentReport);

        btnUserMessaging.setOnClickListener(this);
        btnUserTasks.setOnClickListener(this);
        btnUserDutySchedular.setOnClickListener(this);
        btnUserIncidentReport.setOnClickListener(this);

        LocationUpdatesService mService = AppController.getInstance().mService;
        if (mService != null)
            mService.requestLocationUpdates();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btnUserMessaging:
                UserMessagingFrag navFrag1 = new UserMessagingFrag();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.addToBackStack("UserMessagingFrag");
                ft.replace(R.id.NavFrameLayout1, navFrag1);
                ft.commit();
                break;

            case R.id.btnUserTasks:
                break;

            case R.id.btnUserDutySchedular:
                break;

            case R.id.btnUserIncidentReport:
                break;
        }
    }

    @Override
    public void onStart() {
        LocationUpdatesService mService = AppController.getInstance().mService;
        if (mService != null)
            mService.requestLocationUpdates();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
