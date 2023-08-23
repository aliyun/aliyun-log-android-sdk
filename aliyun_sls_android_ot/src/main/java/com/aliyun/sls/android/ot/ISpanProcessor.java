package com.aliyun.sls.android.ot;

/**
 * @author gordon
 * @date 2022/4/13
 */
public interface ISpanProcessor {
    boolean onEnd(Span span);
}
