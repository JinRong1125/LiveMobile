package com.tti.tlivemobile.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dylan_liang on 2017/6/19.
 */

public class Platform implements Parcelable {

    private int accountId;
    private String platformState;
    private String userName;
    private String avatarPath;
    private String platformTitle;
    private int categoryId;
    private String categoryName;
    private String streamUrl;
    private String platformImagePath;
    private int viewersValue;

    public Platform() {
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getPlatformState() {
        return platformState;
    }

    public void setPlatformState(String platformState) {
        this.platformState = platformState;
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

    public String getPlatformTitle() {
        return platformTitle;
    }

    public void setPlatformTitle(String platformTitle) {
        this.platformTitle = platformTitle;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getPlatformImagePath() {
        return platformImagePath;
    }

    public void setPlatformImagePath(String platformImagePath) {
        this.platformImagePath = platformImagePath;
    }

    public int getViewersValue() {
        return viewersValue;
    }

    public void setViewersValue(int viewersValue) {
        this.viewersValue = viewersValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.accountId);
        parcel.writeString(this.platformState);
        parcel.writeString(this.userName);
        parcel.writeString(this.avatarPath);
        parcel.writeString(this.platformTitle);
        parcel.writeInt(this.categoryId);
        parcel.writeString(this.categoryName);
        parcel.writeString(this.streamUrl);
        parcel.writeString(this.platformImagePath);
        parcel.writeInt(this.viewersValue);
    }

    protected Platform(Parcel in) {
        this.accountId = in.readInt();
        this.platformState = in.readString();
        this.userName = in.readString();
        this.avatarPath = in.readString();
        this.platformTitle = in.readString();
        this.categoryId = in.readInt();
        this.categoryName = in.readString();
        this.streamUrl = in.readString();
        this.platformImagePath = in.readString();
        this.viewersValue = in.readInt();
    }

    public static final Parcelable.Creator<Platform> CREATOR = new Parcelable.Creator<Platform>() {
        public Platform createFromParcel(Parcel source) {
            return new Platform(source);
        }

        public Platform[] newArray(int size) {
            return new Platform[size];
        }
    };
}
