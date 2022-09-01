package com.aliyun.sls.android.producer.example.example.trace.utils;

import android.widget.ImageView;
import com.aliyun.sls.android.producer.example.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

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
//                .optionalCenterCrop()
//                .dontAnimate()
//                .apply(RequestOptions.bitmapTransform(new RoundedCorners(50)))
                .placeholder(R.mipmap.ic_launcher)
                .transform(new CenterCrop(), new RoundedCorners(16))
                .into(imageView);
    }
}
