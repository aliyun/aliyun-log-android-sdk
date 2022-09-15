package com.aliyun.sls.android.ot;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
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
        ERROR("ERROR"),
        CUSTOM("CUSTOM");

        public String code;
        public String message;

        StatusCode(String code) {
            this.code = code;
        }

        public static StatusCode of(String code, String message) {
            StatusCode statusCode = CUSTOM;
            statusCode.code = code;
            statusCode.message = message;

            return statusCode;
        }

        public static StatusCode of(String message) {
            StatusCode statusCode = ERROR;
            statusCode.message = message;

            return statusCode;
        }

    }

    public String name;
    public SpanKind kind = SpanKind.CLIENT;
    public String traceID;
    public String spanID;
    public String parentSpanID;
    public long start;
    public long end;
    public long duration;
    public List<Attribute> attribute;
    public StatusCode statusCode = StatusCode.UNSET;
    public String statusMessage;
    public String host;
    public Resource resource;
    public String service;

    public String sessionId;
    public String transactionId;

    private final AtomicBoolean finished = new AtomicBoolean();

    public Span() {
    }

    public void addAttribute(Attribute attribute) {
        this.attribute.add(attribute);
    }

    public void addAttribute(Attribute... attributes) {
        this.addAttribute(Arrays.asList(attributes));
    }

    public void addAttribute(List<Attribute> attributes) {
        this.attribute.addAll(attributes);
    }

    public void setStatus(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

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

    public boolean isFinished() {
        return finished.get();
    }

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
}
