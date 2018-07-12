package com.tti.tlivemobile.fragment;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.QuerySubscribeRequest;
import com.tti.tlivelibrary.tliveservice.request.SubscribeRequest;
import com.tti.tlivelibrary.tliveservice.request.UnSubscribeRequest;
import com.tti.tlivelibrary.tliveservice.response.QuerySubscribeResponse;
import com.tti.tlivelibrary.tliveservice.response.SubscribeResponse;
import com.tti.tlivelibrary.tliveservice.response.UnSubscribeResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.model.ExoPlayerContainer;
import com.tti.tlivemobile.model.InformationContainer;
import com.tti.tlivemobile.model.Platform;
import com.tti.tlivemobile.utils.Utils;

import net.skoumal.fragmentback.BackFragment;

/**
 * Created by dylan_liang on 2017/6/20.
 */

public class WatchFragment extends Fragment implements BackFragment {
    private static final String TAG = "WatchFragment";

    private View view;
    private FrameLayout watchFragment, streamContainer, chatContainer, infoContainer, infoLayout,
            playerLayout, imageLayout, optionLayout;
    private TextView streamTitle;
    private ImageView platformImage;
    private ImageButton backButton, subscribeButton, informationButton, zoomButton, closeButton;
    private ProgressBar progressBar;

    private ExoPlayerContainer exoPlayerContainer;
    private InformationContainer informationContainer;
    private OptionHandler optionHandler;

    private SpiceManager spiceManager;
    private QuerySubscribeRequest querySubscribeRequest;
    private SubscribeRequest subscribeRequest;
    private UnSubscribeRequest unSubscribeRequest;

    private Platform platform;
    private boolean isPause;
    private boolean isSubscribed;
    private boolean isUserZoomIn;

    private static final int OPTION_HIDE = 0;
    private static final int OPTION_TIMEOUT = 5000;

    public static Fragment newInstance() {
        return new WatchFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watch, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        isPause = false;
        isUserZoomIn = false;

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        spiceManager = SpiceServiceManager.getInstance().getSpiceManager();
        querySubscribe();
        if (getActivity() != null) initView();
    }

    public void onResume() {
        super.onResume();
        if (isPause)
            exoPlayerContainer.requestFocus();
        isPause = false;
    }

    public void onPause() {
        super.onPause();
        exoPlayerContainer.getPlayer().setPlayWhenReady(false);
        SpiceServiceManager.getInstance().cancelRequest(
                querySubscribeRequest,
                subscribeRequest,
                unSubscribeRequest
        );

        if (isUserZoomIn) {
            isUserZoomIn = false;
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        isPause = true;
    }

    public void onDestroy() {
        super.onDestroy();
        if (exoPlayerContainer != null)
            exoPlayerContainer.releasePlayer();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (newConfig.orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                showBar();
                chatContainer.setVisibility(View.VISIBLE);
                zoomButton.setBackgroundResource(R.drawable.icon_largescreen);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_USER:
                hideBar();
                chatContainer.setVisibility(View.GONE);
                zoomButton.setBackgroundResource(R.drawable.icon_smallscreen);
                break;
        }

        if (!isUserZoomIn)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    private void initView() {
        platform = getActivity().getIntent().getParcelableExtra(AppConstants.PLATFORM_ITEM);

        watchFragment = (FrameLayout) view.findViewById(R.id.fragment_watch);
        watchFragment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showOption();
                return false;
            }
        });

        streamContainer = (FrameLayout) getActivity().findViewById(R.id.stream_container);
        chatContainer = (FrameLayout) getActivity().findViewById(R.id.chat_container);
        infoContainer = (FrameLayout) getActivity().findViewById(R.id.info_container);
        infoLayout = (FrameLayout) getActivity().findViewById(R.id.info_layout);

        playerLayout = (FrameLayout) view.findViewById(R.id.player_layout);
        imageLayout = (FrameLayout) view.findViewById(R.id.image_layout);
        optionLayout = (FrameLayout) view.findViewById(R.id.option_layout);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        streamTitle = (TextView) view.findViewById(R.id.stream_title);
        streamTitle.setText(platform.getPlatformTitle());

        exoPlayerContainer = new ExoPlayerContainer();
        exoPlayerContainer.initPlayerView(
                getActivity(),
                platform,
                playerLayout,
                imageLayout,
                progressBar,
                streamTitle,
                spiceManager);

        playerLayout.addView(exoPlayerContainer.getPlayerView());

        platformImage = (ImageView) view.findViewById(R.id.platform_image);
        setPlatformImage();

