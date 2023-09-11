package com.aliyun.sls.android.exporter.otlp;

import java.util.Map;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

/**
 * @author gordon
 * @date 2021/08/02
 */
final class TraceTranslator {

    /**
     * Export {@link Attributes} value as string.
     *
     * @param attrs {@link Attributes}
     * @param key   key for value.
     * @return string value.
     */
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
