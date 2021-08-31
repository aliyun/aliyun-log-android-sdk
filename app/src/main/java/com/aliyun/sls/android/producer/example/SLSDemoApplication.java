package com.aliyun.sls.android.producer.example;

import android.app.Application;

import com.aliyun.sls.android.producer.example.utils.PreferenceUtils;

/**
 * @author gordon
 * @date 2021/08/31
 */
public class SLSDemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.CONFIG_ENABLE) {
            PreferenceUtils.overrideConfig(this);
        }
    }
}
