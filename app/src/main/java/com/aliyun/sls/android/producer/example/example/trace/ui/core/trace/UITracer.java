package com.aliyun.sls.android.producer.example.example.trace.ui.core.trace;

import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.aliyun.sls.android.plugin.trace.SLSTracePlugin;

import java.util.Map;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

/**
 * @author gordon
 * @date 2021/10/18
 */
public final class UITracer {
    private static Tracer tracer = SLSTracePlugin.getInstance().getTelemetrySdk().getTracer("UITracer");

    private UITracer() {
        //no instance
    }


    public static <BIND extends ViewBinding, ITEM> void traceExpose(BIND bind, ITEM item, int pos) {
        bind.getRoot().post(() -> tracer.spanBuilder("expose_recycler_item")
                .setAttribute("position", pos)
                .setAttribute("view", getViewInfo(bind.getRoot()))
                .setAttribute("view_data", item.toString())
                .startSpan()
                .end());
    }

    public static void traceClick(String module, String event, Map<String, String> ext) {
        Span span = tracer.spanBuilder(String.format("click_%s_%s", module, event)).startSpan();
        if (null != ext) {
            for (Map.Entry<String, String> entry : ext.entrySet()) {
                span.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        span.end();
    }

    public static void tracePage() {

    }

    private static String getViewInfo(View view) {
        return view.toString();
    }
}
