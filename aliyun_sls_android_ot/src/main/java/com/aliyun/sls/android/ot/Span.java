package com.aliyun.sls.android.ot;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import android.text.TextUtils;
import android.util.Pair;
import com.aliyun.sls.android.ot.context.Scope;
import com.aliyun.sls.android.ot.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2022/3/31
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
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
    protected List<Event> events;
    protected StatusCode statusCode = StatusCode.UNSET;
    protected String statusMessage;
    protected String host;
    protected Resource resource;
    protected String service;

    protected String sessionId;
    protected String transactionId;

    private final AtomicBoolean finished = new AtomicBoolean();
    /* packaged */Scope scope;

    Span() {
        this.attribute = new LinkedList<>();
        this.events = new LinkedList<>();
        this.resource = new Resource();
    }

    // region getter
    public String getName() {
        return name;
    }

    public SpanKind getKind() {
        return kind;
    }

    public String getTraceId() {
        return traceID;
    }

    public String getSpanId() {
        return spanID;
    }

    public String getParentSpanId() {
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

    public Span setTraceId(String traceID) {
        this.traceID = traceID;
        return this;
    }

    public Span setSpanId(String spanID) {
        this.spanID = spanID;
        return this;
    }

    public Span setParentSpanId(String parentSpanID) {
        this.parentSpanID = parentSpanID;
        return this;
    }

    public Span setParent(Span span) {
        this.parentSpanID = span.parentSpanID;
        this.traceID = span.traceID;
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

    // region event
    public Span addEvent(String name) {
        addEvent(
            Event.create(name)
        );

        return this;
    }

    public Span addEvent(String name, Attribute attribute) {
        addEvent(
            Event.create(name).addAttribute(attribute)
        );

        return this;
    }

    public Span addEvent(String name, Attribute... attributes) {
        addEvent(
            Event.create(name).addAttribute(attributes)
        );

        return this;
    }

    public Span addEvent(String name, List<Attribute> attributes) {
        addEvent(
            Event.create(name).addAttribute(attributes)
        );

        return this;
    }
    // endregion

    // region exception
    public Span recordException(Throwable t) {
        this.recordException(t, (Attribute[])null);
        return this;
    }

    public Span recordException(Throwable t, Attribute... attributes) {
        return recordException(t, null == attributes ? null : Arrays.asList(attributes));
    }

    public Span recordException(Throwable t, List<Attribute> attributes) {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            t.printStackTrace(printWriter);
        }

        addEvent(
            Event.create("exception")
                .addAttribute(
                    Attribute.of(
                        Pair.create("exception.type", t.getClass().getCanonicalName()),
                        Pair.create("exception.message", TextUtils.isEmpty(t.getMessage()) ? "" : t.getMessage()),
                        Pair.create("exception.stacktrace", stringWriter.toString())
                    )
                )
                .addAttribute(attributes)
        );
        return this;
    }
    // endregion

    private void addEvent(Event event) {
        events.add(event);
    }

    // region end
    public boolean end() {
        if (finished.getAndSet(true)) {
            return false;
        }

        this.duration = (this.end - this.start) / 1000;

        if (null != scope) {
            try {
                scope.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean isEnd() {
        return finished.get();
    }
    // endregion

    // region data convertor
    public Map<String, String> toMap() {
        //noinspection deprecation
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
        data.put("service", TextUtils.isEmpty(service) ? "Android" : service);

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

        if (events.size() != 0) {
            JSONArray logs = new JSONArray();
            for (Event event : events) {
                object = new JSONObject();
                JSONUtils.put(object, "name", TextUtils.isEmpty(event.getName()) ? "" : event.getName());
                JSONUtils.put(object, "epochNanos", event.getEpochNanos());
                JSONUtils.put(object, "totalAttributeCount", event.getTotalAttributeCount());

                final List<Attribute> attributes = event.getAttributes();
                Collections.sort(attributes);
                JSONObject attrObject = new JSONObject();
                for (Attribute attr : attributes) {
                    JSONUtils.put(attrObject, attr.key, attr.value);
                }
                JSONUtils.put(object, "attributes", attrObject);

                logs.put(object);
            }

            data.put("logs", logs.toString());
        }

        return data;
    }
    // endregion
}
