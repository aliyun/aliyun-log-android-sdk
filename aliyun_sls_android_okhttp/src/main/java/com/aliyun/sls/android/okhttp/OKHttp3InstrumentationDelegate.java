package com.aliyun.sls.android.okhttp;

import com.aliyun.sls.android.trace.instrument.HttpInstrumentationDelegate;
import okhttp3.Request;

/**
 * @author gordon
 * @date 2022/9/15
 */
public interface OKHttp3InstrumentationDelegate extends HttpInstrumentationDelegate<Request> {
}
