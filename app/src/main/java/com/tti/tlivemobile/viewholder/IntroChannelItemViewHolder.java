package com.tti.tlivemobile.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tti.tlivemobile.R;
import com.tti.tlivemobile.adapter.IntroChannelAdapter;

/**
 * Created by dylan_liang on 2017/3/20.
 */

public class IntroChannelItemViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout channelItem;
    public ImageView contentImage;
    public ProgressBar progressBar;
    public TextView contentText;
    public ImageButton editButton;
    public ImageButton deleteButton;

    public IntroChannelItemViewHolder(final View itemView,
                                      final IntroChannelAdapter.AdapterListener onClickListener) {
        super(itemView);
        channelItem = (LinearLayout) itemView.findViewById(R.id.channel_item);
        contentImage = (ImageView) itemView.findViewById(R.id.content_image);
        contentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onContentImageClick(itemView, getAdapterPosition());
            }
        });
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        contentText = (TextView) itemView.findViewById(R.id.content_text);
        editButton = (ImageButton) itemView.findViewById(R.id.edit_Button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onEditChannelClick(itemView, getAdapterPosition());
            }
        });
        deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onDeleteChannelClick(itemView, getAdapterPosition());
            }
        });
    }
}
