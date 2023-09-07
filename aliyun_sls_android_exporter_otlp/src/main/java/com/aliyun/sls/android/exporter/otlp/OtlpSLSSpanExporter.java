package com.aliyun.sls.android.exporter.otlp;

import java.util.Collection;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * @author yulong.gyl
 * @date 2023/9/7
 */
public class OtlpSLSSpanExporter implements SpanExporter {

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        return null;
    }

    @Override
    public CompletableResultCode flush() {
        return null;
    }

    @Override
    public CompletableResultCode shutdown() {
        return null;
    }
}
