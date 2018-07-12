package com.tti.tlivemobile.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.response.BaseResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.activity.LoginActivity;
import com.tti.tlivemobile.application.TliveMoblie;
import com.tti.tlivemobile.model.Account;
import com.tti.tlivemobile.model.LoginInformation;

/**
 * Created by dylan_liang on 2017/6/15.
 */

public class Utils {

    public static boolean isPermissionGranted(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    public static void requestMultiplePermissions(Activity activity) {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(permissions, 1);
        }
    }

    public static boolean isPad(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static float setDipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static void hideKeyView(Context context, View view) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow( view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void enableDisableView(View view, boolean isEnable) {
        view.setEnabled(isEnable);
        if ( view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;

            for (int id = 0; id < group.getChildCount(); id++) {
                enableDisableView(group.getChildAt(id), isEnable);
            }
        }
    }

    public static void requestFailure(String request) {
        Toast.makeText(TliveMoblie.getContext(),
                request + TliveMoblie.getContext().getResources().getString(R.string.request_failure), Toast.LENGTH_SHORT).show();
    }

    public static void responseMessage(String message) {
        if (message != null)
            Toast.makeText(TliveMoblie.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static boolean isTokenMessage(Activity activity, BaseResponse response) {
        if (response.TokenStatus != null) {
            int TokenStatus = response.TokenStatus;
            switch (TokenStatus) {
                case Constants.INVALID_TOKEN:
                    backLogin(activity);
                    break;
                case Constants.ERROR_TOKEN:
                    backLogin(activity);
                    break;
                case Constants.EXCEED_TOKEN:
                    backLogin(activity);
                    break;
            }
            return true;
        }
        else
            return false;
    }

    public static void backLogin(Activity activity) {
        if (activity != null) {
            Preferences.clearAll();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        }
    }

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(TliveMoblie.getContext());
    }

    public static class Preferences {
        private static String ACCOUNT_SESSION_TOKEN = "SessionToken";
        private static String ACCOUNT_UUID = "UUID";
        private static String ACCOUNT_INFO = "AccountInfo";
        private static String LOGIN_INFO = "LoginInfo";

        public static void clearAll() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }

        public static void setSessionToken(String SessionToken) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(ACCOUNT_SESSION_TOKEN, SessionToken);
            editor.apply();
        }

        public static String getSessionToken() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            return sharedPreferences.getString(ACCOUNT_SESSION_TOKEN, "");
        }

        public static void setUUID(String UUID) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(ACCOUNT_UUID, UUID);
            editor.apply();
        }

        public static String getUUID() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            return sharedPreferences.getString(ACCOUNT_UUID, "");
        }

        public static void setAccount(Account account) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(ACCOUNT_INFO, new Gson().toJson(account));
            editor.apply();
        }

        public static Account getAccount() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            String json = sharedPreferences.getString(ACCOUNT_INFO, "");

            return new Gson().fromJson(json, Account.class);
        }

        public static void setLoginInfo(LoginInformation loginInfo) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(LOGIN_INFO, new Gson().toJson(loginInfo));
            editor.apply();
        }

        public static LoginInformation getLoginInfo() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            String json = sharedPreferences.getString(LOGIN_INFO, null);

            return new Gson().fromJson(json, LoginInformation.class);
        }
    }
}
