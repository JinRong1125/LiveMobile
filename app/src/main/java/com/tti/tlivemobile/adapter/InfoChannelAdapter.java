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
import com.tti.tlivemobile.model.Channel;
import com.tti.tlivemobile.viewholder.InfoChannelItemViewHolder;

import java.util.List;

/**
 * Created by dylan_liang on 2017/3/20.
 */

public class InfoChannelAdapter extends RecyclerView.Adapter<InfoChannelItemViewHolder> {

    private InfoChannelItemViewHolder viewHolder;
    private Context context;
    private List<Channel> channelList;

    public InfoChannelAdapter(Context context, List<Channel> channelList) {
        this.context = context ;
        this.channelList = channelList;
    }

    @Override
    public InfoChannelItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_channel_item, parent, false);
        viewHolder = new InfoChannelItemViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(InfoChannelItemViewHolder holder, int position) {
        holder.contentText.setText(channelList.get(position).getContentText());

        setStreamImage(holder.contentImage, holder.progressBar, channelList.get(position).getContentImagePath());
    }

    @Override
    public int getItemCount() {
        return channelList.size();
    }

    private void setStreamImage(final ImageView imageView, final ProgressBar progressBar, final String photoPath) {
        Glide.with(context)
                .load(photoPath)
                .asBitmap()
                .error(R.drawable.stream_l)
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
