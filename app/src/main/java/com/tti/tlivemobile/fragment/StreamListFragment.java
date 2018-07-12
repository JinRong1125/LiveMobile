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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.QueryPlatformRequest;
import com.tti.tlivelibrary.tliveservice.response.QueryPlatformResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.activity.WatchActivity;
import com.tti.tlivemobile.adapter.StreamListAdapter;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.model.Platform;
import com.tti.tlivemobile.utils.ItemClickSupport;
import com.tti.tlivemobile.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dylan_liang on 2017/3/20.
 */

public class StreamListFragment extends Fragment {
    private static final String TAG = "StreamListFragment";

    private View view;
    private ProgressBar progressBar;
    private TextView nothingText;
    private RecyclerView streamListView;

    private StreamListAdapter streamListAdapter;
    private LinearLayoutManager layoutManager;

    private SpiceManager spiceManager;
    private QueryPlatformRequest queryPlatformRequest;

    private List<Platform> platformList;
    private Platform platform;

    public static Fragment newInstance() {
        return new StreamListFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_streamlist, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        initView();
    }

    public void onResume() {
        super.onResume();
        queryPlatform();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void initView() {
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        nothingText = (TextView) view.findViewById(R.id.nothing_text);

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

        if (platformList.size() != 0)
            setPlatform();
        else
            nothingText.setVisibility(View.VISIBLE);
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

    private void setUserStream(int position) {
        platform = platformList.get(position);

        Intent intent = new Intent(getActivity(), WatchActivity.class);
        intent.putExtra(AppConstants.PLATFORM_ITEM, platform);
        startActivity(intent);
    }

    private void queryPlatform() {
        platformList = new ArrayList<>();
        streamListView.setAdapter(null);

        queryPlatformRequest = new QueryPlatformRequest();
        spiceManager.execute(queryPlatformRequest, QueryPlatformListener);
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
}
