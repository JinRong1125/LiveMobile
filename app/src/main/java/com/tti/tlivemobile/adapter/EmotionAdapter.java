package com.tti.tlivemobile.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tti.tlivemobile.model.Emotion;

import java.util.List;

/**
 * Created by dylan_liang on 2017/3/17.
 */

public class EmotionAdapter extends BaseAdapter {

    private Context context;

    private List<Emotion> emotionList;

    public EmotionAdapter(Context context, List<Emotion> emotionList) {
        this.context = context;
        this.emotionList = emotionList;
    }

    @Override
    public int getCount() {
        return emotionList.size();
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView = new ImageView(context);

        Glide.with(context).load(emotionList.get(i).getEmotionPath())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).fitCenter().into(imageView);

        return imageView;
    }

}
