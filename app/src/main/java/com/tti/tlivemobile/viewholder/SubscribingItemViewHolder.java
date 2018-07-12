package com.tti.tlivemobile.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tti.tlivemobile.R;
import com.tti.tlivemobile.adapter.SubscribingAdapter;

/**
 * Created by dylan_liang on 2017/3/20.
 */

public class SubscribingItemViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout subscribingItem;
    public ImageView avatarImage;
    public ProgressBar progressBar;
    public TextView liveImage;
    public TextView userName;
    public TextView streamTitle;
    public TextView categoryName;
    public ImageButton unSubscribeButton;

    public SubscribingItemViewHolder(final View itemView,
                                     final SubscribingAdapter.AdapterListener onClickListener) {
        super(itemView);
        subscribingItem = (LinearLayout) itemView.findViewById(R.id.subscribing_item);
        avatarImage = (ImageView) itemView.findViewById(R.id.avatar_image);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        liveImage = (TextView) itemView.findViewById(R.id.live_image);
        userName = (TextView) itemView.findViewById(R.id.user_name);
        streamTitle = (TextView) itemView.findViewById(R.id.stream_title);
        categoryName = (TextView) itemView.findViewById(R.id.category_name);
        unSubscribeButton = (ImageButton) itemView.findViewById(R.id.un_subscribe_button);
        unSubscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onUnSubscribeClick(itemView, getAdapterPosition());
            }
        });
    }
}
