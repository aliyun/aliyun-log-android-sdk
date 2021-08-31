package com.aliyun.sls.android.plugin.trace;

import java.util.Map;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

/**
 * @author gordon
 * @date 2021/08/02
 */
public final class TraceTranslator {
    public static String attrsValueToString(Attributes attrs, String key) {
        if (null == attrs || null == attrs.asMap()) {
            return null;
        }

        if (null == key) {
            return null;
        }

        for (Map.Entry<AttributeKey<?>, Object> entry : attrs.asMap().entrySet()) {
            if (key.equals(entry.getKey().getKey())) {
                return entry.getValue().toString();
            }
        }

        return null;
    }
}
