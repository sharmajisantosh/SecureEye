package com.example.secureEye.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.secureEye.Model.UserProfile;

import java.util.List;

public class GeofenceAllUserAdapter extends ArrayAdapter<UserProfile> {
    private Context context;
    private List<UserProfile> userProfileList;

    public GeofenceAllUserAdapter(@NonNull Context context, int resource, @NonNull List<UserProfile> userProfileList) {
        super(context, resource, userProfileList);
        this.context=context;
        this.userProfileList=userProfileList;

    }

    @Override
    public int getCount(){
        return userProfileList.size();
    }

    @Override
    public UserProfile getItem(int position){
        return userProfileList.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setText(userProfileList.get(position).getName());

        return label;
    }
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setText(userProfileList.get(position).getName());

        return label;
    }

}
