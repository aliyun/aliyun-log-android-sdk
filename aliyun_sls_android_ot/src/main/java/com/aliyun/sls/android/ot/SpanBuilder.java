package com.aliyun.sls.android.ot;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.aliyun.sls.android.ot.context.ContextManager;
import com.aliyun.sls.android.ot.utils.IdGenerator;
import com.aliyun.sls.android.ot.utils.TimeUtils;

/**
 * @author gordon
 * @date 2022/4/12
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class SpanBuilder {
    private final String spanName;
    private final ISpanProcessor spanProcessor;
    private final ISpanProvider spanProvider;
    private Span parent;
    private boolean active = false;
    private SpanKind kind = SpanKind.CLIENT;
    private final List<Attribute> attributes = new CopyOnWriteArrayList<>();
    private final Resource resource = new Resource();
    private Long start = null;

    // region constructor
    public SpanBuilder(String spanName, ISpanProcessor processor, ISpanProvider provider) {
        this.spanName = spanName;
        this.spanProcessor = processor;
        this.spanProvider = provider;
    }
    // endregion

    // region setter
    public SpanBuilder setParent(Span span) {
        this.parent = span;
        return this;
    }

    public SpanBuilder setActive(boolean active) {
        this.active = active;
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

    public SpanBuilder addResource(Resource resource) {
        this.resource.merge(resource);
        return this;
    }

    // endregion

    // region build
    public Span build() {
        Span span = new RecordableSpan(spanProcessor);
        span.setName(spanName);
        span.setSpanId(IdGenerator.generateSpanId());

        // find the parent span first.
        final Span parentSpan;
        if (null != parent) {
            parentSpan = parent;
        } else {
            parentSpan = ContextManager.INSTANCE.activeSpan();
        }

        if (null != parentSpan) {
            span.setTraceId(parentSpan.getTraceId());
            span.setParentSpanId(parentSpan.getSpanId());
        } else {
            span.setTraceId(IdGenerator.generateTraceId());
        }

        span.setKind(kind);
        if (null != spanProvider) {
            List<Attribute> attrs = spanProvider.provideAttribute();
            if (null != attrs) {
                span.addAttribute(attrs);
            }
        }

        span.addAttribute(attributes);

        Resource r = Resource.getDefault();
        if (null != spanProvider) {
            r.merge(spanProvider.provideResource());
        }
        r.merge(resource);
        span.addResource(r);

        span.setStart(null != start ? start : TimeUtils.instance.now());

        if (active) {
            span.scope = ContextManager.INSTANCE.makeCurrent(span);
        }

        return span;
    }
    // endregion
}
