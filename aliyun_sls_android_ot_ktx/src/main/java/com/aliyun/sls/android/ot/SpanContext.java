package com.aliyun.sls.android.ot;

import java.io.IOException;

import com.aliyun.sls.android.ot.context.ContextManager;
import com.aliyun.sls.android.ot.context.Scope;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.ThreadContextElement;
import org.jetbrains.annotations.Nullable;

/**
 * @author gordon
 * @date 2022/10/26
 */
public class SpanContext implements ThreadContextElement<Scope>{

    static final CoroutineContext.Key<SpanContext> KEY = new CoroutineContext.Key<SpanContext>(){};
    private final Span span;

    public SpanContext(Span span) {
        this.span = span;
    }

    public Span getSpan() {
        return span;
    }

    @Override
    public Key<?> getKey() {
        return KEY;
    }

    @Override
    public <R> R fold(R r,
        Function2<? super R, ? super Element, ? extends R> operation) {
        return CoroutineContext.Element.DefaultImpls.fold(this, r, operation);
    }

    @Nullable
    @Override
    public <E extends Element> E get(Key<E> key) {
        return CoroutineContext.Element.DefaultImpls.get(this, key);
    }

    @Override
    public CoroutineContext minusKey(Key<?> key) {
        return CoroutineContext.Element.DefaultImpls.minusKey(this, key);
    }

    @Override
    public CoroutineContext plus(CoroutineContext coroutineContext) {
        return CoroutineContext.DefaultImpls.plus(this, coroutineContext);
    }

    @Override
    public void restoreThreadContext(CoroutineContext coroutineContext, Scope scope) {
        try {
            scope.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Scope updateThreadContext(CoroutineContext coroutineContext) {
        return ContextManager.INSTANCE.makeCurrent(span);
    }
}
