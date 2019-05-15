package com.example.secureEye.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.secureEye.Model.User;
import com.example.secureEye.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OfflineUserAdapter extends RecyclerView.Adapter<OfflineUserAdapter.MyViewHolder> {
    private List<User> userList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.txtOfflineName);
        }
    }
    public OfflineUserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        User user=userList.get(position);
        holder.name.setText(user.getDispName()+" is offline");
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
