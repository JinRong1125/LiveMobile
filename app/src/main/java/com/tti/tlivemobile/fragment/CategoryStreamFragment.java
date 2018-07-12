package com.tti.tlivemobile.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.QueryPlatformByCategoryRequest;
import com.tti.tlivelibrary.tliveservice.response.QueryPlatformResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.activity.MainActivity;
import com.tti.tlivemobile.activity.WatchActivity;
import com.tti.tlivemobile.adapter.StreamListAdapter;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.model.Category;
import com.tti.tlivemobile.model.Platform;
import com.tti.tlivemobile.utils.ItemClickSupport;
import com.tti.tlivemobile.utils.Utils;

import net.skoumal.fragmentback.BackFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dylan_liang on 2017/3/20.
 */

public class CategoryStreamFragment extends Fragment implements BackFragment {
    private static final String TAG = "CategoryStreamFragment";

    private View view;
    private ProgressBar progressBar;
    private ImageButton backButton;
    private TextView categoryName, nothingText;
    private RecyclerView streamListView;

    private StreamListAdapter streamListAdapter;
    private LinearLayoutManager layoutManager;

    private SpiceManager spiceManager;
    private QueryPlatformByCategoryRequest queryPlatformByCategoryRequest;

    private List<Platform> platformList;
    private Platform platform;
    private Category category;

    private boolean isBack;

    public static Fragment newInstance() {
        return new CategoryStreamFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_stream, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        isBack = false;

        initView();
    }

    public void onResume() {
        super.onResume();
        queryPlatformByCategory();
        progressBar.setVisibility(View.VISIBLE);
    }

    public void onPause() {
        super.onPause();
        SpiceServiceManager.getInstance().cancelRequest(queryPlatformByCategoryRequest);
    }

    private void initView() {
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        nothingText = (TextView) view.findViewById(R.id.nothing_text);

        category =  getActivity().getIntent().getParcelableExtra(AppConstants.CATEGORY_ITEM);
        categoryName =  (TextView) view.findViewById(R.id.category_name);
        categoryName.setText(category.getCategoryName());

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        streamListView = (RecyclerView) view.findViewById(R.id.streamlist_view);
        streamListView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        streamListView.setLayoutManager(layoutManager);
        ItemClickSupport.addTo(streamListView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                setUserStream(position);
            }
        });

        backButton = (ImageButton) view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        spiceManager = SpiceServiceManager.getInstance().getSpiceManager();
    }

    private void setPlatformList(QueryPlatformResponse response) {
        for (int i = 0; i < response.platformList.size(); i++) {
            if (response.platformList.get(i).platformState.equals(AppConstants.STATE_ON))  {
                platform = new Platform();
                platform.setAccountId(response.platformList.get(i).accountId);
                platform.setPlatformState(response.platformList.get(i).platformState);
                platform.setUserName(response.platformList.get(i).userName);
                platform.setPlatformTitle(response.platformList.get(i).platformTitle);
                platform.setCategoryId(response.platformList.get(i).categoryId);
                platform.setCategoryName(response.platformList.get(i).categoryName);
                platform.setStreamUrl(response.platformList.get(i).streamUrl);
                platform.setPlatformImagePath(response.platformList.get(i).platformImagePath);
                platform.setViewersValue(response.platformList.get(i).viewersValue);
                platformList.add(platform);

                sortViewersValue(i);
            }
        }

        setPlatform();
    }

    private void setPlatform() {
        streamListAdapter = new StreamListAdapter(getActivity(), platformList);
        streamListView.setAdapter(streamListAdapter);
    }

    private void sortViewersValue(int base) {
        if (base > 0) {
            for (int i = base; i > 0; i--) {
                if (platformList.get(base).getViewersValue() > platformList.get(i - 1).getViewersValue()) {
                    Platform platform = platformList.get(base);
                    platformList.set(i, platformList.get(i - 1));
                    platformList.set(i - 1, platform);
                    base--;
                }
                else
                    break;
            }
        }
    }

    private void back() {
        if (!isBack) {
            isBack = true;
            AppConstants.CATEGORY_TYPE = AppConstants.CATEGORY_MAIN;
            if (getActivity() != null)
                ((MainActivity) getActivity()).updateFragment();
        }
    }

    private void setUserStream(int position) {
        platform = platformList.get(position);

        Intent intent = new Intent(getActivity(), WatchActivity.class);
        intent.putExtra(AppConstants.PLATFORM_ITEM, platform);
        startActivity(intent);
    }

    private void queryPlatformByCategory() {
        platformList = new ArrayList<>();
        streamListView.setAdapter(null);

        queryPlatformByCategoryRequest = new QueryPlatformByCategoryRequest(
                category.getCategoryId());
        spiceManager.execute(queryPlatformByCategoryRequest, QueryPlatformListener);
    }

    RequestListener<QueryPlatformResponse> QueryPlatformListener = new RequestListener<QueryPlatformResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            progressBar.setVisibility(View.GONE);
            Utils.requestFailure("QueryPlatform");
        }

        @Override
        public void onRequestSuccess(QueryPlatformResponse response) {
            if (response == null) {
                progressBar.setVisibility(View.GONE);
                Utils.requestFailure("QueryPlatform");
                return;
            }

            progressBar.setVisibility(View.GONE);
            if (response.Code == Constants.QUERY_SUCCESS) {
                if (response.platformList.size() > 0)
                    setPlatformList(response);
                else
                    nothingText.setVisibility(View.VISIBLE);
            }
            else
                nothingText.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public boolean onBackPressed() {
        back();
        return true;
    }

    @Override
    public int getBackPriority() {
        return 0;
    }
}
