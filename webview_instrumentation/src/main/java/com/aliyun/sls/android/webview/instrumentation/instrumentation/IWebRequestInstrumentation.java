package com.aliyun.sls.android.webview.instrumentation.instrumentation;

import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;

/**
 * @author yulong.gyl
 * @date 2023/7/4
 */
public interface IWebRequestInstrumentation {

    void requestStarted(WebRequestInfo info);

    void requestHeadersUpdated(WebRequestInfo info);

    void requestMimeTypeUpdated(WebRequestInfo info);

    void requestBodyUpdated(WebRequestInfo info);

    void responseReturned(WebRequestInfo info);

}
