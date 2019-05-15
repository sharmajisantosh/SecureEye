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
public class GuardManager extends Fragment implements View.OnClickListener {

    private CardView btnNewGuard, btnModifyGuard, btnDeleteGuard;


    public GuardManager() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_guard_manager, container, false);
        SpannableString str = new SpannableString("Guard Manager");
        str.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        btnNewGuard = view.findViewById(R.id.btnNewGuard);
        btnModifyGuard = view.findViewById(R.id.btnModifyGuard);
        btnDeleteGuard = view.findViewById(R.id.btnDeleteGuard);

        btnNewGuard.setOnClickListener(this);
        btnModifyGuard.setOnClickListener(this);
        btnDeleteGuard.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnNewGuard:
                AddNewUser addNewUser = new AddNewUser();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.NavFrameLayout, addNewUser);
                ft.commit();
                break;
        }

    }
}
