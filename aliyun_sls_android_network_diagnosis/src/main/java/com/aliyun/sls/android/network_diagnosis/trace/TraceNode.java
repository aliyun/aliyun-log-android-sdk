package com.aliyun.sls.android.network_diagnosis.trace;

import com.alibaba.netspeed.network.DetectConfig;
import com.alibaba.netspeed.network.FlowNode;

import android.content.Context;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.utdid.Utdid;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.Request;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.TcpPingRequest;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.trace.ReadableSpan;

/**
 * @author yulong.gyl
 * @date 2023/11/22
 */
public class TraceNode {
    private static final String TAG = "TraceNode";
    private OpenTelemetry openTelemetrySdk;
    private String type;
    private Request request;
    private Context context;
    private Span span;

    public TraceNode(OpenTelemetry openTelemetrySdk, String type, Request request, Context context) {
        this.openTelemetrySdk = openTelemetrySdk;
        this.type = type;
        this.request = request;
        this.context = context;

        start();
    }

    private void start() {
        if (null == openTelemetrySdk) {
            SLSLog.e(TAG, "You should call setOpenTelemetry() method first.");
            return;
        }

        span = openTelemetrySdk.getTracer("network_diagnosis")
            .spanBuilder(type)
            .setParent(io.opentelemetry.context.Context.current())
            .startSpan();

        span.setAttribute("detection.type", type);
        span.setAttribute("detection.domain", request.domain);
        if ("tcpping".equalsIgnoreCase(type)) {
            span.setAttribute("detection.port", ((TcpPingRequest)request).port);
            span.setAttribute("detection.maxTimes", ((TcpPingRequest)request).maxTimes);
            span.setAttribute("detection.timeout", ((TcpPingRequest)request).timeout);
        }

        span.setAttribute("detection.traceId", span.getSpanContext().getTraceId());
        span.setAttribute("detection.spanId", span.getSpanContext().getSpanId());
        span.setAttribute("detection.deviceId", Utdid.getInstance().getUtdid(context));

    }

    public void setDetectConfig(DetectConfig config) {
        if (null == span) {
            return;
        }
        FlowNode node = new FlowNode(this.type,
            span.getSpanContext().getTraceId(),
            span.getSpanContext().getSpanId(),
            span instanceof ReadableSpan ?
                ((ReadableSpan)span).getParentSpanContext().getSpanId() : ""
        );
        config.setFlowNode(node);
    }

    public void end() {
        if (null != span) {
            span.end();
        }
    }
}
