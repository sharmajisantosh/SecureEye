package com.example.secureEye.Fragment;


import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secureEye.Model.UserMessage;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Services.LocationHelper;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.TypefaceSpan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminUserDutySchedular extends Fragment implements View.OnClickListener {

    private static final String TAG = "AdminUserDutySchedular";
    private FirebaseAuth mAuth;
    private UserProfile selectedUser;
    private TextView tvUserName, tvUserEmail, tvDutyUserGeoZone, tvDutyStartTime, tvDutyEndTime;
    private Button btnAllotDuty;
    private CircleImageView userProfilePic;
    private String userId;
    private Date selectedStartDate, selectedEndDate;
    private SimpleDateFormat simpleDateFormat;
    private CollectionReference userProfileRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_admin_user_duty_schedular, container, false);
        mAuth= FirebaseAuth.getInstance();
        SpannableString str = new SpannableString("Users List");
        str.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        userProfilePic= view.findViewById(R.id.dutyUserProfilePic);
        tvUserName=view.findViewById(R.id.dutyUserName);
        tvUserEmail=view.findViewById(R.id.dutyUserEmail);
        tvDutyUserGeoZone=view.findViewById(R.id.dutyUserGeoZone);
        tvDutyStartTime=view.findViewById(R.id.dutyStartTime);
        tvDutyEndTime=view.findViewById(R.id.dutyEndTime);
        btnAllotDuty=view.findViewById(R.id.btnAllotDuty);

        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        selectedUser = (UserProfile) getArguments().getSerializable("userDuty");
        Picasso.get().load(selectedUser.getProfilePic()).into(userProfilePic);
        tvUserName.setText(selectedUser.getName());
        tvUserEmail.setText(selectedUser.getEmail());
        tvDutyUserGeoZone.setText(selectedUser.getGeoZone());
        userId=selectedUser.getUid();
        tvDutyEndTime.setEnabled(false);

        userProfileRef= Constant_URLS.USER_PROFILE_REF;


        tvDutyStartTime.setOnClickListener(this);
        tvDutyEndTime.setOnClickListener(this);
        btnAllotDuty.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dutyStartTime:
                setStartTime();
                break;

            case R.id.dutyEndTime:
                setEndTime();
                break;

            case R.id.btnAllotDuty:
                uploadDutyTime();
                break;
        }

    }

    private void setStartTime() {
        final Date now = new Date();
        final Calendar calendarMin = Calendar.getInstance();
        final Calendar calendarMax = Calendar.getInstance();
        calendarMin.setTime(now); // Set min now
        calendarMax.setTime(now); // Set max now
        calendarMax.add(Calendar.DATE, 2);
        final Date minDate = calendarMin.getTime();
        final Date maxDate = calendarMax.getTime();

        SwitchDateTimeDialogFragment dateTimeDialogFragment=SwitchDateTimeDialogFragment.newInstance(
                "Duty Start date","Ok", "cancel");
        dateTimeDialogFragment.setDefaultDateTime(minDate);
        dateTimeDialogFragment.setMinimumDateTime(minDate);
        dateTimeDialogFragment.setMaximumDateTime(maxDate);
        dateTimeDialogFragment.startAtCalendarView();

        dateTimeDialogFragment.show(getActivity().getSupportFragmentManager(),"new Date time");
        dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                selectedStartDate = date;
                tvDutyStartTime.setText(simpleDateFormat.format(date));
                tvDutyEndTime.setEnabled(true);
                Calendar calDefaultEndTime = Calendar.getInstance();
                calDefaultEndTime.setTime(selectedStartDate);
                calDefaultEndTime.add(Calendar.HOUR,10);
                Date endDate=calDefaultEndTime.getTime();
                tvDutyEndTime.setText(simpleDateFormat.format(endDate));
                selectedEndDate=endDate;

            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });
    }

    private void setEndTime() {
        final Calendar calendarMax = Calendar.getInstance();
        calendarMax.setTime(new Date());
        calendarMax.add(Calendar.DATE, 2);
        final Date minDate = selectedStartDate;
        final Date maxDate = calendarMax.getTime();

        SwitchDateTimeDialogFragment dateTimeDialogFragment=SwitchDateTimeDialogFragment.newInstance(
                "Duty End date","Ok", "cancel");
        dateTimeDialogFragment.setDefaultDateTime(minDate);
        dateTimeDialogFragment.setMinimumDateTime(minDate);
        dateTimeDialogFragment.setMaximumDateTime(maxDate);
        dateTimeDialogFragment.startAtCalendarView();

        dateTimeDialogFragment.show(getActivity().getSupportFragmentManager(),"new Date time");
        dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                selectedEndDate = date;
                tvDutyEndTime.setText(simpleDateFormat.format(date));
            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });

    }

    private void uploadDutyTime() {
        if (selectedStartDate!=null&&selectedEndDate!=null){

            ProgressDialog progressDialog1 = new ProgressDialog(getActivity());
            progressDialog1.setCancelable(false);
            progressDialog1.setMessage("Scheduling Duty .... ");
            progressDialog1.show();

            Date date=new Date();
            HashMap<String, Object> newDutyTime = new HashMap<>();
            newDutyTime.put("uid", selectedUser.getUid());
            newDutyTime.put("startTime", selectedStartDate);
            newDutyTime.put("endTime", selectedEndDate);
            newDutyTime.put("adminName", mAuth.getCurrentUser().getDisplayName());
            newDutyTime.put("adminMail", mAuth.getCurrentUser().getEmail());
            newDutyTime.put("userDeviceId", selectedUser.getDeviceToken());
            newDutyTime.put("timeStamp", LocationHelper.getGMTTime());

            userProfileRef.document(selectedUser.getUid()).collection("DutyTimes").document(""+System.currentTimeMillis())
                    .set(newDutyTime).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){

                        Toast.makeText(getActivity(), "Duty Schedulled", Toast.LENGTH_SHORT).show();

                        AdminNavDashboard navFrag1 = new AdminNavDashboard();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.NavFrameLayout, navFrag1);
                        ft.commit();
                        progressDialog1.dismiss();
                    }
                    progressDialog1.dismiss();
                }
            });
        }
    }
}
