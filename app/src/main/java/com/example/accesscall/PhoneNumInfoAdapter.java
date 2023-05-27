package com.example.accesscall;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PhoneNumInfoAdapter extends RecyclerView.Adapter<PhoneNumInfoAdapter.ViewHolder>{
    MainActivity main;
    public static ArrayList<PhoneNumInfo> items = new ArrayList<PhoneNumInfo>();

    public void addItem(PhoneNumInfo item){
        if(!items.contains(item))
            items.add(item);
    }

    public void setItems(ArrayList<PhoneNumInfo> items){
        this.items = items;
    }

    public PhoneNumInfo getItem(int position){
        return items.get(position);
    }

    public void setItem(int position, PhoneNumInfo item){
        items.set(position, item);
    }

    public void newArray(){
        items.clear();
    }

    @NonNull
    @Override
    public PhoneNumInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.telinfo_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneNumInfoAdapter.ViewHolder holder, int position) {
        PhoneNumInfo item = items.get(position);
        holder.setItem(item);
    }
//
    //
    @Override
    public int getItemCount() {
        return items.size();
    }

    public ArrayList<PhoneNumInfo> getList(){
        return items;
    }

    public boolean phoneNumCheck(String phoneNum){
        boolean flag = false;
        for(int i = 0; i < items.size(); i++){
            PhoneNumInfo info = items.get(i);
            Log.d("check","item:"+info.getName()+"/"+info.getPhoneNumber()+"phoneNum:"+phoneNum);
            if(info.getPhoneNumber().equals(phoneNum)){
                flag = true;
                return flag;
            }
        }
        return flag;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView, textView2;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            textView2 = itemView.findViewById(R.id.textView2);
        }

        public void setItem(PhoneNumInfo item){
            textView.setText(item.getName());
            textView2.setText(item.getPhoneNumber());
        }
    }
}
