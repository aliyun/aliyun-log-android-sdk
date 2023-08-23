package com.aliyun.sls.android.ot.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.producer.utils.Utils;

/**
 * @author gordon
 * @date 2022/9/8
 */
public enum ContextManager {
    INSTANCE;

    private final ThreadLocal<Context> THREAD_LOCAL_CONTEXT = new ThreadLocal<>();
    private final Map<Span, Context> CONTEXT_CACHE = new ConcurrentHashMap<>();
    private final AtomicReference<Span> GLOBAL_ACTIVE_SPAN = new AtomicReference<>();
    private final SharedPreferences sharedPreferences;
    private final String startupTimestamp = String.valueOf(System.currentTimeMillis());

    ContextManager() {
        android.content.Context ctx = Utils.getContext();
        if (null == ctx) {
            sharedPreferences = null;
        } else {
            sharedPreferences = ctx.getSharedPreferences("sls_cached_span", android.content.Context.MODE_PRIVATE);
        }
    }

    public Context current() {
        Context context = THREAD_LOCAL_CONTEXT.get();
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

    public Span getGlobalActiveSpan() {
        return GLOBAL_ACTIVE_SPAN.get();
    }

    private void removeCachedSpan(List<String> keys, int toIndex) {
        Editor editor = sharedPreferences.edit();
        for (int i = 0; i < toIndex; i++) {
            editor.remove(keys.get(i));
        }
        editor.apply();
    }

    public Span getLastGlobalActiveSpan() {
        if (null == sharedPreferences) {
            return null;
        }

        Map<String, ?> all = sharedPreferences.getAll();
        if (null == all) {
            return null;
        }

        List<String> keys = new ArrayList<>(all.keySet());
        Collections.sort(keys);

        Set<String> finalValue;
        if (keys.size() == 1) {
            finalValue = sharedPreferences.getStringSet(keys.get(0), null);
        } else if (keys.contains(startupTimestamp)) {
            int index = keys.indexOf(startupTimestamp) - 1;
            if (index >= 0) {
                finalValue = sharedPreferences.getStringSet(keys.get(index), null);

                removeCachedSpan(keys, index);
            } else {
                finalValue = sharedPreferences.getStringSet(keys.get(0), null);
            }
        } else {
            finalValue = sharedPreferences.getStringSet(keys.get(keys.size() - 1), null);
            removeCachedSpan(keys, keys.size() - 1);
        }

        if (null == finalValue) {
            return null;
        }

        Span cachedSpan = new CachedSpan();
        for (String value : finalValue) {
            if (value.contains("t:")) {
                cachedSpan.setTraceId(value.split(":")[1]);
            } else if (value.contains("s:")) {
                cachedSpan.setSpanId(value.split(":")[1]);
            } else if (value.contains("p:")) {
                String parentSpanId = value.split(":").length > 1 ? value.split(":")[1] : null;
                if (!TextUtils.isEmpty(parentSpanId)) {
                    cachedSpan.setParentSpanId(parentSpanId);
                }
            }
        }

        return cachedSpan;
    }

    public void setGlobalActiveSpan(Span span) {
        GLOBAL_ACTIVE_SPAN.set(span);

        if (null == sharedPreferences) {
            return;
        }

        sharedPreferences.edit()
            .putStringSet(startupTimestamp, new HashSet<String>() {
                {
                    add(startupTimestamp);
                    add("t:" + span.getTraceId());
                    add("s:" + span.getSpanId());
                    add("p:" + (TextUtils.isEmpty(span.getParentSpanId()) ? "" : span.getParentSpanId()));
                }
            })
            .apply();
    }

    private static class Context {
        Span span;

        Context(Span span) {
            this.span = span;
        }
    }

}
