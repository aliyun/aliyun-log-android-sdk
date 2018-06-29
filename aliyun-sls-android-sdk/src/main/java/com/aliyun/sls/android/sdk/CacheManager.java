package com.aliyun.sls.android.sdk;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.net.NetworkInfo;
import android.util.Log;

import com.aliyun.sls.android.sdk.core.AsyncTask;
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.request.PostCachedLogRequest;
import com.aliyun.sls.android.sdk.result.PostCachedLogResult;

import android.net.ConnectivityManager;

public class CacheManager {

    private final static String TAG = "CacheManager";

    private Timer mTimer;
    private LOGClient mClient;

    public CacheManager(LOGClient mClient) {
        this.mClient = mClient;
    }

    public void setupTimer() {
        // 初始化定时器
        mTimer = new Timer();
        TimerTask timerTask = new CacheTimerTask(this);
        mTimer.schedule(timerTask, 30000, 30000);
    }

    private static class CacheTimerTask extends TimerTask {

        private WeakReference<CacheManager> mWeakCacheManager;

        public CacheTimerTask(CacheManager manager){
            mWeakCacheManager = new WeakReference<>(manager);
        }

        @Override
        public void run() {
            if(mWeakCacheManager.get() == null){
                return;
            }else{
                ConnectivityManager cm =
                        (ConnectivityManager)mWeakCacheManager.get().mClient.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if (!isConnected) {
                    return;
                }

                // 有网络连接的情况下如何决定是否上传日志(1.WIFI_ONLY只在wifi环境上传; 2.有网就传)
                Boolean shouldPost = false;
                if (mWeakCacheManager.get().mClient.getPolicy() == ClientConfiguration.NetworkPolicy.WIFI_ONLY && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    shouldPost = true;
                } else if (mWeakCacheManager.get().mClient.getPolicy() == ClientConfiguration.NetworkPolicy.WWAN_OR_WIFI) {
                    shouldPost = true;
                }

                if (shouldPost) {
                    mWeakCacheManager.get().fetchDataFromDBAndPost();
                }
            }

        }
    }

    // 停止定时器
    public void stopTimer() {
        if(mTimer != null) {
            mTimer.cancel();
            // 一定设置为null，否则定时器不会被回收
            mTimer = null;
        }
    }

    private void fetchDataFromDBAndPost() {
        List<LogEntity> list = SLSDatabaseManager.getInstance().queryRecordFromDB();
        for (final LogEntity item : list) {
            String clientEndPoint = this.mClient.GetEndPoint();
            if (clientEndPoint.equals(item.getEndPoint())) {
                try {
                    PostCachedLogRequest request = new PostCachedLogRequest(item.getProject(), item.getStore(), item.getJsonString());
                    AsyncTask<PostCachedLogResult> result = this.mClient.asyncPostCachedLog(request, new CompletedCallback<PostCachedLogRequest, PostCachedLogResult>() {
                        @Override
                        public void onSuccess(PostCachedLogRequest request, PostCachedLogResult result) {
                            SLSDatabaseManager.getInstance().deleteRecordFromDB(item);
                        }

                        @Override
                        public void onFailure(PostCachedLogRequest request, LogException exception) {
                            SLSLog.logError("send cached log failed");
                        }
                    });
                } catch (LogException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stopTimer();
        Log.d(TAG,"CacheManager finalize");
    }
}
