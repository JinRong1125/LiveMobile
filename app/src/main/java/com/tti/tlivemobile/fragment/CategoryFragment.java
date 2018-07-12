package com.tti.tlivemobile.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.QueryCategoryRequest;
import com.tti.tlivelibrary.tliveservice.response.QueryCategoryResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.activity.MainActivity;
import com.tti.tlivemobile.adapter.CategoryAdapter;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.model.Category;
import com.tti.tlivemobile.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dylan_liang on 2017/6/15.
 */

public class CategoryFragment extends Fragment {
    private static final String TAG = "CategoryFragment";

    private View view;
    private ProgressBar progressBar;
    private GridView categoryView;

    private CategoryAdapter categoryAdapter;
    private QueryCategoryRequest queryCategoryRequest;

    private SpiceManager spiceManager;

    private List<Category> categoryList;

    public static Fragment newInstance() {
        return new CategoryFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;

        initView();
        queryCategory();
    }

    private void initView() {
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        categoryView = (GridView) view.findViewById(R.id.category_view);
        categoryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setCategoryStream(position);
            }
        });

        spiceManager = SpiceServiceManager.getInstance().getSpiceManager();
    }

    private void setCategoryList(QueryCategoryResponse response) {
        for (int i = 0; i < response.categoryList.size(); i++) {
            Category category = new Category();
            category.setCategoryId(response.categoryList.get(i).categoryId);
            category.setCategoryName(response.categoryList.get(i).categoryName);
            category.setCategoryImagePath(response.categoryList.get(i).categoryImagePath);
            categoryList.add(category);
        }

        setCategory();
    }

    private void setCategory() {
        categoryAdapter = new CategoryAdapter(getActivity(), categoryList);
        categoryView.setAdapter(categoryAdapter);
    }

    private void setCategoryStream(int position) {
        Intent intent = getActivity().getIntent();
        intent.putExtra(AppConstants.CATEGORY_ITEM, categoryList.get(position));

        AppConstants.CATEGORY_TYPE = AppConstants.CATEGORY_STREAM;
        if (getActivity() != null)
            ((MainActivity) getActivity()).updateFragment();
    }

    private void queryCategory() {
        categoryList = new ArrayList<>();

        queryCategoryRequest = new QueryCategoryRequest();
        spiceManager.execute(queryCategoryRequest, QueryCategoryListener);
    }

    private RequestListener<QueryCategoryResponse> QueryCategoryListener = new RequestListener<QueryCategoryResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            progressBar.setVisibility(View.GONE);
            Utils.requestFailure("QueryCategory");
        }

        @Override
        public void onRequestSuccess(QueryCategoryResponse response) {
            if (response == null) {
                progressBar.setVisibility(View.GONE);
                Utils.requestFailure("QueryCategory");
                return;
            }

            progressBar.setVisibility(View.GONE);
            if (response.Code == Constants.QUERY_SUCCESS)
                setCategoryList(response);
        }
    };
}
