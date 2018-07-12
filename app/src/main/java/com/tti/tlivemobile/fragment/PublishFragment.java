package com.tti.tlivemobile.fragment;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.faucamp.simplertmp.RtmpHandler;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.ClosePlatformRequest;
import com.tti.tlivelibrary.tliveservice.response.ClosePlatformResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.activity.MainActivity;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.utils.Utils;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;
import net.skoumal.fragmentback.BackFragment;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

/**
 * Created by dylan_liang on 2017/6/16.
 */

public class PublishFragment extends Fragment implements RtmpHandler.RtmpListener,
        SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener, BackFragment {
    private static final String TAG = "PublishFragment";

    private View view;
    private ProgressBar progressBar;
    private LinearLayout optionLayout;
    private ImageButton finishButton, cameraButton;
    private TextView platformTitle;

    private SrsCameraView srsCameraView;
    private SrsPublisher srsPublisher;
    private OptionHandler optionHandler;

    private SpiceManager spiceManager;
    private ClosePlatformRequest closePlatformRequest;

    private String streamUrl;
    private int previewWidth;
    private int previewHeight;
    private int outputWidth;
    private int outputHeight;
    private int outputVerticalWidth;

    private boolean isPause;

    private static final int HD_HEIGHT = 720;
    private static final int VERTICAL_HEIGHT = 1024;

    private static final int OPTION_HIDE = 0;
    private static final int OPTION_TIMEOUT = 3000;

    public static Fragment newInstance() {
        return new PublishFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publish, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        isPause = false;

        initView();
        getBestCameraSize();
    }

    public void onResume() {
        super.onResume();
        if (isPause)
            startPublish();
        isPause = false;
    }

    public void onPause() {
        super.onPause();
        stopPublish();
        isPause = true;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        srsPublisher.stopEncode();
        srsPublisher.setScreenOrientation(newConfig.orientation);

        switch (newConfig.orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                srsPublisher.setOutputResolution(outputVerticalWidth, VERTICAL_HEIGHT);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_USER:
                srsPublisher.setOutputResolution(outputWidth, outputHeight);
                break;
        }

        srsPublisher.startEncode();
        srsPublisher.startCamera();
    }

    private void initView() {
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progress_bar);
        optionLayout = (LinearLayout) view.findViewById(R.id.option_layout);

        srsCameraView = (SrsCameraView) view.findViewById(R.id.camera_view);
        srsCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showOption();
                return false;
            }
        });

        finishButton = (ImageButton) view.findViewById(R.id.finish_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePlatform();
                stayShowOption();
                Utils.enableDisableView(view, false);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        cameraButton = (ImageButton) view.findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                srsPublisher.switchCameraFace((srsPublisher.getCamraId() + 1) % Camera.getNumberOfCameras());
            }
        });

        platformTitle = (TextView) view.findViewById(R.id.platform_title);
        platformTitle.setText(getActivity().getIntent().getStringExtra(AppConstants.STRING_ITEM));

        optionHandler = new OptionHandler();
        showOption();

        spiceManager = SpiceServiceManager.getInstance().getSpiceManager();
    }


    private void getBestCameraSize() {
        Camera camera = Camera.open();
        List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
        int bestWidth = 0;
        int bestHeight = 0;
        int compareWidth = 0;
        int compareHeight = 0;

        for (int i = 1; i < sizeList.size(); i++) {
            compareWidth = sizeList.get(i).width;
            compareHeight = sizeList.get(i).height;
            if (compareHeight <= HD_HEIGHT) {
                if ((bestWidth * bestHeight) < (compareWidth * compareHeight)) {
                    bestWidth = compareWidth;
                    bestHeight = compareHeight;
                }
            }
        }

        compareWidth = camera.getParameters().getPreviewSize().width;
        compareHeight = camera.getParameters().getPreviewSize().height;
        if (compareHeight <= HD_HEIGHT) {
            if ((bestWidth * bestHeight) < (compareWidth * compareHeight)) {
                bestWidth = compareWidth;
                bestHeight = compareHeight;
            }
        }

        camera.stopPreview();
        camera.release();

        previewWidth = bestWidth;
        previewHeight = bestHeight;
        outputWidth = bestWidth;
        outputHeight = bestHeight;
        outputVerticalWidth = bestHeight * VERTICAL_HEIGHT / bestWidth;

        setPublisher();
    }

    private void setPublisher() {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // UnChangeable
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR); //Changeable
        streamUrl = Constants.RTMP_PUBLISH_SERVER + String.valueOf(Utils.Preferences.getAccount().getAccountId());

        srsPublisher = new SrsPublisher((SrsCameraView) view.findViewById(R.id.camera_view));
        srsPublisher.setEncodeHandler(new SrsEncodeHandler(this));
        srsPublisher.setRtmpHandler(new RtmpHandler(this));
        srsPublisher.setRecordHandler(new SrsRecordHandler(this));
        srsPublisher.setPreviewResolution(previewWidth, previewHeight);
        srsPublisher.setOutputResolution(outputVerticalWidth, VERTICAL_HEIGHT);

        srsPublisher.switchToHardEncoder();
        srsPublisher.setVideoHDMode();

        startPublish();
    }

    private void startPublish() {
        srsPublisher.startCamera();
        srsPublisher.startPublish(streamUrl);
    }

    private void stopPublish() {
        srsPublisher.stopCamera();
        srsPublisher.stopPublish();
    }

    private void back() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void closePlatform() {
        closePlatformRequest = new ClosePlatformRequest(Utils.Preferences.getSessionToken());
        spiceManager.execute(closePlatformRequest, ClosePlatformListener);
    }

    RequestListener<ClosePlatformResponse> ClosePlatformListener = new RequestListener<ClosePlatformResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            back();
            Utils.requestFailure("ClosePlatform");
        }

        @Override
        public void onRequestSuccess(ClosePlatformResponse response) {
            if (response == null) {
                back();
                Utils.requestFailure("ClosePlatform");
                return;
            }
            Utils.responseMessage(response.Message);

            if (response.Code == Constants.QUERY_FAILED) {
                if (!Utils.isTokenMessage(getActivity(), response))
                    back();
            }
            else
                back();
        }
    };

    @Override
    public boolean onBackPressed() {
        closePlatform();
        stayShowOption();
        Utils.enableDisableView(view, false);
        progressBar.setVisibility(View.VISIBLE);
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

    private void handleException(Exception e) {
        try {
            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            srsPublisher.stopPublish();
            srsPublisher.stopRecord();
        } catch (Exception e1) {

        }
    }

    @Override
    public void onNetworkWeak() {
    }

    @Override
    public void onNetworkResume() {
    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {

    }

    @Override
    public void onRecordPause() {

    }

    @Override
    public void onRecordResume() {

    }

    @Override
    public void onRecordStarted(String msg) {
    }

    @Override
    public void onRecordFinished(String msg) {

    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {

    }

    @Override
    public void onRecordIOException(IOException e) {

    }

    @Override
    public void onRtmpConnecting(String msg) {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRtmpConnected(String msg) {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onRtmpVideoStreaming() {
    }

    @Override
    public void onRtmpAudioStreaming() {
    }

    @Override
    public void onRtmpStopped() {
    }

    @Override
    public void onRtmpDisconnected() {
    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {

    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {

    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {

    }

    @Override
    public void onRtmpSocketException(SocketException e) {
    }

    @Override
    public void onRtmpIOException(IOException e) {
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
    }
}
