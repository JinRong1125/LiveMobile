package com.tti.tlivemobile.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.tti.tlivemobile.R;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.fragment.ChatFragment;
import com.tti.tlivemobile.fragment.PublishFragment;

import net.skoumal.fragmentback.BackFragmentHelper;

/**
 * Created by dylan_liang on 2017/6/16.
 */

public class PublishActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_publish);

        setStreamPage();
    }

    @Override
    public void onBackPressed() {
        if(!BackFragmentHelper.fireOnBackPressedEvent(this)) {
            super.onBackPressed();
        }
    }

    private void setStreamPage() {
        AppConstants.CHAT_TYPE = AppConstants.CHAT_PUBLISH;

        fragmentManager = getSupportFragmentManager();

        Fragment publishFragment = PublishFragment.newInstance();
        fragmentManager.beginTransaction().replace(R.id.stream_container, publishFragment).commit();

        Fragment chatFragment = ChatFragment.newInstance();
        fragmentManager.beginTransaction().replace(R.id.chat_container, chatFragment).commit();
    }
}