        backButton = (ImageButton) view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        subscribeButton = (ImageButton) view.findViewById(R.id.subscribe_button);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSubscribed)
                    subscribe();
                else
                    unSubscribe();
                startSubscribe();
            }
        });

        informationButton = (ImageButton) view.findViewById(R.id.information_button);
        informationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setInformation();
            }
        });

        zoomButton = (ImageButton) view.findViewById(R.id.zoom_button);
        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (chatContainer.getVisibility()) {
                    case View.VISIBLE:
                        isUserZoomIn = true;
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        break;
                    case View.GONE:
                        isUserZoomIn = false;
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        break;
                }
            }
        });

        optionHandler = new OptionHandler();
        showOption();
    }

    private void setPlatformImage() {
        Glide.with(getActivity())
                .load(platform.getPlatformImagePath())
                .asBitmap()
                .error(R.drawable.stream_l)
                .into(platformImage);
    }

    @Override
    public boolean onBackPressed() {
        if (infoContainer.getVisibility() == View.VISIBLE)
            closeInformation();
        else
            getActivity().finish();
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
                case OPTION_HIDE:
                    hideOption();
                    break;
            }
        }
    }

    private void showOption() {
        optionHandler.removeMessages(OPTION_HIDE);
        optionHandler.sendMessageDelayed(optionHandler.obtainMessage(OPTION_HIDE), OPTION_TIMEOUT);

        showBar();
        optionLayout.setVisibility(View.VISIBLE);
    }

    private void stayShowOption() {
        optionHandler.removeMessages(OPTION_HIDE);
        optionLayout.setVisibility(View.VISIBLE);
    }

    private void hideOption() {
        if (chatContainer.getVisibility() != View.VISIBLE)
            hideBar();
        optionLayout.setVisibility(View.GONE);
    }

    private void showBar() {
        if (getActivity() != null) {
            View decorView = getActivity().getWindow().getDecorView();
            int UIoptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(UIoptions);
        }
    }

    private void hideBar() {
        if (getActivity() != null) {
            View decorView = getActivity().getWindow().getDecorView();
            int UIoptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(UIoptions);
        }
    }

    private void startSubscribe() {
        stayShowOption();
        subscribeButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void finishSubscribe() {
        showOption();
        subscribeButton.setEnabled(true);
        if (exoPlayerContainer.getPlayer().getPlaybackState() != ExoPlayer.STATE_BUFFERING)
            progressBar.setVisibility(View.GONE);
    }

    private void setInformation() {
        Utils.enableDisableView(streamContainer, false);
        Utils.enableDisableView(chatContainer, false);
        watchFragment.setEnabled(true);
        infoContainer.setVisibility(View.VISIBLE);

        informationContainer = new InformationContainer(
                getActivity(),
                platform.getAccountId(),
                platform.getUserName(),
                platform.getPlatformImagePath(),
                spiceManager);
        View infoView = informationContainer.getInfoView();
        infoLayout.addView(infoView);

        closeButton = (ImageButton) infoView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeInformation();
            }
        });
    }

    private void closeInformation() {
        Utils.enableDisableView(streamContainer, true);
        Utils.enableDisableView(chatContainer, true);
        infoContainer.setVisibility(View.GONE);

        infoLayout.removeAllViews();
        informationContainer = null;
    }

    private void querySubscribe() {
        querySubscribeRequest = new QuerySubscribeRequest(Utils.Preferences.getSessionToken());
        spiceManager.execute(querySubscribeRequest, QuerySubscribeListener);
    }

    private RequestListener<QuerySubscribeResponse> QuerySubscribeListener = new RequestListener<QuerySubscribeResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Utils.requestFailure("QuerySubscribe");
        }

        @Override
        public void onRequestSuccess(QuerySubscribeResponse response) {
            if (response == null) {
                Utils.requestFailure("QuerySubscribe");
                return;
            }

            if (response.Code == Constants.QUERY_SUCCESS) {
                if (response.subscribeList.size() > 0) {
                    for (int i = 0; i < response.subscribeList.size(); i++) {
                        if (response.subscribeList.get(i).accountId == platform.getAccountId()) {
                            subscribeButton.setBackgroundResource(R.drawable.icon_like);
                            isSubscribed = true;
                            return;
                        }
                        subscribeButton.setBackgroundResource(R.drawable.icon_like_w);
                        isSubscribed = false;
                    }
                }
                else {
                    subscribeButton.setBackgroundResource(R.drawable.icon_like_w);
                    isSubscribed = false;
                }
            }
            else if (response.Code == Constants.QUERY_FAILED) {
                Utils.isTokenMessage(getActivity(), response);
                Utils.responseMessage(response.Message);
            }
        }
    };

    private void subscribe() {
        subscribeRequest = new SubscribeRequest(Utils.Preferences.getSessionToken(), platform.getAccountId());
        spiceManager.execute(subscribeRequest, SubscribeListener);
    }

    private RequestListener<SubscribeResponse> SubscribeListener = new RequestListener<SubscribeResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            finishSubscribe();
            Utils.requestFailure("Subscribe");
        }

        @Override
        public void onRequestSuccess(SubscribeResponse response) {
            if (response == null) {
                finishSubscribe();
                Utils.requestFailure("Subscribe");
                return;
            }
            Utils.responseMessage(response.Message);

            finishSubscribe();
            if (response.Code == Constants.QUERY_SUCCESS) {
                subscribeButton.setBackgroundResource(R.drawable.icon_like);
                isSubscribed = true;
            }
            else if (response.Code == Constants.QUERY_FAILED)
                Utils.isTokenMessage(getActivity(), response);
        }
    };

    private void unSubscribe() {
        unSubscribeRequest = new UnSubscribeRequest(Utils.Preferences.getSessionToken(), platform.getAccountId());
        spiceManager.execute(unSubscribeRequest, UnSubscribeListener);
    }

    private RequestListener<UnSubscribeResponse> UnSubscribeListener = new RequestListener<UnSubscribeResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            finishSubscribe();
            Utils.requestFailure("UnSubscribe");
        }

        @Override
        public void onRequestSuccess(UnSubscribeResponse response) {
            if (response == null) {
                finishSubscribe();
                Utils.requestFailure("UnSubscribe");
                return;
            }
            Utils.responseMessage(response.Message);

            finishSubscribe();
            if (response.Code == Constants.QUERY_SUCCESS) {
                subscribeButton.setBackgroundResource(R.drawable.icon_like_w);
                isSubscribed = false;
            }
            else if (response.Code == Constants.QUERY_FAILED)
                Utils.isTokenMessage(getActivity(), response);
        }
    };
}
