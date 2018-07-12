package com.tti.tlivemobile.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dylan_liang on 2017/6/19.
 */

public class LoginInformation implements Parcelable {

    private String userId;
    private String password;

    public LoginInformation() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.userId);
        parcel.writeString(this.password);
    }

    protected LoginInformation(Parcel in) {
        this.userId = in.readString();
        this.password = in.readString();
    }

    public static final Creator<LoginInformation> CREATOR = new Creator<LoginInformation>() {
        public LoginInformation createFromParcel(Parcel source) {
            return new LoginInformation(source);
        }

        public LoginInformation[] newArray(int size) {
            return new LoginInformation[size];
        }
    };
}
