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
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.QueryPlatformByAccountRequest;
import com.tti.tlivelibrary.tliveservice.request.QuerySubscribeRequest;
import com.tti.tlivelibrary.tliveservice.request.UnSubscribeRequest;
import com.tti.tlivelibrary.tliveservice.response.QueryPlatformResponse;
import com.tti.tlivelibrary.tliveservice.response.QuerySubscribeResponse;
import com.tti.tlivelibrary.tliveservice.response.UnSubscribeResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.activity.MainActivity;
import com.tti.tlivemobile.activity.WatchActivity;
import com.tti.tlivemobile.adapter.SubscribingAdapter;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.model.Platform;
import com.tti.tlivemobile.utils.ItemClickSupport;
import com.tti.tlivemobile.utils.Utils;

import net.skoumal.fragmentback.BackFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dylan_liang on 2017/6/20.
 */

public class SubscribingFragment extends Fragment implements BackFragment {
    private static final String TAG = "SubscribingFragment";

    private View view;
    private ProgressBar progressBar;
    private TextView nothingText;
    private RecyclerView subscribingView;
    private ImageButton backButton;

    private SubscribingAdapter subscribingAdapter;
    private LinearLayoutManager layoutManager;

    private SpiceManager spiceManager;
    private QuerySubscribeRequest querySubscribeRequest;
    private UnSubscribeRequest unSubscribeRequest;
    private QueryPlatformByAccountRequest queryPlatformByAccountRequest;

    private List<Platform> platformList, platformOffList;
    private Platform platform;

    private boolean isBack;

    public static Fragment newInstance() {
        return new SubscribingFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscribing, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        isBack = false;

        initView();
    }

    public void onResume() {
        super.onResume();
        querySubscribe();
        progressBar.setVisibility(View.VISIBLE);
    }

    public void onPause() {
        super.onPause();
        SpiceServiceManager.getInstance().cancelRequest(
                querySubscribeRequest,
                unSubscribeRequest,
                queryPlatformByAccountRequest
        );
    }

