package com.tti.tlivemobile.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dylan_liang on 2017/6/19.
 */

public class Category implements Parcelable {

    private int categoryId;
    private String categoryName;
    private String categoryImagePath;

    public Category() {
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

    public String getCategoryImagePath() {
        return categoryImagePath;
    }

    public void setCategoryImagePath(String categoryImagePath) {
        this.categoryImagePath = categoryImagePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.categoryId);
        parcel.writeString(this.categoryName);
        parcel.writeString(this.categoryImagePath);
    }

    protected Category(Parcel in) {
        this.categoryId = in.readInt();
        this.categoryName = in.readString();
        this.categoryImagePath = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
