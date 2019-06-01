package com.example.secureEye.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.secureEye.R;

import java.util.List;

public class ChoosenImageAdapter extends RecyclerView.Adapter<ChoosenImageAdapter.MyViewHolder> {

    public List<String> fileNameList;

    public ChoosenImageAdapter(List<String> fileNameList) {
        this.fileNameList = fileNameList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_image_list_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String fileName = fileNameList.get(position);
        holder.fileNameView.setText(fileName);
    }

    @Override
    public int getItemCount() {
        return fileNameList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView fileNameView;
        public MyViewHolder(View itemView) {
            super(itemView);
            fileNameView = (TextView) itemView.findViewById(R.id.tvChoosenImageName);
        }
    }
}
