package com.aliyun.sls.android.ot.context;

import java.io.IOException;

/**
 * @author gordon
 * @date 2022/9/8
 */
public enum NoopScope implements Scope {
    INSTANCE;

    @SuppressWarnings("RedundantThrows")
    @Override
    public void close() throws IOException {

    }
}
