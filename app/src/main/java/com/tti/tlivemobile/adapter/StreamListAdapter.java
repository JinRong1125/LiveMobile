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
import com.tti.tlivemobile.viewholder.StreamItemViewHolder;

import java.util.List;

/**
 * Created by dylan_liang on 2017/3/20.
 */

public class StreamListAdapter extends RecyclerView.Adapter<StreamItemViewHolder> {

    private StreamItemViewHolder viewHolder;
    private Context context;
    private List<Platform> platformList;

    public StreamListAdapter(Context context, List<Platform> platformList) {
        this.context = context ;
        this.platformList = platformList;
    }

    @Override
    public StreamItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.streamlist_item, parent, false);
        viewHolder = new StreamItemViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(StreamItemViewHolder holder, int position) {
        if (platformList.get(position).getPlatformState().equals(AppConstants.STATE_ON))
            holder.liveImage.setVisibility(View.VISIBLE);

        holder.viewersValue.setText(String.valueOf(platformList.get(position).getViewersValue()));
        holder.streamTitle.setText(platformList.get(position).getPlatformTitle());
        holder.userName.setText(platformList.get(position).getUserName());
        holder.categoryName.setText(platformList.get(position).getCategoryName());

        setStreamImage(holder.streamImage, holder.progressBar, platformList.get(position).getPlatformImagePath());
    }

    @Override
    public int getItemCount() {
        return platformList.size();
    }

    private void setStreamImage(final ImageView imageView, final ProgressBar progressBar, final String photoPath) {
        Glide.with(context)
                .load(photoPath)
                .asBitmap()
                .error(R.drawable.stream_m)
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
