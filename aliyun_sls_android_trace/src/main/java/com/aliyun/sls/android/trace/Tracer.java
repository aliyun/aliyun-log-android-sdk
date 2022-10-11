package com.aliyun.sls.android.trace;

import com.aliyun.sls.android.ot.ISpanProcessor;
import com.aliyun.sls.android.ot.ISpanProvider;
import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.Span.StatusCode;
import com.aliyun.sls.android.ot.SpanBuilder;
import com.aliyun.sls.android.ot.context.ContextManager;
import com.aliyun.sls.android.ot.context.Scope;
import com.aliyun.sls.android.producer.Log;

/**
 * @author gordon
 * @date 2022/9/6
 */
public class Tracer {
    static ISpanProcessor spanProcessor;
    static ISpanProvider spanProvider;
    static TraceFeature traceFeature;

    private Tracer() {
        //no instance
    }

    static void setTraceFeature(TraceFeature feature) {
        Tracer.traceFeature = feature;
    }

    /**
     * 写日志
     *
     * @param log
     * @return
     */
    public static boolean log(Log log) {
        if (null == log) {
            return false;
        }

        Span span = spanBuilder("logs").build();
        //span.start /= 1000;
        //span.end /= 1000;
        log.putContents(span.toMap());

        return Tracer.traceFeature.addLog(log);
    }

    /**
     * 构造一个SpanBuilder
     *
     * @param spanName span 名称
     * @return SpanBuilder
     */
    public static SpanBuilder spanBuilder(final String spanName) {
        return new SpanBuilder(spanName, spanProcessor, spanProvider);
    }

    /**
     * 构造一个span
     *
     * @param spanName span 名称
     * @return Span
     */
    public static Span startSpan(final String spanName) {
        return startSpan(spanName, false);
    }

    public static Span startSpan(final String spanName, final boolean active) {
        return spanBuilder(spanName).setActive(active).build();
    }

    /**
     * 传入一个代码块，并自动构造一个span。代码块中的代码会自动执行
     *
     * @param spanName span 名称
     * @param r        代码块
     */
    public static void withinSpan(final String spanName, final Runnable r) {
        withinSpan(spanName, true, r);
    }

    /**
     * 传入一个代码块，并自动构造一个span。代码块中的代码会自动执行
     *
     * @param spanName span 名称
     * @param active   是否保持，如果保持，则代码块中产生的span对象会与当前span自动关联
     * @param r        代码块
     */
    public static void withinSpan(final String spanName, final boolean active, Runnable r) {
        withinSpan(spanName, active, null, r);
    }

    /**
     * 传入一个代码块，并自动构造一个span。代码块中的代码会自动执行
     *
     * @param spanName span 名称
     * @param active   是否保持，如果保持，则代码块中产生的span对象会与当前span自动关联
     * @param parent   当前span会自动关联父span
     * @param r        代码块
     */
    public static void withinSpan(final String spanName, final boolean active, final Span parent, Runnable r) {
        Span span = spanBuilder(spanName).setParent(parent).build();
        try (Scope ignored = active ? ContextManager.INSTANCE.makeCurrent(span) : null) {
            r.run();
        } catch (Throwable t) {
            span.setStatus(StatusCode.ERROR);
            span.setStatusMessage(String.format("exception: {name: %s, reason: %s}", t.getClass().getName(), t.getMessage()));

            span.recordException(t);
        } finally {
            span.end();
        }
    }
}
