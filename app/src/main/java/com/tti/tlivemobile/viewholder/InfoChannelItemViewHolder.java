package com.tti.tlivemobile.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tti.tlivemobile.R;

/**
 * Created by dylan_liang on 2017/3/20.
 */

public class InfoChannelItemViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout channelItem;
    public ImageView contentImage;
    public ProgressBar progressBar;
    public TextView contentText;

    public InfoChannelItemViewHolder(View itemView) {
        super(itemView);
        channelItem = (LinearLayout) itemView.findViewById(R.id.channel_item);
        contentImage = (ImageView) itemView.findViewById(R.id.content_image);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        contentText = (TextView) itemView.findViewById(R.id.content_text);
    }
}
