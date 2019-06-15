package com.example.secureEye.Fragment;


import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.example.secureEye.Adapter.AdminUserListDutyAdapter;
import com.example.secureEye.Interface.RecyclerItemClickListner;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.SharedPrefManager;
import com.example.secureEye.Utils.TypefaceSpan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminUserDutyList extends Fragment {

    private static final String TAG = "AdminUserDutyList";
    private FirebaseAuth mAuth;
    private RecyclerView userDutyRecycler;
    private CollectionReference userAdminLinkRef, userProfileRef;
    private List<UserProfile> userList;
    private List<String> userIdList;
    private AdminUserListDutyAdapter dutyAdapter;
    private ProgressDialog progressDialog1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_admin_user_duty_list, container, false);
        mAuth=FirebaseAuth.getInstance();

        SpannableString str = new SpannableString("Users List");
        str.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        userDutyRecycler=view.findViewById(R.id.recyclerAdminAllUserList);
        userDutyRecycler.setHasFixedSize(false);

        userAdminLinkRef= Constant_URLS.USER_ADMIN_LINK_REF;
        userProfileRef=Constant_URLS.USER_PROFILE_REF;

        userList=new ArrayList<>();
        userIdList=new ArrayList<>();
        progressDialog1 = new ProgressDialog(getActivity());
        progressDialog1.setCancelable(false);
        progressDialog1.setMessage("Please wait .... ");
        progressDialog1.show();


        getUserList();

        return view;
    }

    private void getUserList() {
        String adminGeoZone=SharedPrefManager.getInstance(getActivity()).getUserGeoZone();
        Log.d(TAG, "adminGeoZone= "+adminGeoZone);
        userAdminLinkRef.document(adminGeoZone).collection(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    userIdList.clear();
                    List<DocumentSnapshot> userList = task.getResult().getDocuments();
                    if (userList.size() > 0) {
                        for (int i = 0; i < userList.size(); i++) {
                            userIdList.add(userList.get(i).getId());
                            Log.d(TAG, "userid list "+userList.get(i).getId());
                        }
                        setUserProfileList();

                    } else {
                        progressDialog1.dismiss();
                        Toast.makeText(getActivity(), "You have no users", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void setUserProfileList() {
        if (userIdList.size()>0){
            for (int i=0;i<userIdList.size();i++){
                userProfileRef.document(userIdList.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isComplete()) {
                            DocumentSnapshot user = task.getResult();
                            userList.add(user.toObject(UserProfile.class));
                            dutyAdapter.notifyDataSetChanged();
                            Log.d(TAG, "userName list "+user.toObject(UserProfile.class).getName());
                            progressDialog1.dismiss();

                        }
                        progressDialog1.dismiss();
                    }
                });
            }

            dutyAdapter=new AdminUserListDutyAdapter(userList, new RecyclerItemClickListner() {
                @Override
                public void onItemClickListner(int position) {
                    UserProfile userProfile=userList.get(position);

                    Bundle bundle=new Bundle();
                    bundle.putSerializable("userDuty", userProfile);

                    AdminUserDutySchedular dutySchedular = new AdminUserDutySchedular();
                    dutySchedular.setArguments(bundle);
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.addToBackStack(TAG);
                    ft.replace(R.id.NavFrameLayout, dutySchedular);
                    ft.commit();
                }
            });

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            userDutyRecycler.setLayoutManager(mLayoutManager);
            userDutyRecycler.setItemAnimator(new DefaultItemAnimator());
            userDutyRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), 0));
            final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_right_to_left);
            userDutyRecycler.setLayoutAnimation(controller);
            userDutyRecycler.setAdapter(dutyAdapter);
            dutyAdapter.notifyDataSetChanged();


        }
    }
}
