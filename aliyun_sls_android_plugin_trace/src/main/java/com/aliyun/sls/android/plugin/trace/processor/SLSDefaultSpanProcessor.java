package com.aliyun.sls.android.plugin.trace.processor;

import android.os.Build;
import android.os.Environment;
import android.system.Os;

import com.aliyun.sls.android.utdid.Utdid;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * @author gordon
 * @date 2021/08/11
 */
public class SLSDefaultSpanProcessor implements SpanProcessor {

    private android.content.Context context;

    public SLSDefaultSpanProcessor(android.content.Context context) {
        this.context = context;

    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        // device specification, ref: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/device.md
        span.setAttribute("device.id", Utdid.getInstance().getUtdid(context));
        span.setAttribute("device.model.identifier", Build.MODEL);
        span.setAttribute("device.model.name", Build.PRODUCT);

        // os specification, ref: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/os.md
        span.setAttribute("os.type", "Android");
//        span.setAttribute("os.description", Build.DEVICE);
        span.setAttribute("os.name", "Android");
        span.setAttribute("os.version", Build.VERSION.SDK_INT);

        // host specification, ref: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/host.md
        span.setAttribute("host.id", Utdid.getInstance().getUtdid(context));
        span.setAttribute("host.name", Build.HOST);
        span.setAttribute("host.type", Build.TYPE);
        span.setAttribute("host.arch", Build.CPU_ABI);
//        span.setAttribute("host.image.name", Build.VERSION.SDK_INT);
//        span.setAttribute("host.image.id", Build.VERSION.SDK_INT);
//        span.setAttribute("host.image.version", Build.VERSION.SDK_INT);
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {

    }

    @Override
    public boolean isEndRequired() {
        return false;
    }
}
