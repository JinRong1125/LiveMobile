package com.tti.tlivemobile.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.model.Platform;

import java.util.List;

/**
 * Created by dylan_liang on 2017/3/20.
 */

public class SubscribersAdapter extends BaseAdapter {

    private Context context;
    private List<Platform> platformList;

    public SubscribersAdapter(Context context, List<Platform> platformList) {
        this.context = context;
        this.platformList = platformList;
    }

    @Override
    public int getCount() {
        return platformList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.subscribers_item, viewGroup, false);
        }

        TextView userName = (TextView) view.findViewById(R.id.user_name);
        userName.setText(platformList.get(position).getUserName());

        ImageView avatarImage = (ImageView) view.findViewById(R.id.avatar_image);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        setAvatar(avatarImage, progressBar, platformList.get(position).getAvatarPath());

        return view;
    }

    private void setAvatar(final ImageView imageView, final ProgressBar progressBar, final String avatarPath) {
        Glide.with(context)
                .load(avatarPath)
                .asBitmap()
                .error(R.drawable.user_m)
                .listener(new com.bumptech.glide.request.RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);
    }
}
