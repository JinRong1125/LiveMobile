package com.tti.tlivemobile.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.tti.tlivemobile.R;
import com.tti.tlivemobile.model.Channel;
import com.tti.tlivemobile.viewholder.InfoChannelItemViewHolder;
import com.tti.tlivemobile.viewholder.IntroChannelItemViewHolder;

import java.util.List;

/**
 * Created by dylan_liang on 2017/3/20.
 */

public class IntroChannelAdapter extends RecyclerView.Adapter<IntroChannelItemViewHolder> {

    private IntroChannelItemViewHolder viewHolder;
    private Context context;
    private List<Channel> channelList;

    public AdapterListener onClickListener;

    public IntroChannelAdapter(Context context, List<Channel> channelList, AdapterListener onClickListener) {
        this.context = context ;
        this.channelList = channelList;
        this.onClickListener = onClickListener;
    }

    @Override
    public IntroChannelItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.intro_channel_item, parent, false);
        viewHolder = new IntroChannelItemViewHolder(view, onClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(IntroChannelItemViewHolder holder, int position) {
        holder.contentText.setText(channelList.get(position).getContentText());

        setStreamImage(holder.contentImage, holder.progressBar, channelList.get(position).getContentImagePath());
    }

    @Override
    public int getItemCount() {
        return channelList.size();
    }

    public interface AdapterListener {
        void onContentImageClick(View v, int position);

        void onEditChannelClick(View v, int position);

        void onDeleteChannelClick(View v, int position);
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
