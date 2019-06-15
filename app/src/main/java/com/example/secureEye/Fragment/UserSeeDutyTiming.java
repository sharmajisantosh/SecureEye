package com.example.secureEye.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.health.TimerStat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserSeeDutyTiming extends Fragment {

    private static final String TAG = "UserSeeDutyTiming";
    private FirebaseAuth mAuth;
    private CollectionReference userProfileRef;
    private TextView tvDutyStartTime, tvDutyEndTime, tvDutyUpdateTime;
    private SimpleDateFormat easyDateFormat;
    private SimpleDateFormat simpleDateFormat;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_see_duty_timing, container, false);
        mAuth = FirebaseAuth.getInstance();

        tvDutyStartTime = view.findViewById(R.id.seeDutyStartTime);
        tvDutyEndTime = view.findViewById(R.id.seeDutyEndTime);
        tvDutyUpdateTime = view.findViewById(R.id.dutyUpdateTime);

        userProfileRef = Constant_URLS.USER_PROFILE_REF;
        easyDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z", Locale.ENGLISH);

        userProfileRef.document(mAuth.getUid()).collection("DutyTimes").orderBy("timeStamp",
                Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> dutyTimeList = task.getResult().getDocuments();
                    if (dutyTimeList.size() > 0) {
                        Timestamp starttime = (Timestamp) dutyTimeList.get(0).get("startTime");
                        Timestamp endTime = (Timestamp) dutyTimeList.get(0).get("endTime");
                        String updateTime = dutyTimeList.get(0).get("timeStamp").toString();
                        Date startDate = starttime.toDate();
                        Date endDate = endTime.toDate();
                        Date updatedTime = getDateFromString(updateTime);

                        tvDutyStartTime.setText(easyDateFormat.format(startDate));
                        tvDutyEndTime.setText(easyDateFormat.format(endDate));
                        tvDutyUpdateTime.setText(easyDateFormat.format(updatedTime));
                        Log.d(TAG, "onComplete: " + updateTime);
                        Log.d(TAG, "id= " + dutyTimeList.get(0).getId());
                    } else {
                        Toast.makeText(getActivity(), "No duty record found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    private Date getDateFromString(String updateTime) {

        try {
            Date date = simpleDateFormat.parse(updateTime);

            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;

    }
}
