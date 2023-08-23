package com.aliyun.sls.android.ot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author gordon
 * @date 2022/10/17
 */
public class Link {

    private String traceId;
    private String spanId;
    private final List<Attribute> attributes;
    private final Object lock = new Object();

    private Link(String traceId, String spanId) {
        this.attributes = new ArrayList<>();
        this.traceId = traceId;
        this.spanId = spanId;
    }

    public static Link create(String traceId, String spanId) {
        return new Link(traceId, spanId);
    }

    public Link addAttribute(Attribute... attributes) {
        if (null == attributes) {
            return this;
        }

        this.addAttribute(Arrays.asList(attributes));
        return this;
    }

    public Link addAttribute(List<Attribute> attributes) {
        if (null == attributes) {
            return this;
        }
        synchronized (lock) {
            this.attributes.addAll(attributes);
            return this;
        }
    }

    public List<Attribute> getAttributes() {
        synchronized (lock) {
            return attributes;
        }
    }

    public String getTraceId() {
        synchronized (lock) {
            return traceId;
        }
    }

    public void setTraceId(String traceId) {
        synchronized (lock) {
            this.traceId = traceId;
        }
    }

    public String getSpanId() {
        synchronized (lock) {
            return spanId;
        }
    }

    public void setSpanId(String spanId) {
        synchronized (lock) {
            this.spanId = spanId;
        }
    }
}
