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
import com.tti.tlivemobile.model.Category;

import java.util.List;

/**
 * Created by dylan_liang on 2017/3/20.
 */

public class CategoryAdapter extends BaseAdapter {

    private Context context;
    private List<Category> categoryList;

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @Override
    public int getCount() {
        return categoryList.size();
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
            view = inflater.inflate(R.layout.category_item, viewGroup, false);
        }

        TextView userName = (TextView) view.findViewById(R.id.category_name);
        userName.setText(categoryList.get(position).getCategoryName());

        ImageView categoryImage = (ImageView) view.findViewById(R.id.category_image);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        setCategoryImage(categoryImage, progressBar, categoryList.get(position).getCategoryImagePath());

        return view;
    }

    private void setCategoryImage(final ImageView imageView, final ProgressBar progressBar, final String avatarPath) {
        Glide.with(context)
                .load(avatarPath)
                .asBitmap()
                .error(R.drawable.tti_logo)
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
