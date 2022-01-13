package com.aliyun.sls.android.plugin.trace.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * @author gordon
 * @date 2021/08/11
 */
public class SLSSpanProcessor implements SpanProcessor {
    private final List<SpanProcessor> spanProcessorsStart;
    private final List<SpanProcessor> spanProcessorsEnd;
    private final List<SpanProcessor> spanProcessorsAll;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public static SLSSpanProcessor create(List<SpanProcessor> spanProcessorList) {
        return new SLSSpanProcessor(spanProcessorList);
    }

    private SLSSpanProcessor(List<SpanProcessor> spanProcessorList) {
        this.spanProcessorsAll = spanProcessorList;
        this.spanProcessorsStart = new ArrayList<>(spanProcessorsAll.size());
        this.spanProcessorsEnd = new ArrayList<>(spanProcessorsAll.size());
        for (SpanProcessor spanProcessor : spanProcessorsAll) {
            if (spanProcessor.isStartRequired()) {
                spanProcessorsStart.add(spanProcessor);
            }
            if (spanProcessor.isEndRequired()) {
                spanProcessorsEnd.add(spanProcessor);
            }
        }
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan readableSpan) {
        for (SpanProcessor spanProcessor : spanProcessorsStart) {
            spanProcessor.onStart(parentContext, readableSpan);
        }
    }

    @Override
    public boolean isStartRequired() {
        return !spanProcessorsStart.isEmpty();
    }

    @Override
    public void onEnd(ReadableSpan readableSpan) {
        for (SpanProcessor spanProcessor : spanProcessorsEnd) {
            spanProcessor.onEnd(readableSpan);
        }
    }

    @Override
    public boolean isEndRequired() {
        return !spanProcessorsEnd.isEmpty();
    }

    @Override
    public CompletableResultCode shutdown() {
        if (isShutdown.getAndSet(true)) {
            return CompletableResultCode.ofSuccess();
        }
        List<CompletableResultCode> results = new ArrayList<>(spanProcessorsAll.size());
        for (SpanProcessor spanProcessor : spanProcessorsAll) {
            results.add(spanProcessor.shutdown());
        }
        return CompletableResultCode.ofAll(results);
    }

    @Override
    public CompletableResultCode forceFlush() {
        List<CompletableResultCode> results = new ArrayList<>(spanProcessorsAll.size());
        for (SpanProcessor spanProcessor : spanProcessorsAll) {
            results.add(spanProcessor.forceFlush());
        }
        return CompletableResultCode.ofAll(results);
    }
}
