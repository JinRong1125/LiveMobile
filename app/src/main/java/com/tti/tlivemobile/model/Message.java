package com.tti.tlivemobile.model;

/**
 * Created by dylan_liang on 2017/3/14.
 */

public class Message {

    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_LOG = 1;
    public static final int TYPE_ACTION = 2;

    private int mType;
    private String mMessage;
    private String mUsername;
    private boolean mIsEmoted;

    private Message() {}

    public int getType() {
        return mType;
    };

    public String getMessage() {
        return mMessage;
    };

    public String getUsername() {
        return mUsername;
    };

    public boolean getIsEmoted() {
        return mIsEmoted;
    };

    public static class Builder {
        private final int mType;
        private String mUsername;
        private String mMessage;
        private boolean mIsEmoted;

        public Builder(int type) {
            mType = type;
        }

        public Builder username(String username) {
            mUsername = username;
            return this;
        }

        public Builder message(String message) {
            mMessage = message;
            return this;
        }

        public Builder isEmoted(boolean isEmoted) {
            mIsEmoted = isEmoted;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.mType = mType;
            message.mUsername = mUsername;
            message.mMessage = mMessage;
            message.mIsEmoted = mIsEmoted;
            return message;
        }
    }
}
