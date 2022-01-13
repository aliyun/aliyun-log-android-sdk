package com.aliyun.sls.android.plugin.trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.content.Context;
import com.aliyun.sls.android.JsonUtil;
import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.SLSLog;
import com.aliyun.sls.android.plugin.ISender;
import com.aliyun.sls.android.plugin.trace.utils.TraceTranslator;
import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerClient;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.LogProducerException;
import com.aliyun.sls.android.producer.LogProducerResult;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.json.JSONArray;
import org.json.JSONObject;

import static android.content.Context.ACTIVITY_SERVICE;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * @author gordon
 * @date 2021/07/29
 */
public class SLSSpanExporter implements SpanExporter, ISender {
    private static final String TAG = "SLSSpanExporter";

    private LogProducerConfig config;
    private LogProducerClient client;

    private SLSConfig slsConfig;

    SLSSpanExporter() {
        // package only
    }

    @Override
    public void init(SLSConfig config) {
        this.slsConfig = config;
        initLogProducer();
    }

    private void initLogProducer() {
        this.config = createConfig();
        if (null == config) {
            return;
        }

        try {
            this.client = new LogProducerClient(config,
                (resultCode, reqId, errorMessage, logBytes, compressedBytes) -> SLSLog
                    .v(TAG, "onCall. result: " + LogProducerResult.fromInt(resultCode) + ", error: " + errorMessage));
        } catch (LogProducerException e) {
            SLSLog.e(TAG, "new LogProducerClient() case error. e: " + e);
        }
    }

    private int getLimitMem(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(ACTIVITY_SERVICE);
        return am.getMemoryClass();
    }

    private LogProducerConfig createConfig() {
        Context context = slsConfig.context;
        String accessKeyId = slsConfig.accessKeyId;
        String accessKeySecret = slsConfig.accessKeySecret;
        String securityToken = slsConfig.securityToken;
        LogProducerConfig config;
        try {
            config = new LogProducerConfig(context, slsConfig.pluginTraceEndpoint, slsConfig.pluginTraceLogProject,
                slsConfig.pluginTraceLogStore, accessKeyId, accessKeySecret,
                securityToken);
        } catch (LogProducerException e) {
            return null;
        }

        config.setTopic("trace_android");
        config.setPacketLogBytes(1024 * 1024);
        config.setPacketLogCount(4096);
        config.setPacketTimeout(2000);
        // 最大内存限制为当前 app 最大heap内存的 50%
        final int maxBufferLimit = (int)(getLimitMem(context) / 2f + 0.5f) * 1024 * 1024;
        SLSLog.d(TAG, "max buffer limit: " + maxBufferLimit);
        config.setMaxBufferLimit(maxBufferLimit);
        // 发送线程数，默认为1
        config.setSendThreadCount(1);

        // 1 开启断点续传功能， 0 关闭
        // 每次发送前会把日志保存到本地的binlog文件，只有发送成功才会删除，保证日志上传At Least Once
        config.setPersistent(1);
        // 持久化的文件名，需要保证文件所在的文件夹已创建。配置多个客户端时，不应设置相同文件

        final File rootPath = new File(new File(context.getFilesDir(), "sls_trace"), "sls_logs");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        config.setPersistentFilePath(rootPath + "/trace_log.dat");
        // 是否每次AddLog强制刷新，高可靠性场景建议打开
        config.setPersistentForceFlush(1);
        // 持久化文件滚动个数，建议设置成10。
        config.setPersistentMaxFileCount(10);
        // 每个持久化文件的大小，建议设置成1-10M
        config.setPersistentMaxFileSize(1024 * 1024 * 10);
        // 本地最多缓存的日志数，不建议超过1M，通常设置为65536即可
        config.setPersistentMaxLogCount(65536);
        config.setDropDelayLog(0);
        config.setDropUnauthorizedLog(0);

        return config;
    }

    @Override
    public boolean send(Log data) {
        final LogProducerResult result = client.addLog(data);
        if (LogProducerResult.LOG_PRODUCER_OK != result) {
            SLSLog.e(TAG, "send spans to sls error. code: " + result);
        } else if (slsConfig.debuggable) {
            SLSLog.v(TAG, "send spans to sls success.");
        }

        return LogProducerResult.LOG_PRODUCER_OK == result;
    }

