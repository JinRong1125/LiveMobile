package com.tti.tlivemobile.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.DeleteChannelRequest;
import com.tti.tlivelibrary.tliveservice.request.QueryChannelByIdRequest;
import com.tti.tlivelibrary.tliveservice.response.DeleteChannelResponse;
import com.tti.tlivelibrary.tliveservice.response.QueryChannelByIdResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.activity.MainActivity;
import com.tti.tlivemobile.adapter.IntroChannelAdapter;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.model.Channel;
import com.tti.tlivemobile.utils.Utils;

import net.skoumal.fragmentback.BackFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dylan_liang on 2017/6/27.
 */

public class IntroductionFragment extends Fragment implements BackFragment {
    private static final String TAG = "IntroductionFragment";

    private View view;
    private LinearLayout optionLayout;
    private ProgressBar progressBar;
    private TextView nothingText;
    private RecyclerView introductionView;
    private ImageButton backButton, addButton;

    private LinearLayoutManager layoutManager;
    private IntroChannelAdapter introChannelAdapter;

    private SpiceManager spiceManager;
    private QueryChannelByIdRequest queryChannelByIdRequest;
    private DeleteChannelRequest deleteChannelRequest;

    private List<Channel> channelList;
    private Channel channel;

    private boolean isBack;

    private OptionHandler optionHandler;

    private static final int OPTION_HIDE = 0;
    private static final int OPTION_SHOW = 1;
    private static final int OPTION_TIMEOUT = 3000;

    public static Fragment newInstance() {
        return new IntroductionFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_introduction, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        isBack = false;

        initView();
        queryChannelById();
    }

    public void onPause() {
        super.onPause();
        SpiceServiceManager.getInstance().cancelRequest(
                queryChannelByIdRequest,
                deleteChannelRequest
        );
    }

    private void initView() {
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        nothingText = (TextView) view.findViewById(R.id.nothing_text);

        backButton = (ImageButton) view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        addButton = (ImageButton) view.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppConstants.USER_TYPE = AppConstants.USER_ADD_INTRODUCTION;
                if (getActivity() != null)
                    ((MainActivity) getActivity()).updateFragment();
            }
        });

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        introductionView = (RecyclerView) view.findViewById(R.id.introduction_view);
        introductionView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        introductionView.setLayoutManager(layoutManager);

        optionHandler = new OptionHandler();

        spiceManager = SpiceServiceManager.getInstance().getSpiceManager();
    }

    private void setChannelList(QueryChannelByIdResponse response) {
        for (int i = 0; i < response.channelList.size(); i++) {
            Channel channel = new Channel();
            channel.setAccountId(response.channelList.get(i).accountId);
            channel.setContentId(response.channelList.get(i).contentId);
            channel.setContentImagePath(response.channelList.get(i).contentImagePath);
            channel.setContentText(response.channelList.get(i).contentText);

            channelList.add(channel);
        }

        if (channelList.size() != 0)
            setChannel();
    }

    private void setChannel() {
        introChannelAdapter = new IntroChannelAdapter(getActivity(), channelList,
                new IntroChannelAdapter.AdapterListener() {

                    @Override
                    public void onContentImageClick(View v, int position) {
                        if (optionLayout != null)
                            optionLayout.setVisibility(View.GONE);

                        optionHandler.removeMessages(OPTION_HIDE);
                        optionLayout = (LinearLayout) v.findViewById(R.id.option_layout);
                        optionHandler.sendEmptyMessage(OPTION_SHOW);
                    }

                    @Override
                    public void onEditChannelClick(View v, int position) {
                        channel = channelList.get(position);
                        Intent intent = getActivity().getIntent();
                        intent.putExtra(AppConstants.CHANNEL_ITEM, channel);
                        AppConstants.USER_TYPE = AppConstants.USER_EDIT_INTRODUCTION;
                        if (getActivity() != null)
                            ((MainActivity) getActivity()).updateFragment();
                    }

                    @Override
                    public void onDeleteChannelClick(View v, int position) {
                        channel = channelList.get(position);
                        deleteChannel();
                        Utils.enableDisableView(view, false);
                        backButton.setEnabled(true);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
        introductionView.setAdapter(introChannelAdapter);
    }

    @Override
    public boolean onBackPressed() {
        back();
        return true;
    }

    @Override
    public int getBackPriority() {
        return 0;
    }

    private class OptionHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OPTION_SHOW:
                    showOption();
                    break;
                case OPTION_HIDE:
                    hideOption();
                    break;
            }
        }
    }

    private void showOption() {
        optionLayout.setVisibility(View.VISIBLE);
        optionHandler.sendMessageDelayed(optionHandler.obtainMessage(OPTION_HIDE), OPTION_TIMEOUT);
    }

    private void hideOption() {
        optionLayout.setVisibility(View.GONE);
    }

    private void finishProcess() {
        Utils.enableDisableView(view, true);
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

    private void queryChannelById() {
        channelList = new ArrayList<>();
        introductionView.setAdapter(null);

        queryChannelByIdRequest = new QueryChannelByIdRequest(
                Utils.Preferences.getSessionToken(), Utils.Preferences.getAccount().getAccountId());
        spiceManager.execute(queryChannelByIdRequest, QueryChannelByIdListener);
    }

    RequestListener<QueryChannelByIdResponse> QueryChannelByIdListener = new RequestListener<QueryChannelByIdResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            finishProcess();
            Utils.requestFailure("QueryChannelById");
        }

        @Override
        public void onRequestSuccess(QueryChannelByIdResponse response) {
            if (response == null) {
                finishProcess();
                Utils.requestFailure("QueryChannelById");
                return;
            }
            Utils.responseMessage(response.Message);

            finishProcess();
            if (response.Code == Constants.QUERY_SUCCESS) {
                if (response.channelList.size() > 0)
                    setChannelList(response);
                else
                    nothingText.setVisibility(View.VISIBLE);
            }
            else if (response.Code == Constants.QUERY_FAILED) {
                if (!Utils.isTokenMessage(getActivity(), response))
                    nothingText.setVisibility(View.VISIBLE);
            }
            else
                nothingText.setVisibility(View.VISIBLE);
        }
    };

    private void deleteChannel() {
        deleteChannelRequest = new DeleteChannelRequest(
                Utils.Preferences.getSessionToken(), channel.getContentId());
        spiceManager.execute(deleteChannelRequest, DeleteChannelListener);
    }

    RequestListener<DeleteChannelResponse> DeleteChannelListener = new RequestListener<DeleteChannelResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            finishProcess();
            Utils.requestFailure("QueryChannelById");
        }

        @Override
        public void onRequestSuccess(DeleteChannelResponse response) {
            if (response == null) {
                finishProcess();
                return;
            }
            Utils.responseMessage(response.Message);

            if (response.Code == Constants.QUERY_SUCCESS)
                queryChannelById();
            else if (response.Code == Constants.QUERY_FAILED) {
                if (!Utils.isTokenMessage(getActivity(), response))
                    finishProcess();
            }
            else
                finishProcess();
        }
    };
}
