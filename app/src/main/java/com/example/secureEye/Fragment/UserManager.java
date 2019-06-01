package com.example.secureEye.Fragment;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.cardview.widget.CardView;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.secureEye.R;
import com.example.secureEye.Utils.TypefaceSpan;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserManager extends Fragment implements View.OnClickListener {

    private CardView btnNewUser, btnModifyUser, btnDeleteUser;


    public UserManager() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_manager, container, false);
        SpannableString str = new SpannableString("User Manager");
        str.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        btnNewUser = view.findViewById(R.id.btnNewUser);
        btnModifyUser = view.findViewById(R.id.btnModifyUser);
        btnDeleteUser = view.findViewById(R.id.btnDeleteUser);

        btnNewUser.setOnClickListener(this);
        btnModifyUser.setOnClickListener(this);
        btnDeleteUser.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnNewUser:
                AddNewUser addNewUser = new AddNewUser();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.NavFrameLayout, addNewUser);
                ft.commit();
                break;

            case R.id.btnModifyUser:
                UpdateUser updateUser = new UpdateUser();
                FragmentTransaction ft1 = getActivity().getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.NavFrameLayout, updateUser);
                ft1.commit();
                break;

            case R.id.btnDeleteUser:
                DeleteUser deleteUser = new DeleteUser();
                FragmentTransaction ft2 = getActivity().getSupportFragmentManager().beginTransaction();
                ft2.replace(R.id.NavFrameLayout, deleteUser);
                ft2.commit();
                break;
        }

    }


}
