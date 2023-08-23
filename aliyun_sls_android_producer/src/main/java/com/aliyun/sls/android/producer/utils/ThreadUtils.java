package com.aliyun.sls.android.producer.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author gordon
 * @date 2021/06/08
 */
public class ThreadUtils {
    private static final ExecutorService CACHED_THREAD_POOL = Executors.newCachedThreadPool();

    private ThreadUtils() {
        //no instance
    }

    public static void exec(Runnable r) {
        CACHED_THREAD_POOL.execute(r);
    }

    public static Executor cachedExecutors() {
        return CACHED_THREAD_POOL;
    }
}
