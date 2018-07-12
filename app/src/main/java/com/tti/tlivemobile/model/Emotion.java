package com.tti.tlivemobile.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dylan_liang on 2017/6/19.
 */

public class Emotion implements Parcelable {

    private int emotionId;
    private String emotionCode;
    private String emotionPath;

    public Emotion() {
    }

    public int getEmotionId() {
        return emotionId;
    }

    public void setEmotionId(int emotionId) {
        this.emotionId = emotionId;
    }

    public String getEmotionCode() {
        return emotionCode;
    }

    public void setEmotionCode(String emotionCode) {
        this.emotionCode = emotionCode;
    }

    public String getEmotionPath() {
        return emotionPath;
    }

    public void setEmotionPath(String emotionPath) {
        this.emotionPath = emotionPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.emotionId);
        parcel.writeString(this.emotionCode);
        parcel.writeString(this.emotionPath);
    }

    protected Emotion(Parcel in) {
        this.emotionId = in.readInt();
        this.emotionCode = in.readString();
        this.emotionPath = in.readString();
    }

    public static final Creator<Emotion> CREATOR = new Creator<Emotion>() {
        public Emotion createFromParcel(Parcel source) {
            return new Emotion(source);
        }

        public Emotion[] newArray(int size) {
            return new Emotion[size];
        }
    };
}
