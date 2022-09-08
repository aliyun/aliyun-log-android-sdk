package com.aliyun.sls.android.trace;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.ISpanProcessor;
import com.aliyun.sls.android.ot.ISpanProvider;
import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.SpanBuilder;

/**
 * @author gordon
 * @date 2022/9/5
 */
@SuppressWarnings("unused")
public class Session {
    private final List<Span> children = new CopyOnWriteArrayList<>();
    private Span sessionRoot;
    private ISpanProcessor processor;
    private ISpanProvider provider;

    public static Session startSession(String sessionName) {
        Session session = new Session();
        session.processor = Tracer.spanProcessor;
        session.provider = Tracer.spanProvider;

        session.sessionRoot = new SpanBuilder(sessionName, session.processor, session.provider)
            .addAttribute(Attribute.of("t", "session"))
            .build();
        session.sessionRoot.sessionId = session.sessionRoot.spanID;
        return session;
    }

    public Span getRoot() {
        return sessionRoot;
    }

    public Span startChild(String spanName) {
        Span span = new SpanBuilder(spanName, this.processor, this.provider)
            .setParent(sessionRoot)
            .build();
        children.add(span);
        return span;
    }

    public void end() {
        for (Span child : children) {
            if (child.isFinished()) {
                continue;
            }

            child.end();
        }

        sessionRoot.end();
    }

    public String getSessionId() {
        return sessionRoot.sessionId;
    }

}