    private void initView() {
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        nothingText = (TextView) view.findViewById(R.id.nothing_text);

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        subscribingView = (RecyclerView) view.findViewById(R.id.subscribing_view);
        subscribingView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        subscribingView.setLayoutManager(layoutManager);
        ItemClickSupport.addTo(subscribingView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                setUserStream(position);
                Utils.enableDisableView(view, false);
                backButton.setEnabled(true);
                progressBar.setVisibility(View.VISIBLE);
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

    private void setSubscribingList(QuerySubscribeResponse response) {
        for (int i = 0; i < response.subscribeList.size(); i++) {
            platform = new Platform();
            platform.setAccountId(response.subscribeList.get(i).accountId);
            platform.setUserName(response.subscribeList.get(i).userName);
            platform.setAvatarPath(response.subscribeList.get(i).avatarPath);
            platform.setPlatformState(response.subscribeList.get(i).platformState);
            platform.setPlatformTitle(response.subscribeList.get(i).platformTitle);
            platform.setCategoryId(response.subscribeList.get(i).categoryId);
            platform.setCategoryName(response.subscribeList.get(i).categoryName);
            platform.setPlatformImagePath(response.subscribeList.get(i).platformImagePath);

            if (response.subscribeList.get(i).platformState.equals(AppConstants.STATE_ON))  {
                platformList.add(platform);
            }
            else {
                platformOffList.add(platform);
            }
        }

        for (int i = 0; i < platformOffList.size(); i++) {
            platformList.add(platformOffList.get(i));
        }

        setSubscribing();
    }

    private void setSubscribing() {
        subscribingAdapter = new SubscribingAdapter(getActivity(), platformList,
                new SubscribingAdapter.AdapterListener() {
                    @Override
                    public void onUnSubscribeClick(View v, int position) {
                        platform = platformList.get(position);
                        unSubscribe();
                        Utils.enableDisableView(view, false);
                        backButton.setEnabled(true);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
        subscribingView.setAdapter(subscribingAdapter);
    }

    private void setUserStream(int position) {
        platform = platformList.get(position);
        queryPlatformByAccount();
    }

    private void back() {
        if (!isBack) {
            isBack = true;
            AppConstants.USER_TYPE = AppConstants.USER_MAIN;
            if (getActivity() != null)
                ((MainActivity) getActivity()).updateFragment();
        }
    }

    private void querySubscribe() {
        platformList = new ArrayList<>();
        platformOffList = new ArrayList<>();
        subscribingView.setAdapter(null);

        querySubscribeRequest = new QuerySubscribeRequest(Utils.Preferences.getSessionToken());
        spiceManager.execute(querySubscribeRequest, QuerySubscribeListener);
    }

    private RequestListener<QuerySubscribeResponse> QuerySubscribeListener = new RequestListener<QuerySubscribeResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Utils.requestFailure("QuerySubscribe");
            back();
        }

        @Override
        public void onRequestSuccess(QuerySubscribeResponse response) {
            if (response == null) {
                Utils.requestFailure("QuerySubscribe");
                back();
                return;
            }

            Utils.enableDisableView(view, true);
            progressBar.setVisibility(View.GONE);
            if (response.Code == Constants.QUERY_SUCCESS) {
                if (response.subscribeList.size() > 0) {
                    setSubscribingList(response);
                }
                else
                    nothingText.setVisibility(View.VISIBLE);
            }
            else if (response.Code == Constants.QUERY_FAILED) {
                if (!Utils.isTokenMessage(getActivity(), response))
                    nothingText.setVisibility(View.VISIBLE);
                Utils.responseMessage(response.Message);
            }
            else
                nothingText.setVisibility(View.VISIBLE);
        }
    };

    private void unSubscribe() {
        unSubscribeRequest = new UnSubscribeRequest(Utils.Preferences.getSessionToken(), platform.getAccountId());
        spiceManager.execute(unSubscribeRequest, UnSubscribeListener);
    }

    private RequestListener<UnSubscribeResponse> UnSubscribeListener = new RequestListener<UnSubscribeResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            progressBar.setVisibility(View.GONE);
            Utils.enableDisableView(view, true);
            Utils.requestFailure("UnSubscribe");
        }

        @Override
        public void onRequestSuccess(UnSubscribeResponse response) {
            if (response == null) {
                progressBar.setVisibility(View.GONE);
                Utils.enableDisableView(view, true);
                Utils.requestFailure("UnSubscribe");
                return;
            }
            Utils.responseMessage(response.Message);

            if (response.Code == Constants.QUERY_SUCCESS)
                querySubscribe();
            else if (response.Code == Constants.QUERY_FAILED) {
                if (!Utils.isTokenMessage(getActivity(), response)) {
                    progressBar.setVisibility(View.GONE);
                    Utils.enableDisableView(view, true);
                }
            }
            else {
                progressBar.setVisibility(View.GONE);
                Utils.enableDisableView(view, true);
            }
        }
    };

    private void queryPlatformByAccount() {
        queryPlatformByAccountRequest = new QueryPlatformByAccountRequest(platform.getAccountId());
        spiceManager.execute(queryPlatformByAccountRequest, QueryPlatformListener);
    }

    private RequestListener<QueryPlatformResponse> QueryPlatformListener = new RequestListener<QueryPlatformResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Utils.enableDisableView(view, true);
            progressBar.setVisibility(View.GONE);
            Utils.requestFailure("QueryPlatform");
        }

        @Override
        public void onRequestSuccess(QueryPlatformResponse response) {
            if (response == null) {
                Utils.enableDisableView(view, true);
                progressBar.setVisibility(View.GONE);
                Utils.requestFailure("QueryPlatform");
                return;
            }

            Utils.enableDisableView(view, true);
            progressBar.setVisibility(View.GONE);
            if (response.Code == Constants.QUERY_SUCCESS) {
                if (response.platformList.size() > 0) {
                    platform.setStreamUrl(response.platformList.get(0).streamUrl);

                    Intent intent = new Intent(getActivity(), WatchActivity.class);
                    intent.putExtra(AppConstants.PLATFORM_ITEM, platform);
                    startActivity(intent);
                }
            }
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
