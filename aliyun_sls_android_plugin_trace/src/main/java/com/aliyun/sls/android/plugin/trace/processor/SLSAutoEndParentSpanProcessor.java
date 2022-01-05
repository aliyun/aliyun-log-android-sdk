package com.aliyun.sls.android.plugin.trace.processor;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * @author gordon
 * @date 2021/08/11
 */
public class SLSAutoEndParentSpanProcessor implements SpanProcessor {
    private Map<ReadableSpan, Context> contextCacheList;

    public SLSAutoEndParentSpanProcessor() {
        contextCacheList = new HashMap<>();
    }


    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        // connect crash reporter
        if (TextUtils.equals("crash_reporter", span.getName())) {
            if (contextCacheList.get(span) == null) {
                contextCacheList.put(span, parentContext);
            }
        }
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
        Context parentContext = contextCacheList.get(span);
        if (null != parentContext) {
            Span parentSpan = Span.fromContext(parentContext);
            if (null != parentSpan) {
                // Auto end parent span and make spans will be in the special trace.
                // The only scene is crash reporter current now.
                parentSpan.end();
            }
            contextCacheList.remove(span);
        }
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }
}
