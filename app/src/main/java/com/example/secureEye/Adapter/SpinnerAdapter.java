package com.example.secureEye.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SpinnerAdapter extends BaseAdapter implements android.widget.SpinnerAdapter {

    List<UserProfile> users;
    Context context;
    public SpinnerAdapter(@NonNull Context context, @NonNull List<UserProfile> userList) {
        this.users=userList;
        this.context=context;
    }

    @Override
    public View getDropDownView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        return initView(position,view,parent);
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        return initView(position, view, parent);
    }

    private View initView(int position, View view, ViewGroup parent) {
        if (view==null){
            view=LayoutInflater.from(context).inflate(R.layout.admin_list_item,parent,false);
        }
        TextView tv=view.findViewById(R.id.tvSpinner);
        tv.setText(users.get(position).getName());
        return view;
    }


}
