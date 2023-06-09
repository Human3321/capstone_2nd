package com.example.accesscall;

import androidx.annotation.Nullable;

public class PhoneNumInfo {
    String name;
    String phoneNumber;

    public PhoneNumInfo(String name, String phoneNumber){
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        PhoneNumInfo object = (PhoneNumInfo)obj;
        
        if(this.name.equals(object.name)&&this.phoneNumber.equals(object.phoneNumber)){
            return true;
        }else{
            return false;
        }
    }
}
