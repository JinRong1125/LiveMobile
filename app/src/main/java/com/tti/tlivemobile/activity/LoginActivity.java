package com.tti.tlivemobile.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.tti.tlivemobile.R;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.fragment.LoginFragment;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.utils.Utils;

/**
 * Created by dylan_liang on 2017/6/15.
 */

public class LoginActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        checkPermission();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void checkPermission() {
        if (Utils.isPermissionGranted(this)) {
            SpiceServiceManager.getInstance().startSpice();
            setLoginPage();
        }
        else
            Utils.requestMultiplePermissions(this);
    }

    private void setLoginPage() {
        setContentView(R.layout.activity_login);
        fragmentManager = getSupportFragmentManager();
        Fragment loginFragment = LoginFragment.newInstance();
        fragmentManager.beginTransaction().replace(R.id.login_container, loginFragment).commit();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppConstants.PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SpiceServiceManager.getInstance().startSpice();
                setLoginPage();
            }
            else
                finish();
        }
    }
}