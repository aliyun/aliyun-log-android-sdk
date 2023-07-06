package com.aliyun.sls.android.webview.instrumentation.instrumentation;

import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;

/**
 * @author yulong.gyl
 * @date 2023/7/4
 */
public interface IWebRequestInstrumentation {

    /**
     * Called after WebRequest started.
     *
     * @param info {@link WebRequestInfo}
     */
    void createdRequest(WebRequestInfo info);

    /**
     * Called after WebRequest ended.
     *
     * @param info {@link WebRequestInfo}
     */
    void receivedResponse(WebRequestInfo info);

}
