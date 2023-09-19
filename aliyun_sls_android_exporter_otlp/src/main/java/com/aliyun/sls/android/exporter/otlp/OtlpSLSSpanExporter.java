package com.aliyun.sls.android.exporter.otlp;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import com.aliyun.sls.android.otel.common.AccessKey;
import com.aliyun.sls.android.otel.common.ConfigurationManager;
import com.aliyun.sls.android.otel.common.ConfigurationManager.AccessKeyProvider;
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

import static android.util.Log.e;
import static android.util.Log.v;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * @author yulong.gyl
 * @date 2023/9/7
 */
public class OtlpSLSSpanExporter implements SpanExporter {

    private static final String TAG = "SLSSpanExporter";

    private LogProducerConfig config;
    private LogProducerClient client;

    private final String scope;
    private String endpoint;
    private String project;
    private String logstore;
    private boolean isPersistentFlush = false;
    private String accessKeyId;
    private String accessKeySecret;
    private String accessKeyToken;

    public static OtlpSLSSpanExporterBuilder builder() {
        return new OtlpSLSSpanExporterBuilder();
    }

    OtlpSLSSpanExporter(String scope, String endpoint, String project, String logstore, boolean isPersistentFlush,
        String accessKeyId, String accessKeySecret, String accessKeyToken) {
        this.scope = scope;
        this.endpoint = endpoint;
        this.project = project;
        this.logstore = logstore;
        this.isPersistentFlush = isPersistentFlush;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.accessKeyToken = accessKeyToken;
        initLogProducer();
    }

    private void initLogProducer() {
        this.config = createConfig();
        if (null == config) {
            return;
        }

        try {
            this.client = new LogProducerClient(config,
                (resultCode, reqId, errorMessage, logBytes, compressedBytes) -> {
                    v(TAG,
                        "client onCall. result: " + LogProducerResult.fromInt(resultCode) + ", error: " + errorMessage);

                    final AccessKeyProvider provider = ConfigurationManager.getInstance().getAccessKeyProvider();
                    if (null == provider) {
                        return;
                    }
                    final AccessKey accessKey = provider.getAccessKey(scope);
                    if (null == accessKey) {
                        return;
                    }

                    final String accessKeyId = accessKey.getAccessKeyId();
                    final String accessKeySecret = accessKey.getAccessKeySecret();
                    final String accessKeyToken = accessKey.getAccessKeySecurityToken();
                    if (TextUtils.isEmpty(accessKeyToken)) {
                        config.setAccessKeyId(accessKeyId);
                        config.setAccessKeySecret(accessKeySecret);
                    } else {
                        config.resetSecurityToken(accessKey.getAccessKeyId(),
                            accessKey.getAccessKeySecret(),
                            accessKey.getAccessKeySecurityToken()
                        );
                    }
                });
        } catch (LogProducerException e) {
            e(TAG, "new LogProducerClient() case error. e: " + e);
        }
    }

    private LogProducerConfig createConfig() {
        LogProducerConfig config;
        try {
            config = new LogProducerConfig(endpoint, project, logstore, accessKeyId, accessKeySecret, accessKeyToken);
        } catch (LogProducerException e) {
            return null;
        }

        config.setTopic(scope);
        config.setPacketLogBytes(1024 * 1024);
        config.setPacketLogCount(4096);
        config.setPacketTimeout(3000);
        config.setMaxBufferLimit(32 * 1024 * 1024);

        config.setPersistent(1);
        final File rootPath = new File(config.getContext().getFilesDir(), scope);
        if (!rootPath.exists()) {
            boolean ignored = rootPath.mkdirs();
        }
        config.setPersistentFilePath(rootPath + "/data");
        // 是否每次AddLog强制刷新，高可靠性场景建议打开
        config.setPersistentForceFlush(this.isPersistentFlush ? 1 : 0);
        // 持久化文件滚动个数，建议设置成10。
        config.setPersistentMaxFileCount(10);
        // 每个持久化文件的大小，建议设置成1-10M
        config.setPersistentMaxFileSize(1024 * 1024 * 10);
        // 本地最多缓存的日志数，不建议超过1M，通常设置为65536即可
        config.setPersistentMaxLogCount(65536);
        config.setDropDelayLog(0);
        config.setDropUnauthorizedLog(0);
        config.logProducerDebug();

        return config;
    }

    public boolean send(Log data) {
        final LogProducerResult result = client.addLog(data);
        if (LogProducerResult.LOG_PRODUCER_OK != result) {
            e(TAG, "send spans to sls error. code: " + result);
        }
        //else if (slsConfig.debuggable) {
        //    SLSLog.v(TAG, "send spans to sls success.");
        //}

        return LogProducerResult.LOG_PRODUCER_OK == result;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        final List<CompletableResultCode> resultCodes = new ArrayList<>();

        for (SpanData span : spans) {
            final Log log = spanToLog(span);
            //if (slsConfig.debuggable) {
            //    SLSLog.v(TAG, "export span: " + log);
            //}

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
