package com.aliyun.sls;

import android.app.Application;
import com.aliyun.sls.android.sdk.SLSDatabaseManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SLSDatabaseManager.getInstance().setupDB(getApplicationContext());
    }
}
