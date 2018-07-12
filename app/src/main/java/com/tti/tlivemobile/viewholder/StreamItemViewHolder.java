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

public class StreamItemViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout streamItem;
    public ImageView streamImage;
    public ProgressBar progressBar;
    public TextView liveImage;
    public TextView viewersValue;
    public TextView streamTitle;
    public TextView userName;
    public TextView categoryName;

    public StreamItemViewHolder(View itemView) {
        super(itemView);
        streamItem = (LinearLayout) itemView.findViewById(R.id.stream_item);
        streamImage = (ImageView) itemView.findViewById(R.id.stream_image);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        liveImage = (TextView) itemView.findViewById(R.id.live_image);
        viewersValue = (TextView) itemView.findViewById(R.id.viewers_value);
        streamTitle = (TextView) itemView.findViewById(R.id.stream_title);
        userName = (TextView) itemView.findViewById(R.id.user_name);
        categoryName = (TextView) itemView.findViewById(R.id.category_name);
    }
}
