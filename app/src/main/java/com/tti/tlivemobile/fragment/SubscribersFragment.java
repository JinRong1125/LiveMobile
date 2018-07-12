package com.tti.tlivemobile.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.QueryPlatformByAccountRequest;
import com.tti.tlivelibrary.tliveservice.request.QuerySubscribeMeRequest;
import com.tti.tlivelibrary.tliveservice.response.QueryPlatformResponse;
import com.tti.tlivelibrary.tliveservice.response.QuerySubscribeMeResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.activity.MainActivity;
import com.tti.tlivemobile.activity.WatchActivity;
import com.tti.tlivemobile.adapter.SubscribersAdapter;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.model.Platform;
import com.tti.tlivemobile.utils.Utils;

import net.skoumal.fragmentback.BackFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dylan_liang on 2017/6/20.
 */

public class SubscribersFragment extends Fragment implements BackFragment {
    private static final String TAG = "SubscribersFragment";

    private View view;
    private TextView nothingText;
    private ProgressBar progressBar;
    private GridView subscribersView;
    private ImageButton backButton;

    private SubscribersAdapter subscribersAdapter;

    private SpiceManager spiceManager;
    private QuerySubscribeMeRequest querySubscribeMeRequest;
    private QueryPlatformByAccountRequest queryPlatformByAccountRequest;

    private List<Platform> platformList;
    private Platform platform;

    private boolean isBack;

    public static Fragment newInstance() {
        return new SubscribersFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscribers, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        isBack = false;

        initView();
    }

    public void onResume() {
        super.onResume();
        querySubscribeMe();
        progressBar.setVisibility(View.VISIBLE);
    }

    public void onPause() {
        super.onPause();
        SpiceServiceManager.getInstance().cancelRequest(
                querySubscribeMeRequest,
                queryPlatformByAccountRequest
        );
    }

    private void initView() {
        nothingText = (TextView) view.findViewById(R.id.nothing_text);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        subscribersView = (GridView) view.findViewById(R.id.subscribers_view);
        subscribersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setUserStream(position);
                subscribersView.setEnabled(false);
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

    private void setSubscribersList(QuerySubscribeMeResponse response) {
        for (int i = 0; i < response.subscribemeList.size(); i++) {
            platform = new Platform();
            platform.setAccountId(response.subscribemeList.get(i).accountId);
            platform.setUserName(response.subscribemeList.get(i).userName);
            platform.setAvatarPath(response.subscribemeList.get(i).avatarPath);
            platformList.add(platform);
        }

        setSubscribers();
    }

    private void setSubscribers() {
        subscribersAdapter = new SubscribersAdapter(getActivity(), platformList);
        subscribersView.setAdapter(subscribersAdapter);
    }

    private void setUserStream(int position) {
        platform = platformList.get(position);
        queryPlatformByAccount();
    }

    private void finishQueryPlatform() {
        subscribersView.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    private void back() {
        if (!isBack) {
            isBack = true;
            AppConstants.USER_TYPE = AppConstants.USER_MAIN;
            if (getActivity() != null)
                ((MainActivity) getActivity()).updateFragment();
        }
    }

    private void querySubscribeMe() {
        platformList = new ArrayList<>();
        subscribersView.setAdapter(null);

        querySubscribeMeRequest = new QuerySubscribeMeRequest(Utils.Preferences.getSessionToken());
        spiceManager.execute(querySubscribeMeRequest, QuerySubscribeMeListener);
    }

    private RequestListener<QuerySubscribeMeResponse> QuerySubscribeMeListener = new RequestListener<QuerySubscribeMeResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Utils.requestFailure("QuerySubscribeMe");
            back();
        }

        @Override
        public void onRequestSuccess(QuerySubscribeMeResponse response) {
            if (response == null) {
                Utils.requestFailure("QuerySubscribeMe");
                back();
                return;
            }

            progressBar.setVisibility(View.GONE);
            if (response.Code == Constants.QUERY_SUCCESS) {
                if (response.subscribemeList.size() > 0)
                    setSubscribersList(response);
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

    private void queryPlatformByAccount() {
        queryPlatformByAccountRequest = new QueryPlatformByAccountRequest(platform.getAccountId());
        spiceManager.execute(queryPlatformByAccountRequest, QueryPlatformListener);
    }

    private RequestListener<QueryPlatformResponse> QueryPlatformListener = new RequestListener<QueryPlatformResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            finishQueryPlatform();
            Utils.requestFailure("QueryPlatform");
        }

        @Override
        public void onRequestSuccess(QueryPlatformResponse response) {
            if (response == null) {
                finishQueryPlatform();
                Utils.requestFailure("QueryPlatform");
                return;
            }

            finishQueryPlatform();
            if (response.Code == Constants.QUERY_SUCCESS) {
                if (response.platformList.size() > 0) {
                    platform.setPlatformTitle(response.platformList.get(0).platformTitle);
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
