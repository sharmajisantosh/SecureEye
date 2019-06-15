package com.example.secureEye.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secureEye.Interface.RecyclerItemClickListner;
import com.example.secureEye.Model.User;
import com.example.secureEye.Model.UserMessage;
import com.example.secureEye.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.List;

public class UsersMessageAdapter extends FirestoreRecyclerAdapter<UserMessage,UsersMessageAdapter.MyViewHolder> {
    private static final String TAG = "UsersMessageAdapter";
    private RecyclerItemClickListner itemClickListner;

    public UsersMessageAdapter(@NonNull FirestoreRecyclerOptions<UserMessage> options, RecyclerItemClickListner itemClickListner) {
        super(options);
        this.itemClickListner=itemClickListner;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, message;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.tvMessageUserName);
            message = (TextView) view.findViewById(R.id.tvMessageUserMessage);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int pos, @NonNull UserMessage userMessage) {

        Log.d(TAG, "onBindViewHolder: isRead "+userMessage.isRead()+"   pos "+pos);

        if(userMessage.isRead()){
            holder.name.setText(userMessage.getFromName());
            holder.name.setTypeface(null, Typeface.NORMAL);
            String messagePart = getSmallMessage(userMessage.getMessage());
            holder.message.setText(messagePart);
            holder.message.setTypeface(null, Typeface.NORMAL);
        }else {
            holder.name.setText(userMessage.getFromName());
            holder.name.setTypeface(null, Typeface.BOLD);
            String messagePart = getSmallMessage(userMessage.getMessage());
            holder.message.setText(messagePart);
            holder.message.setTypeface(null, Typeface.BOLD);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListner.onItemClickListner(pos);
            }
        });

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_message_list_item, parent, false);
        return new MyViewHolder(itemView);
    }


    private String getSmallMessage(String message) {
        String messageHint = "";
        if (message.length() > 40) {
            messageHint = message.substring(0, 40) + " ...";
        } else {
            messageHint = message;
        }

        return messageHint;
    }

}
