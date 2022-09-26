package com.aliyun.sls.android.ot.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aliyun.sls.android.ot.Span;

/**
 * @author gordon
 * @date 2022/9/8
 */
public enum ContextManager {
    INSTANCE;

    private final ThreadLocal<Context> THREAD_LOCAL_CONTEXT = new ThreadLocal<>();
    private final Map<Span, Context> CONTEXT_CACHE = new ConcurrentHashMap<>();

    public Context current() {
        Context context =  THREAD_LOCAL_CONTEXT.get();
        if (null == context) {
            context = new Context(null);
            THREAD_LOCAL_CONTEXT.set(context);
        }
        return context;
    }

    public void update(Span span) {
        Context context = current();
        context.span = span;
    }

    public Span activeSpan() {
        return null == current() ? null : current().span;
    }

    public Scope makeCurrent(Span span) {
        Context current = CONTEXT_CACHE.get(span);
        if (null == current) {
            current = new Context(span);
            CONTEXT_CACHE.put(span, current);
        }

        final Context beforeContext = current();
        if (beforeContext == current) {
            return NoopScope.INSTANCE;
        }

        THREAD_LOCAL_CONTEXT.set(current);

        Context finalCurrent = current;
        return () -> {
            THREAD_LOCAL_CONTEXT.set(beforeContext);
            //noinspection SuspiciousMethodCalls
            CONTEXT_CACHE.remove(finalCurrent);
        };
    }

    private static class Context {
        Span span;

        Context(Span span) {
            this.span = span;
        }
    }

}
