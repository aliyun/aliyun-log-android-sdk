package com.aliyun.sls.android.ot;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.aliyun.sls.android.ot.utils.IdGenerator;
import com.aliyun.sls.android.ot.utils.TimeUtils;

/**
 * @author gordon
 * @date 2022/4/12
 */
public class SpanBuilder {
    private final String spanName;
    private final ISpanProcessor spanProcessor;
    private final ISpanProvider spanProvider;
    private Span parent;
    private SpanKind kind = SpanKind.CLIENT;
    private final List<Attribute> attributes = new CopyOnWriteArrayList<>();
    private Resource resource;
    private Long start = null;

    public SpanBuilder(String spanName, ISpanProcessor processor, ISpanProvider provider) {
        this.spanName = spanName;
        this.spanProcessor = processor;
        this.spanProvider = provider;
    }

    public SpanBuilder setParent(Span span) {
        this.parent = span;
        return this;
    }

    public SpanBuilder setKind(SpanKind kind) {
        this.kind = kind;
        return this;
    }

    public SpanBuilder addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
        return this;
    }

    public SpanBuilder addAttribute(List<Attribute> attributes) {
        this.attributes.addAll(attributes);
        return this;
    }

    public SpanBuilder setStart(Long start) {
        this.start = start;
        return this;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Span build() {
        Span span = new RecordableSpan(spanProcessor);
        span.name = spanName;
        span.spanID = IdGenerator.generateSpanId();

        // find the parent span first.
        final Span parentSpan;
        if (null != parent) {
            parentSpan = parent;
        } else {
            parentSpan = null;
        }

        if (null != parentSpan) {
            span.traceID = parentSpan.traceID;
            span.parentSpanID = parentSpan.spanID;
        } else {
            span.traceID = IdGenerator.generateTraceId();
        }

        span.kind = kind;
        span.attribute = new LinkedList<>();
        if (null != spanProvider) {
            span.attribute.addAll(spanProvider.provideAttribute());
        }

        span.attribute.addAll(attributes);

        Resource r = Resource.getDefault();
        if (null != spanProvider) {
            r.merge(spanProvider.provideResource());
        }
        if (null != resource) {
            r.merge(resource);
        }
        span.resource = r;

        if (null != start) {
            span.start = start;
        } else {
            span.start = TimeUtils.instance.now();
        }

        return span;
    }
}
