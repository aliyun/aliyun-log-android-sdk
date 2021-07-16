package com.aliyun.sls.android.plugin.crashreporter.sender;

import android.text.TextUtils;

/**
 * @author gordon
 * @date 2021/04/19
 */
/*package*/ class Utils {
    private Utils() {
        //no instance
    }

    static String fillWithDashIfEmpty(String content) {
        return TextUtils.isEmpty(content) ? "-" : content;
    }
}
