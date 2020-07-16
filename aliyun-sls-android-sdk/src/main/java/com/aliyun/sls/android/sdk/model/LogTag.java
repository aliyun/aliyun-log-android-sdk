package com.aliyun.sls.android.sdk.model;

import java.util.HashMap;
import java.util.Map;

public class LogTag {
    Map<String, Object> mContent = new HashMap<String, Object>();



    public void PutContent(String key, String value) {
        if (key == null || key.isEmpty()) {
            return;
        }
        if (value == null) {
            mContent.put(key, "");
        } else {
            mContent.put(key, value);
        }
    }

    public Map<String, Object> GetContent() {
        return mContent;
    }
}