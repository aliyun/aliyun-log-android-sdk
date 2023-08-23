@file:Suppress("unused")

package com.aliyun.sls.android.ot

import com.aliyun.sls.android.ot.context.ContextManager
import com.aliyun.sls.android.trace.Tracer
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * @author gordon
 * @date 2022/10/27
 */

//fun <T : Any?> startSpan(
//    operationName: String,
//    parent: Span? = null,
//    active: Boolean = false,
//    block: Span.() -> T
//): T {
//    val span: Span = Tracer.spanBuilder(operationName).setParent(parent).setActive(active).build();
//
//
//
//
//    span.end()
//    return block.invoke(span)
//}


inline fun <T : Any?> withinSpan(
    operationName: String,
    parent: Span? = null,
    active: Boolean = true,
    block: Span.() -> T
): T {
    val span: Span = Tracer.spanBuilder(operationName).setParent(parent).build()

    val scope = if (active) ContextManager.INSTANCE.makeCurrent(span) else null
    return try {
        span.block()
    } catch (e: Throwable) {
        span.setStatus(Span.StatusCode.ERROR)
        span.recordException(e)
        throw e
    } finally {
        span.end()
        scope?.close()
    }
}

// region coroutine
fun withCoroutineScope(
    operationName: String,
    parent: Span? = null,
    context: CoroutineContext = Dispatchers.Main.immediate
): CoroutineScope {
    val span = Tracer.startSpan(operationName).setParent(parent)
    span.end()
    return CoroutineScope(span.asContext() + context)
}

fun <T> withRunBlocking(
    operationName: String,
    parent: Span? = null,
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScopeSpan.() -> T
): T {
    val parentSpan: Span? = ensureParentSpan(parent, context)
    val span = Tracer.spanBuilder(operationName).setParent(parentSpan).build()
    return runBlocking(span.asContext() + context) {
        val t: T
        try {
            t = block(CoroutineScopeSpan(this, span))
        } catch (e: Throwable) {
            span.setStatus(Span.StatusCode.ERROR)
            span.recordException(e)
            throw e
        } finally {
            span.end()
        }
        t
    }
}

fun CoroutineScope.withLaunch(
    operationName: String,
    parent: Span? = null,
    context: CoroutineContext = Dispatchers.Default,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScopeSpan.() -> Unit
): Job {
    val parentSpan: Span? = ensureParentSpan(parent, context, coroutineContext)
    val span = Tracer.spanBuilder(operationName).setParent(parentSpan).build()
    return launch(span.asContext(), start) {
        try {
            block(CoroutineScopeSpan(this, span))
        } catch (e: Throwable) {
            span.setStatus(Span.StatusCode.ERROR)
            span.recordException(e)
            throw e
        } finally {
            span.end()
        }
    }
}

suspend fun <T : Any?> withContext(
    operationName: String,
    parent: Span? = null,
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScopeSpan.() -> T
): T {
    val parentSpan: Span? = ensureParentSpan(parent, context, coroutineContext)
    val span = Tracer.spanBuilder(operationName).setParent(parentSpan).build()
    return withContext(span.asContext() + context) {
        val t: T
        try {
            t = block(CoroutineScopeSpan(this, span))
        } catch (e: Throwable) {
            span.setStatus(Span.StatusCode.ERROR)
            span.recordException(e)
            throw e
        } finally {
            span.end()
        }
        t
    }
}

fun <T : Any?> CoroutineScope.withAsync(
    operationName: String,
    parent: Span? = null,
    context: CoroutineContext = Dispatchers.Default,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScopeSpan.() -> T
): Deferred<T> {
    val parentSpan: Span? = ensureParentSpan(parent, context, coroutineContext)
    val span = Tracer.spanBuilder(operationName).setParent(parentSpan).build()
    return async(span.asContext() + context, start) {
        val t: T
        try {
            t = block(CoroutineScopeSpan(this, span))
        } catch (e: Throwable) {
            span.setStatus(Span.StatusCode.ERROR)
            span.recordException(e)
            throw e
        } finally {
            span.end()
        }
        t
    }
}

suspend fun <T : Any?> Deferred<T>.withAwait(
    operationName: String
): T {
    val parent: Span? = coroutineContext.getTraceContext()
    return withinSpan(operationName, parent, active = false) {
        this@withAwait.await()
    }
}

private suspend fun <T : Any?> CoroutineScope.withCoroutineSpan(
    operationName: String,
    parent: Span? = null,
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScopeSpan.() -> T
): T {
    return withinSpan(
        operationName,
        ensureParentSpan(parent, context, coroutineContext),
        context != Dispatchers.Unconfined
    ) {
        block(CoroutineScopeSpan(this@withCoroutineSpan, this))
    }
}

private fun ensureParentSpan(
    parent: Span? = null,
    context: CoroutineContext? = null,
    insideContext: CoroutineContext? = null
): Span? {
    if (null != parent) {
        return parent
    }

    if (null != context && null != context.getTraceContext()) {
        return context.getTraceContext()
    }

    if (null != insideContext && null != insideContext.getTraceContext()) {
        return insideContext.getTraceContext()
    }

    return null
}
// endregion