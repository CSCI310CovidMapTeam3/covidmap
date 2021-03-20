package com.example.myapplication.adapter;

import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

public class OnBoardingPageAdapter extends RecyclerView.Adapter<OnBoardingPageAdapter.ViewPagerViewHolder> {

    private static final int[] PAGE_SRC = {R.drawable.on_boarding_1,
            R.drawable.on_boarding_2, R.drawable.on_boarding_3};

    @NonNull
    @Override
    public ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(layoutParams);
        return new ViewPagerViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerViewHolder holder, int position) {
        Drawable drawable = holder.mImageView.getResources().getDrawable(PAGE_SRC[position]);
        if (drawable != null)   //
            holder.mImageView.setBackground(drawable);
    }

    @Override
    public int getItemCount() {
        return PAGE_SRC.length;
    }

    public static class ViewPagerViewHolder extends RecyclerView.ViewHolder {

        ImageView mImageView;

        public ViewPagerViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            mImageView = itemView;
        }

    }
}
