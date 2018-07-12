package com.tti.tlivemobile.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.astuetz.PagerSlidingTabStrip;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.adapter.FragmentAdapter;
import com.tti.tlivemobile.constant.AppConstants;

import net.skoumal.fragmentback.BackFragmentHelper;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private FragmentAdapter fragmentAdapter;
    private PagerSlidingTabStrip pagerTabs;
    private FragmentManager fragmentManager;

    private static final int INDICATOR_HEIGHT = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        
        initView();
        setViewPage();
    }

    @Override
    public void onBackPressed() {
        if(!BackFragmentHelper.fireOnBackPressedEvent(this))
            moveTaskToBack(true);
    }

    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        pagerTabs = (PagerSlidingTabStrip) findViewById(R.id.pager_tabs);
    }

    private void setViewPage() {
        AppConstants.USER_TYPE = AppConstants.USER_MAIN;
        AppConstants.CATEGORY_TYPE = AppConstants.CATEGORY_MAIN;

        fragmentManager = getSupportFragmentManager();
        fragmentAdapter = new FragmentAdapter(fragmentManager, this);
        viewPager.setAdapter(fragmentAdapter);

        pagerTabs.setShouldExpand(true);
        pagerTabs.setViewPager(viewPager);
        pagerTabs.setIndicatorHeight(INDICATOR_HEIGHT);
        pagerTabs.setDividerColor(ContextCompat.getColor(this, R.color.transparent));
        pagerTabs.setIndicatorColor(ContextCompat.getColor(this, R.color.white_second));
        pagerTabs.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_second));
    }

    public void updateFragment() {
        fragmentAdapter.notifyDataSetChanged();
    }
}
