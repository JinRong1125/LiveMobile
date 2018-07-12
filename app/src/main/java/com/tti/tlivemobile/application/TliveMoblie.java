package com.tti.tlivemobile.application;

import android.app.Application;
import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivemobile.constant.AppConstants;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by dylan_liang on 2017/6/15.
 */

public class TliveMoblie extends Application {

    private static Context context;
    private Socket socket;

    protected String userAgent;

    public void onCreate() {
        super.onCreate();
        context = this;
        userAgent = Util.getUserAgent(this, AppConstants.APP_NAME);
    }

    {
        try {
            socket = IO.socket(Constants.CHAT_SERVER_211);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Context getContext() {
        return context;
    }

    public Socket getSocket() {
        return socket;
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }
}
