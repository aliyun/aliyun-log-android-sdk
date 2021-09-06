package com.aliyun.sls.android.producer.example.example.trace.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * @author gordon
 * @date 2021/09/06
 */
public class ImageUtils {
    private ImageUtils() {
        //no instance
    }

    public static void loadImage(String url, ImageView imageView) {
        Glide.with(imageView)
                .load(url)
                .optionalCenterCrop()
                .dontAnimate()
                .into(imageView);
    }
}
