package com.tti.tlivemobile.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dylan_liang on 2017/6/19.
 */

public class Account implements Parcelable {

    private int accountId;
    private String userId;
    private String email;
    private String userName;
    private String avatarPath;
    private int subscribingValue;
    private int subscribersValue;
    private int channnelsValue;

    public Account() {
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public int getSubscribingValue() {
        return subscribingValue;
    }

    public void setSubscribingValue(int subscribingValue) {
        this.subscribingValue = subscribingValue;
    }

    public int getSubscribersValue() {
        return subscribersValue;
    }

    public void setSubscribersValue(int subscribersValue) {
        this.subscribersValue = subscribersValue;
    }

    public int getChannnelsValue() {
        return channnelsValue;
    }

    public void setChannnelsValue(int channnelsValue) {
        this.channnelsValue = channnelsValue;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.accountId);
        parcel.writeString(this.userId);
        parcel.writeString(this.email);
        parcel.writeString(this.userName);
        parcel.writeString(this.avatarPath);
        parcel.writeInt(this.subscribingValue);
        parcel.writeInt(this.subscribersValue);
        parcel.writeInt(this.channnelsValue);
    }

    protected Account(Parcel in) {
        this.accountId = in.readInt();
        this.userId = in.readString();
        this.email = in.readString();
        this.userName = in.readString();
        this.avatarPath = in.readString();
        this.subscribingValue = in.readInt();
        this.subscribersValue = in.readInt();
        this.channnelsValue = in.readInt();
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        public Account createFromParcel(Parcel source) {
            return new Account(source);
        }

        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
}
