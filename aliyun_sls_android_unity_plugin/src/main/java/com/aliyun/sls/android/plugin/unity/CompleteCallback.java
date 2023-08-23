package com.aliyun.sls.android.plugin.unity;

/**
 * @author gordon
 * @date 2023/4/27
 */
public interface CompleteCallback {
    void onComplete(int type, String content, String context, String error);
}