    @Override
    public void resetSecurityToken(String accessKeyId, String accessKeySecret, String securityToken) {
        this.config.resetSecurityToken(accessKeyId, accessKeySecret, securityToken);
    }

    @Override
    public void resetProject(String newEndpoint, String newProject, String newLogstore) {
        // ignore, trace span exporter use updateConfig method update the project configuration.
    }

    @Override
    public void updateConfig(SLSConfig config) {
        if (null == config) {
            SLSLog.d(TAG, "updateConfig. invalid config");
            return;
        }

        this.config.setEndpoint(config.pluginTraceEndpoint);
        this.config.setProject(config.pluginTraceLogProject);
        this.config.setLogstore(config.pluginTraceLogStore);
        if (slsConfig.debuggable) {
            SLSLog.v(TAG, "updateConfig, new endpoint: " + config.pluginTraceEndpoint + ", new project: "
                + config.pluginTraceLogProject + ", new logstore: " + config.pluginTraceLogStore);
        }
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        final List<CompletableResultCode> resultCodes = new ArrayList<>();

        for (SpanData span : spans) {
            final Log log = spanToLog(span);
            if (slsConfig.debuggable) {
                SLSLog.v(TAG, "export span: " + log);
            }

            final boolean succ = this.send(log);
            resultCodes.add(succ ? CompletableResultCode.ofSuccess() : CompletableResultCode.ofFailure());
        }

        return CompletableResultCode.ofAll(resultCodes);
    }

    @Override
    public CompletableResultCode flush() {
        // sls not support flush now, default success.
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        // default success
        return CompletableResultCode.ofSuccess();
    }

    private static void put(Log log, String key, String value) {
        if (null == key) {
            key = "null";
        }
        if (null == value) {
            value = "null";
        }
        log.putContent(key, value);
    }

    private JSONObject attributesToLog(Map<AttributeKey<?>, Object> attributes, String... excludeKeys) {
        JSONObject object = new JSONObject();
        List<String> keys = null != excludeKeys ? Arrays.asList(excludeKeys.clone()) : null;
        for (Map.Entry<AttributeKey<?>, Object> entry : attributes.entrySet()) {
            final String key = entry.getKey().getKey();
            if (null != keys && keys.contains(key)) {
                continue;
            }

            JsonUtil.putOpt(object, key, entry.getValue());
        }
        return object;
    }

    private String eventsToLog(List<EventData> eventDataList) {
        JSONArray array = new JSONArray();
        for (EventData eventData : eventDataList) {
            JSONObject object = new JSONObject();
            JsonUtil.putOpt(object, "name", eventData.getName());
            JsonUtil.putOpt(object, "droppedAttributesCount", eventData.getDroppedAttributesCount());
            JsonUtil.putOpt(object, "epochNanos", eventData.getEpochNanos());
            JsonUtil.putOpt(object, "totalAttributeCount", eventData.getTotalAttributeCount());
            if (null != eventData.getAttributes()) {
                JsonUtil.putOpt(object, "attributes", attributesToLog(eventData.getAttributes().asMap()));
            }
            array.put(object);
        }

        return array.toString();
    }

    private String instrumentationLibraryInfoToJSON(InstrumentationLibraryInfo instrumentationLibraryInfo) {
        JSONObject instrumentationLibraryInfoJSON = new JSONObject();
        JsonUtil.putOpt(instrumentationLibraryInfoJSON, "name", instrumentationLibraryInfo.getName());
        JsonUtil.putOpt(instrumentationLibraryInfoJSON, "schemaUrl", instrumentationLibraryInfo.getSchemaUrl());
        JsonUtil.putOpt(instrumentationLibraryInfoJSON, "version", instrumentationLibraryInfo.getVersion());

        return instrumentationLibraryInfoJSON.toString();
    }

    private String linksToLog(List<LinkData> linkDataList) {
        JSONArray array = new JSONArray();
        for (LinkData linkData : linkDataList) {
            //            JSONObject object = new JSONObject();
            if (null != linkData.getAttributes()) {
                array.put(attributesToLog(linkData.getAttributes().asMap()));
            }
            //            JsonUtil.putOpt(object, "totalAttributeCount", linkData.getTotalAttributeCount());
            //            JsonUtil.putOpt(linkDataJSON, "", linkData.getSpanContext());
        }

        return array.toString();
    }

