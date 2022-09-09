package com.aliyun.sls.android.trace;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aliyun.sls.android.ot.ISpanProcessor;
import com.aliyun.sls.android.ot.ISpanProvider;
import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.Span.StatusCode;
import com.aliyun.sls.android.ot.SpanBuilder;
import com.aliyun.sls.android.ot.context.ContextManager;
import com.aliyun.sls.android.ot.context.Scope;

/**
 * @author gordon
 * @date 2022/9/6
 */
public class Tracer {
    private static Map<String, Session> runningSession = new ConcurrentHashMap<>();

    static ISpanProcessor spanProcessor;
    static ISpanProvider spanProvider;
    static TraceFeature traceFeature;

    private Tracer() {
        //no instance
    }

    static void setTraceFeature(TraceFeature feature) {
        Tracer.traceFeature = feature;
    }

    //public static Session startSession(final String sessionName) {
    //    if (runningSession.containsKey(sessionName)) {
    //        return runningSession.get(sessionName);
    //    }
    //
    //    return Session.startSession(sessionName);
    //}
    //
    //public static void endSession(final Session session) {
    //    if (null == session) {
    //        return;
    //    }
    //
    //    session.end();
    //}

    //public static Span startSpan(final Session session, String spanName) {
    //    if (null == session) {
    //        return null;
    //    }
    //
    //    return session.startChild(spanName);
    //}

    /**
     * 构造一个SpanBuilder
     * @param spanName span 名称
     * @return SpanBuilder
     */
    public static SpanBuilder spanBuilder(final String spanName) {
        return new SpanBuilder(spanName, spanProcessor, spanProvider);
    }

    /**
     * 构造一个span
     * @param spanName span 名称
     * @return Span
     */
    public static Span startSpan(final String spanName) {
        return spanBuilder(spanName).build();
    }

    /**
     * 传入一个代码块，并自动构造一个span。代码块中的代码会自动执行
     * @param spanName span 名称
     * @param r 代码块
     */
    public static void withinSpan(final String spanName, final Runnable r) {
        withinSpan(spanName, true, r);
    }

    /**
     * 传入一个代码块，并自动构造一个span。代码块中的代码会自动执行
     * @param spanName span 名称
     * @param active 是否保持，如果保持，则代码块中产生的span对象会与当前span自动关联
     * @param r 代码块
     */
    public static void withinSpan(final String spanName, final  boolean active, Runnable r) {
        withinSpan(spanName, active, null, r);
    }

    /**
     * 传入一个代码块，并自动构造一个span。代码块中的代码会自动执行
     * @param spanName span 名称
     * @param active 是否保持，如果保持，则代码块中产生的span对象会与当前span自动关联
     * @param parent 当前span会自动关联父span
     * @param r 代码块
     */
    public static void withinSpan(final String spanName, final boolean active, final Span parent, Runnable r) {
        Span span = spanBuilder(spanName).setParent(parent).build();
        try (Scope ignored = active ? ContextManager.INSTANCE.makeCurrent(span) : null) {
            r.run();
        } catch (Throwable t) {
            span.setStatus(StatusCode.of(t.getMessage()));
        } finally {
            span.end();
        }
    }

}
