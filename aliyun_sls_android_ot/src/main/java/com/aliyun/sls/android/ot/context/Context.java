package com.aliyun.sls.android.ot.context;

import com.aliyun.sls.android.ot.Span;

/**
 * @author gordon
 * @date 2022/9/7
 */
public interface Context {

    Context current();

    Span activeSpan();

    Scope makeCurrent(Span span);

}
