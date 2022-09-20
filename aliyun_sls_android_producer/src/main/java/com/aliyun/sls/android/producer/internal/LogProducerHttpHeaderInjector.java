package com.aliyun.sls.android.producer.internal;

/**
 * @author gordon
 * @date 2022/9/19
 */
public interface LogProducerHttpHeaderInjector {
    String[] injectHeaders(String[] srcHeaders, int count);
}
