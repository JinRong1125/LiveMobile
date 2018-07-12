package com.tti.tlivemobile.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.fragment.AddIntroducionFragment;
import com.tti.tlivemobile.fragment.CategoryFragment;
import com.tti.tlivemobile.fragment.CategoryStreamFragment;
import com.tti.tlivemobile.fragment.EditIntroducionFragment;
import com.tti.tlivemobile.fragment.IntroductionFragment;
import com.tti.tlivemobile.fragment.NewStreamFragment;
import com.tti.tlivemobile.fragment.SearchFragment;
import com.tti.tlivemobile.fragment.StreamListFragment;
import com.tti.tlivemobile.fragment.SubscribersFragment;
import com.tti.tlivemobile.fragment.SubscribingFragment;
import com.tti.tlivemobile.fragment.UserFragment;

/**
 * Created by dylan_liang on 2017/6/15.
 */

public class FragmentAdapter extends FragmentStatePagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

    private static final int PAGE_COUNT = 4;

    private SparseArray<Fragment> fragmentArray = new SparseArray<>();

    private Context context;

    public FragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case AppConstants.USER_PAGE:
                switch (AppConstants.USER_TYPE) {
                    case AppConstants.USER_MAIN:
                        return UserFragment.newInstance();
                    case AppConstants.USER_STREAM:
                        return NewStreamFragment.newInstance();
                    case AppConstants.USER_SUBSCRIBING:
                        return SubscribingFragment.newInstance();
                    case AppConstants.USER_SUBSCRIBERS:
                        return SubscribersFragment.newInstance();
                    case AppConstants.USER_INTRODUCTION:
                        return IntroductionFragment.newInstance();
                    case AppConstants.USER_ADD_INTRODUCTION:
                        return AddIntroducionFragment.newInstance();
                    case AppConstants.USER_EDIT_INTRODUCTION:
                        return EditIntroducionFragment.newInstance();
                }
            case AppConstants.STREAM_PAGE:
                return StreamListFragment.newInstance();
            case AppConstants.CATEGORY_PAGE:
                switch (AppConstants.CATEGORY_TYPE) {
                    case AppConstants.CATEGORY_MAIN:
                        return CategoryFragment.newInstance();
                    case AppConstants.CATEGORY_STREAM:
                        return CategoryStreamFragment.newInstance();
                }
            case AppConstants.SEARCH_PAGE:
                return SearchFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
//            case 0:
//                return context.getString(R.string.page_user);
//            case 1:
//                return context.getString(R.string.page_streamer);
//            case 2:
//                return context.getString(R.string.page_category);
//            case 3:
//                return context.getString(R.string.page_search);
            default:
                return "";
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragmentArray.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        fragmentArray.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getPageIconResId(int position) {
        switch (position) {
            case 0:
                return R.drawable.menu_user;
            case 1:
                return R.drawable.menu_streamer;
            case 2:
                return R.drawable.menu_category;
            case 3:
                return R.drawable.menu_search;
        }
        return position;
    }
}
