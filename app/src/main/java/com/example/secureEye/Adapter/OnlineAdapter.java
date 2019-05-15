package com.example.secureEye.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.secureEye.Interface.CheckboxCheckedListner;
import com.example.secureEye.Model.User;
import com.example.secureEye.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OnlineAdapter extends ArrayAdapter<User> {

    public List<User> onlineUserList;
    private Context context;
    private CheckboxCheckedListner checkedListener;

    public OnlineAdapter(@NonNull Context context, int resource, List<User> userList) {
        super(context, resource, userList);
        this.onlineUserList=new ArrayList<User>();
        this.onlineUserList=userList;
        this.context=context;
    }

    private class ViewHolder{
        TextView userName;
        CheckBox checkBox;

        ViewHolder(View v){
            userName=v.findViewById(R.id.tvSpinner);
            checkBox=v.findViewById(R.id.checkbox1);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View row=convertView;

        ViewHolder holder=null;
        if (row==null){
            LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row=inflater.inflate(R.layout.online_list_item,parent,false);
            holder=new ViewHolder(row);
            row.setTag(holder);

        }else {
            holder= (ViewHolder) row.getTag();
        }

        holder.userName.setText(onlineUserList.get(position).getDispName());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkedListener!=null){

                    if (onlineUserList.get(position).getDispName().equals("All")&&isChecked){
                        checkedListener.selectAll(position,isChecked);
                    }else if (onlineUserList.get(position).getDispName().equals("All")&&!isChecked){
                        checkedListener.selectAll(position,isChecked);
                    }
                    checkedListener.getCheckboxCheckedListner(position, isChecked);
                }
            }
        });

        return row;
    }

    public void setCheckedListener(CheckboxCheckedListner checkedListener){
        this.checkedListener=checkedListener;
    }
}
