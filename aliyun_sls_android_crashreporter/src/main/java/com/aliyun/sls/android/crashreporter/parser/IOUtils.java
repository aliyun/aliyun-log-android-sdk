package com.aliyun.sls.android.crashreporter.parser;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author gordon
 * @date 2022/5/8
 */
final class IOUtils {
    private IOUtils() {
        //no instance
    }

    public static void closeSilently(Closeable closeable) {
        if (null == closeable) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
