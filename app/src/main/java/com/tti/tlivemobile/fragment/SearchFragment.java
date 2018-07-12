package com.tti.tlivemobile.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.SearchPlatformRequest;
import com.tti.tlivelibrary.tliveservice.response.SearchPlatformResponse;
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
 * Created by dylan_liang on 2017/6/15.
 */

public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";

    private View view;
    private ProgressBar progressBar;
    private ImageButton searchButton, clearButton;
    private EditText searchText;
    private RecyclerView streamListView;
    private TextView nothingText;

    private StreamListAdapter streamListAdapter;
    private LinearLayoutManager layoutManager;

    private SpiceManager spiceManager;
    private SearchPlatformRequest searchPlatformRequest;

    private List<Platform> platformList;
    private Platform platform;

    public static Fragment newInstance() {
        return new SearchFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;

        initView();
    }

    private void initView() {
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        nothingText = (TextView) view.findViewById(R.id.nothing_text);

        searchText = (EditText) view.findViewById(R.id.search_text);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == EditorInfo.IME_ACTION_SEARCH) {
                    startSearch();
                    return true;
                }
                return false;
            }
        });

        clearButton = (ImageButton) view.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText.setText("");
            }
        });

        searchButton = (ImageButton) view.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch();
            }
        });

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        streamListView = (RecyclerView) view.findViewById(R.id.streamlist_view);
        streamListView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        streamListView.setLayoutManager(layoutManager);
        ItemClickSupport.addTo(streamListView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                setUserStream(position);
                streamListView.setEnabled(false);
            }
        });

        spiceManager = SpiceServiceManager.getInstance().getSpiceManager();
    }

    private void startSearch() {
        searchPlatform();
        Utils.hideKeyView(getActivity(), view);
        searchText.setEnabled(false);
        searchButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        nothingText.setVisibility(View.GONE);
    }

    private void finishSearch() {
        searchText.setEnabled(true);
        searchButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    private void setPlatformList(SearchPlatformResponse response) {
        for (int i = 0; i < response.platformList.size(); i++) {
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
        }
        setPlatform();
    }

    private void setPlatform() {
        streamListAdapter = new StreamListAdapter(getActivity(), platformList);
        streamListView.setAdapter(streamListAdapter);
    }

    private void setUserStream(int position) {
        platform = platformList.get(position);

        Intent intent = new Intent(getActivity(), WatchActivity.class);
        intent.putExtra(AppConstants.PLATFORM_ITEM, platform);
        startActivity(intent);
    }

    private void searchPlatform() {
        platformList = new ArrayList<>();
        streamListView.setAdapter(null);

        searchPlatformRequest = new SearchPlatformRequest(searchText.getText().toString());
        spiceManager.execute(searchPlatformRequest, SearchPlatformListener);
    }

    private RequestListener<SearchPlatformResponse> SearchPlatformListener = new RequestListener<SearchPlatformResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            finishSearch();
            Utils.requestFailure("SearchPlatform");
        }

        @Override
        public void onRequestSuccess(SearchPlatformResponse response) {
            if (response == null) {
                finishSearch();
                Utils.requestFailure("SearchPlatform");
                return;
            }

            finishSearch();
            if (response.Code == Constants.QUERY_SUCCESS) {
                if (response.platformList.size() > 0) {
                    setPlatformList(response);
                }
                else
                    nothingText.setVisibility(View.VISIBLE);
            }
        }
    };
}
