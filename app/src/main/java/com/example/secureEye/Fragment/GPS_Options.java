package com.example.secureEye.Fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.secureEye.Activity.Add_New_Geofence;
import com.example.secureEye.Activity.LocationHistoryPolyline;
import com.example.secureEye.Activity.MapsTracking;
import com.example.secureEye.R;
import com.example.secureEye.Utils.TypefaceSpan;

/**
 * A simple {@link Fragment} subclass.
 */
public class GPS_Options extends Fragment implements View.OnClickListener {
    private CardView btnShowLiveLocation;
    private CardView btnAddNewGeofence;
    private CardView btnShowUserHistory;

    public GPS_Options(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gps__options, container, false);
        SpannableString str = new SpannableString("Admin");
        str.setSpan(new TypefaceSpan(getActivity(),TypefaceSpan.fontName),0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        btnShowLiveLocation=view.findViewById(R.id.btnLiveLocation);
        btnAddNewGeofence=view.findViewById(R.id.btnNewGeofence);
        btnShowUserHistory=view.findViewById(R.id.btnUserHistory);

        btnShowLiveLocation.setOnClickListener(this);
        btnAddNewGeofence.setOnClickListener(this);
        btnShowUserHistory.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnLiveLocation:
                Intent intent=new Intent(getActivity(), MapsTracking.class);
                startActivity(intent);
                break;

            case R.id.btnNewGeofence:
                Intent intent1=new Intent(getActivity(), Add_New_Geofence.class);
                startActivity(intent1);
                break;

            case R.id.btnUserHistory:
                Intent intent2=new Intent(getActivity(), LocationHistoryPolyline.class);
                startActivity(intent2);

        }
    }
}
