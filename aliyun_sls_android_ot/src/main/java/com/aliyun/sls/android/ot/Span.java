package com.aliyun.sls.android.ot;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aliyun.sls.android.ot.context.ContextManager;
import com.aliyun.sls.android.ot.utils.JSONUtils;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2022/3/31
 */
public class Span {
    public enum StatusCode {
        UNSET("UNSET"),
        OK("OK"),
        ERROR("ERROR");

        public String code;
        public String message;

        StatusCode(String code) {
            this.code = code;
        }

        public static StatusCode of(String message) {
            StatusCode statusCode = ERROR;
            statusCode.message = message;

            return statusCode;
        }

    }

    protected String name;
    protected SpanKind kind = SpanKind.CLIENT;
    protected String traceID;
    protected String spanID;
    protected String parentSpanID;
    protected long start;
    protected long end;
    protected long duration;
    protected List<Attribute> attribute;
    protected StatusCode statusCode = StatusCode.UNSET;
    protected String statusMessage;
    protected String host;
    protected Resource resource;
    protected String service;

    protected String sessionId;
    protected String transactionId;

    private final AtomicBoolean finished = new AtomicBoolean();

    Span() {
        this.attribute = new LinkedList<>();
        this.resource = new Resource();
    }

    // region getter
    public String getName() {
        return name;
    }

    public SpanKind getKind() {
        return kind;
    }

    public String getTraceID() {
        return traceID;
    }

    public String getSpanID() {
        return spanID;
    }

    public String getParentSpanID() {
        return parentSpanID;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getDuration() {
        return duration;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getHost() {
        return host;
    }

    public String getService() {
        return service;
    }
    // endregion

    // region setter
    public Span setName(String name) {
        this.name = name;
        return this;
    }

    public Span setKind(SpanKind kind) {
        this.kind = kind;
        return this;
    }

    public Span setTraceID(String traceID) {
        this.traceID = traceID;
        return this;
    }

    public Span setSpanID(String spanID) {
        this.spanID = spanID;
        return this;
    }

    public Span setParentSpanID(String parentSpanID) {
        this.parentSpanID = parentSpanID;
        return this;
    }

    public Span setStart(long start) {
        this.start = start;
        return this;
    }

    public Span setEnd(long end) {
        this.end = end;
        return this;
    }

    public Span setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public Span setStatus(StatusCode statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public Span setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    public Span setHost(String host) {
        this.host = host;
        return this;
    }

    public Span setService(String service) {
        this.service = service;
        return this;
    }
    // endregion

    // region attribute & resource
    public Span addAttribute(Attribute attribute) {
        this.attribute.add(attribute);
        return this;
    }

    public Span addAttribute(Attribute... attributes) {
        this.addAttribute(Arrays.asList(attributes));
        return this;
    }

    public Span addAttribute(List<Attribute> attributes) {
        this.attribute.addAll(attributes);
        return this;
    }

    public Span addResource(Resource r) {
        if (null == r) {
            return this;
        }

        this.resource.merge(r);
        return this;
    }
    // endregion

    // region end
    public boolean end() {
        if (finished.getAndSet(true)) {
            return false;
        }

        this.duration = (this.end - this.start) / 1000;

        if (ContextManager.INSTANCE.activeSpan() == this) {
            ContextManager.INSTANCE.update(null);
        }
        return true;
    }

    public boolean isEnd() {
        return finished.get();
    }
    // endregion

    // region data convertor
    public Map<String, String> toMap() {
        return this.toData();
    }

    @Deprecated
    public Map<String, String> toData() {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("name", name);
        data.put("kind", kind.kind);
        data.put("traceID", traceID);
        data.put("spanID", spanID);
        data.put("parentSpanID", parentSpanID);
        data.put("sid", sessionId);
        data.put("pid", transactionId);
        data.put("start", String.valueOf(start));
        data.put("duration", String.valueOf(duration));
        data.put("end", String.valueOf(end));
        data.put("statusCode", statusCode.code);
        data.put("statusMessage", statusMessage);
        data.put("host", host);
        data.put("service", "Android");

        JSONObject object = new JSONObject();
        Collections.sort(attribute);
        for (Attribute attr : attribute) {
            JSONUtils.put(object, attr.key, attr.value);
        }
        data.put("attribute", object.toString());

        if (null != resource) {
            Collections.sort(resource.attributes);
            object = new JSONObject();
            for (Attribute attribute : resource.attributes) {
                JSONUtils.put(object, attribute.key, attribute.value);
            }
            data.put("resource", object.toString());
        }

        return data;
    }
    // endreigon
}