    private String resourceToLog(Resource resource, String... excludeKeys) {
        JSONObject object;
        if (null != resource.getAttributes()) {
            object = attributesToLog(resource.getAttributes().asMap(), excludeKeys);
        } else {
            object = new JSONObject();
        }
        return object.toString();
    }

    private String spanContextToJSON(SpanContext spanContext) {
        JSONObject contextJSON = new JSONObject();
        JsonUtil.putOpt(contextJSON, "spanId", spanContext.getSpanId());
        JsonUtil.putOpt(contextJSON, "traceFlags", spanContext.getTraceFlags().asHex());
        JsonUtil.putOpt(contextJSON, "traceId", spanContext.getTraceId());
        JsonUtil.putOpt(contextJSON, "traceState", traceStateToLog(spanContext.getTraceState()));

        return contextJSON.toString();
    }

    private String traceStateToLog(TraceState traceState) {
        JSONObject object = new JSONObject();
        for (Map.Entry<String, String> entry : traceState.asMap().entrySet()) {
            JsonUtil.putOpt(object, entry.getKey(), entry.getValue());
        }
        return object.toString();
    }

    private Log spanToLog(SpanData span) {
        Log log = new Log();

        if (null != span.getResource()) {
            Resource resource = span.getResource();
            put(log, "host", TraceTranslator.attrsValueToString(resource.getAttributes(), "host.name"));
            put(log, "service", TraceTranslator.attrsValueToString(resource.getAttributes(), "service.name"));
            put(log, "resource", resourceToLog(resource, "host.name", "service.name"));
        }

        put(log, "otlp.name", "android-sdk");
        put(log, "otlp.version", BuildConfig.VERSION_NAME);

        put(log, "name", span.getName());
        put(log, "kind", span.getKind().name());
        put(log, "traceID", span.getTraceId());
        put(log, "spanID", span.getSpanId());
        put(log, "parentSpanID", span.getParentSpanId());

        if (null != span.getLinks()) {
            put(log, "links", linksToLog(span.getLinks()));
        }

        if (null != span.getEvents()) {
            put(log, "logs", eventsToLog(span.getEvents()));
        }

        if (null != span.getSpanContext()) {
            put(log, "traceState", traceStateToLog(span.getSpanContext().getTraceState()));
        }

        put(log, "start", String.valueOf(span.getStartEpochNanos() / 1000));
        put(log, "end", String.valueOf(span.getEndEpochNanos() / 1000));
        put(log, "duration", String.valueOf((span.getEndEpochNanos() - span.getStartEpochNanos()) / 1000));

        if (null != span.getAttributes()) {
            put(log, "attribute", attributesToLog(span.getAttributes().asMap()).toString());
        }

        put(log, "statusCode", span.getStatus().getStatusCode().name());
        put(log, "statusMessage", span.getStatus().getDescription());

        //
        //
        //        if (null != span.getInstrumentationLibraryInfo()) {
        //            put(log, "instrumentationLibraryInfo", instrumentationLibraryInfoToJSON(span
        //            .getInstrumentationLibraryInfo()));
        //        }
        //
        //        if (null != span.getInstrumentationLibraryInfo()) {
        //            put(log, "instrumentationLibraryInfo", instrumentationLibraryInfoToJSON(span
        //            .getInstrumentationLibraryInfo()));
        //        }
        //
        //        if (null != span.getParentSpanContext()) {
        //            put(log, "parentSpanContext", spanContextToJSON(span.getParentSpanContext()));
        //        }
        //
        //
        //        if (null != span.getSpanContext()) {
        //            put(log, "spanContext", spanContextToJSON(span.getSpanContext()));
        //        }
        //
        //
        //        put(log, "totalAttributeCount", String.valueOf(span.getTotalAttributeCount()));
        //        put(log, "totalRecordedEvents", String.valueOf(span.getTotalRecordedEvents()));
        //        put(log, "totalRecordedLinks", String.valueOf(span.getTotalRecordedLinks()));

        return log;
    }

    private static long toEpochMicros(long epochNanos) {
        return NANOSECONDS.toMicros(epochNanos);
    }
}
