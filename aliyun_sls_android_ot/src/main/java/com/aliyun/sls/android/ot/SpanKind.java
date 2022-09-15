package com.aliyun.sls.android.ot;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

/**
 * @author gordon
 * @date 2022/3/31
 */
public enum SpanKind {
    INTERNAL("INTERNAL"),
    SERVER("SERVER"),
    CLIENT("CLIENT"),
    PRODUCER("PRODUCER"),
    CONSUMER("CONSUMER");

    private static final Map<String, SpanKind> sSpanKindMap = new HashMap<String, SpanKind>() {
        {
            put(INTERNAL.kind, INTERNAL);
            put(SERVER.kind, SERVER);
            put(CLIENT.kind, CLIENT);
            put(PRODUCER.kind, PRODUCER);
            put(CONSUMER.kind, CONSUMER);
        }
    };

    public final String kind;
    SpanKind(String kind) {
        this.kind = kind;
    }

    @SuppressWarnings("unused")
    public static SpanKind kindOf(String kind) {
        if (TextUtils.isEmpty(kind)) {
            return INTERNAL;
        }

        if (!sSpanKindMap.containsKey(kind.toUpperCase())) {
            return INTERNAL;
        }

        return sSpanKindMap.get(kind.toUpperCase());
    }
}
