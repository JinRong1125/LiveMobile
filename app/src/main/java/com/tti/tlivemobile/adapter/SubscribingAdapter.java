package com.tti.tlivemobile.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.model.Platform;
import com.tti.tlivemobile.viewholder.SubscribingItemViewHolder;

import java.util.List;

/**
 * Created by dylan_liang on 2017/3/20.
 */

public class SubscribingAdapter extends RecyclerView.Adapter<SubscribingItemViewHolder> {

    private SubscribingItemViewHolder viewHolder;

    private Context context;

    private List<Platform> platformList;

    public AdapterListener onClickListener;

    public SubscribingAdapter(Context context, List<Platform> platformList, AdapterListener onClickListener) {
        this.context = context;
        this.platformList = platformList;
        this.onClickListener = onClickListener;
    }

    @Override
    public SubscribingItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subscribing_item, parent, false);
        viewHolder = new SubscribingItemViewHolder(view, onClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SubscribingItemViewHolder holder, int position) {
        if (platformList.get(position).getPlatformState().equals(AppConstants.STATE_ON))
            holder.liveImage.setVisibility(View.VISIBLE);

        holder.userName.setText(platformList.get(position).getUserName());
        holder.streamTitle.setText(platformList.get(position).getPlatformTitle());
        holder.categoryName.setText(platformList.get(position).getCategoryName());

        setAvatar(holder.avatarImage, holder.progressBar, platformList.get(position).getAvatarPath());
    }

    @Override
    public int getItemCount() {
        return platformList.size();
    }

    public interface AdapterListener {
        void onUnSubscribeClick(View v, int position);
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
