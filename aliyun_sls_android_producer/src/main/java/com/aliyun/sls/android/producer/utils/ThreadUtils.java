package com.aliyun.sls.android.producer.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author gordon
 * @date 2021/06/08
 */
public class ThreadUtils {
    private static ExecutorService SINGLE_THREAD_EXECUTOR = Executors.newSingleThreadExecutor();

    private ThreadUtils() {
        //no instance
    }

    public static void exec(Runnable r) {
        SINGLE_THREAD_EXECUTOR.execute(r);
    }
}
