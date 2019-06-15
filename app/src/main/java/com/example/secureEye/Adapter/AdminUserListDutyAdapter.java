package com.example.secureEye.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secureEye.Interface.RecyclerItemClickListner;
import com.example.secureEye.Model.User;
import com.example.secureEye.Model.UserProfile;
import com.example.secureEye.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminUserListDutyAdapter extends RecyclerView.Adapter<AdminUserListDutyAdapter.MyViewHolder> {
    private List<UserProfile> userList;
    private RecyclerItemClickListner itemClickListner;


    public AdminUserListDutyAdapter(List<UserProfile> userList, RecyclerItemClickListner itemClickListner) {
        this.userList = userList;
        this.itemClickListner=itemClickListner;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, email;
        public CircleImageView userProfilePic;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tvDutyUserName);
            email = view.findViewById(R.id.tvDutyUserEmail);
            userProfilePic = view.findViewById(R.id.userProfilePicDuty);
        }
    }

    @NonNull
    @Override
    public AdminUserListDutyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_all_user_list_item, parent, false);
        return new AdminUserListDutyAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminUserListDutyAdapter.MyViewHolder holder, int position) {

        UserProfile user=userList.get(position);
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());
        Picasso.get().load(user.getProfilePic()).into(holder.userProfilePic);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListner.onItemClickListner(position);

            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}

