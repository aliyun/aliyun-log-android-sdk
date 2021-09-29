package com.aliyun.sls.android.producer.example.example.trace.core;

import androidx.lifecycle.ViewModel;

import com.aliyun.sls.android.plugin.trace.SLSTelemetrySdk;
import com.aliyun.sls.android.plugin.trace.SLSTracePlugin;

import io.opentelemetry.api.trace.Tracer;

public class TraceViewModel extends ViewModel {
    protected SLSTelemetrySdk telemetrySdk = SLSTracePlugin.getInstance().getTelemetrySdk();
    protected Tracer tracer;

    public TraceViewModel(String modelName) {
        this.tracer = telemetrySdk.getTracer(modelName);
    }
}
