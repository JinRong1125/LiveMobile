package com.tti.tlivemobile.model;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.QueryChannelByIdRequest;
import com.tti.tlivelibrary.tliveservice.response.QueryChannelByIdResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.adapter.InfoChannelAdapter;
import com.tti.tlivemobile.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dylan_liang on 2017/6/26.
 */

public class InformationContainer {

    private View view;
    private ProgressBar imageProgress, contentProgress;
    private ImageButton closeButton;
    private ImageView platformImage;
    private TextView userNameView, nothingText;
    private RecyclerView informationView;

    private InfoChannelAdapter channelAdapter;
    private LinearLayoutManager layoutManager;

    private SpiceManager spiceManager;

    private Activity activity;

    private int accountId;
    private String userName;
    private String platformImagePath;

    private List<Channel> channelList;

    public InformationContainer(
            Activity activity, int accountId, String userName, String platformImagePath, SpiceManager spiceManager) {
        this.activity = activity;
        this.accountId = accountId;
        this.userName = userName;
        this.platformImagePath = platformImagePath;
        this.spiceManager = spiceManager;

        LayoutInflater inflater = LayoutInflater.from(activity);
        view = inflater.inflate(R.layout.information_container, null, false);

        initView();
        queryChannelById();
    }

    private void initView() {
        imageProgress = (ProgressBar) view.findViewById(R.id.image_progress);
        contentProgress = (ProgressBar) view.findViewById(R.id.content_progress);

        closeButton = (ImageButton) view.findViewById(R.id.close_button);

        platformImage = (ImageView) view.findViewById(R.id.platform_image);
        setPlatformImage();

        userNameView = (TextView) view.findViewById(R.id.user_name);
        userNameView.setText(userName);

        layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        informationView = (RecyclerView) view.findViewById(R.id.information_view);
        informationView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        informationView.setLayoutManager(layoutManager);

        nothingText = (TextView) view.findViewById(R.id.nothing_text);
        nothingText.setVisibility(View.GONE);
    }

    private void setPlatformImage() {
        Glide.with(activity)
                .load(platformImagePath)
                .asBitmap()
                .error(R.drawable.stream_l)
                .listener(new com.bumptech.glide.request.RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        imageProgress.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        imageProgress.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(platformImage);
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
        channelAdapter = new InfoChannelAdapter(activity, channelList);
        informationView.setAdapter(channelAdapter);
    }

    public View getInfoView() {
        return view;
    }

    private void queryChannelById() {
        channelList = new ArrayList<>();

        QueryChannelByIdRequest request = new QueryChannelByIdRequest(
                Utils.Preferences.getSessionToken(), accountId);
        spiceManager.execute(request, QueryChannelByIdListener);
    }

    RequestListener<QueryChannelByIdResponse> QueryChannelByIdListener = new RequestListener<QueryChannelByIdResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            contentProgress.setVisibility(View.GONE);
            Utils.requestFailure("QueryChannelById");
        }

        @Override
        public void onRequestSuccess(QueryChannelByIdResponse response) {
            if (response == null) {
                contentProgress.setVisibility(View.GONE);
                Utils.requestFailure("QueryChannelById");
                return;
            }

            contentProgress.setVisibility(View.GONE);
            if (response.Code == Constants.QUERY_SUCCESS) {
                if (response.channelList.size() > 0)
                    setChannelList(response);
                else
                    nothingText.setVisibility(View.VISIBLE);
            }
            else if (response.Code == Constants.QUERY_FAILED) {
                if (!Utils.isTokenMessage(activity, response))
                    nothingText.setVisibility(View.VISIBLE);
                Utils.responseMessage(response.Message);
            }
            else
                nothingText.setVisibility(View.VISIBLE);
        }
    };
}
