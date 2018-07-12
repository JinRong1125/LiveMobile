package com.tti.tlivemobile.model;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.*;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.opendanmaku.DanmakuView;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.QueryPlatformByAccountRequest;
import com.tti.tlivelibrary.tliveservice.response.QueryPlatformResponse;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.application.TliveMoblie;
import com.tti.tlivemobile.utils.Utils;

/**
 * Created by dylan_liang on 2017/6/20.
 */

public class ExoPlayerContainer implements AudioManager.OnAudioFocusChangeListener {

    private Activity activity;

    private View view;
    private FrameLayout playerLayout, imageLayout;
    private ProgressBar progressBar;
    private TextView streamTitle;

    private AudioManager audioManager;

    private SimpleExoPlayerView playerView;
    private SimpleExoPlayer player;
    private DataSource.Factory mediaDataSourceFactory;

    private Platform platform;
    private MediaSource mediaSource;

    private StreamHandler streamHandler;

    private SpiceManager spiceManager;
    private QueryPlatformByAccountRequest queryPlatformByAccountRequest;

    private static final int TRY_LOAD = 0;
    private static final int RELOAD_TIMEOUT = 10000;

    private static final float LOWER_VOLUME = 0.25f;
    private static final float USER_VOLUME = 1.0f;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    public void initPlayerView(
            Activity activity,
            Platform platform,
            FrameLayout playerLayout,
            FrameLayout imageLayout,
            ProgressBar progressBar,
            TextView streamTitle,
            SpiceManager spiceManager) {
        this.activity = activity;
        this.platform = platform;
        this.imageLayout = imageLayout;
        this.playerLayout = playerLayout;
        this.streamTitle = streamTitle;
        this.progressBar = progressBar;
        this.spiceManager = spiceManager;

        LayoutInflater inflater = LayoutInflater.from(activity);
        view = inflater.inflate(R.layout.exoplayer_container, null, false);

        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        streamHandler = new StreamHandler();

        createPlayer();
    }

    private void createPlayer() {
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(true, C.DEFAULT_VIDEO_BUFFER_SIZE));
        player = ExoPlayerFactory.newSimpleInstance(activity, trackSelector, loadControl);
        player.addListener(new EventListener());

        playerView = (SimpleExoPlayerView) view.findViewById(R.id.player_view);
        playerView.setPlayer(player);
        playerView.setUseController(false);
        mediaSource = buildMediaSource(Uri.parse(platform.getStreamUrl()));

        requestFocus();
    }

    private void playStream() {
        player.setPlayWhenReady(true);
        player.prepare(mediaSource, true, true);
    }

    public void releasePlayer() {
        if (streamHandler != null)
            streamHandler.removeMessages(TRY_LOAD);
        if (player != null) {
            player.stop();
            player.release();
            player = null;
            playerView = null;
        }
    }

    public View getPlayerView() {
        return view;
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    public void requestFocus() {
        int result = audioManager.requestAudioFocus(
                this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            playStream();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch(focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (player != null)
                    player.setVolume(USER_VOLUME);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (player != null)
                    player.setPlayWhenReady(false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (player != null)
                    player.setPlayWhenReady(false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (player != null)
                    player.setVolume(LOWER_VOLUME);
                break;
        }
    }

    private class EventListener implements ExoPlayer.EventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    progressBar.setVisibility(View.GONE);
                    playerLayout.setVisibility(View.GONE);
                    imageLayout.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_READY:
                    progressBar.setVisibility(View.GONE);
                    playerLayout.setVisibility(View.VISIBLE);
                    imageLayout.setVisibility(View.GONE);
                    streamHandler.removeMessages(TRY_LOAD);
                    queryPlatformByAccount();
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    if (imageLayout.getVisibility() != View.VISIBLE)
                        progressBar.setVisibility(View.VISIBLE);
                    streamHandler.sendMessageDelayed(streamHandler.obtainMessage(TRY_LOAD), RELOAD_TIMEOUT);
                    break;
                case ExoPlayer.STATE_ENDED:
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            streamHandler.sendMessageDelayed(streamHandler.obtainMessage(TRY_LOAD), RELOAD_TIMEOUT);
        }

        @Override
        public void onPositionDiscontinuity() {

        }
    }

    private class StreamHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRY_LOAD:
                    player.prepare(mediaSource, true, true);
                    break;
            }
        }
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return ((TliveMoblie) activity.getApplication())
                .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private MediaSource buildMediaSource(Uri uri) {
        mediaDataSourceFactory = buildDataSourceFactory(true);
        int type = Util.inferContentType(uri.getLastPathSegment());

        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), null, null);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), null, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, null, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory,
                        new DefaultExtractorsFactory(), null, null);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private void queryPlatformByAccount() {
        queryPlatformByAccountRequest = new QueryPlatformByAccountRequest(platform.getAccountId());
        spiceManager.execute(queryPlatformByAccountRequest, QueryPlatformListener);
    }

    private RequestListener<QueryPlatformResponse> QueryPlatformListener = new RequestListener<QueryPlatformResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Utils.requestFailure("QueryPlatform");
        }

        @Override
        public void onRequestSuccess(QueryPlatformResponse response) {
            if (response == null) {
                Utils.requestFailure("QueryPlatform");
                return;
            }

            if (response.Code == Constants.QUERY_SUCCESS) {
                if (response.platformList.size() > 0)
                    streamTitle.setText(response.platformList.get(0).platformTitle);
            }
        }
    };
}
