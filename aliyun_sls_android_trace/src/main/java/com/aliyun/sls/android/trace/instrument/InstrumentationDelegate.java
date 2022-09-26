package com.aliyun.sls.android.trace.instrument;

/**
 * @author gordon
 * @date 2022/9/15
 */
public interface InstrumentationDelegate<T> {
    boolean shouldInstrument(T t);
}
