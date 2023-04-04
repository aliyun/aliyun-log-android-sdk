package com.aliyun.sls.android.ot

import kotlin.coroutines.CoroutineContext

/**
 * @author gordon
 * @date 2022/10/26
 */

fun Span.asContext(): CoroutineContext {
    return SpanContext(this)
}

fun CoroutineContext.getTraceContext(): Span? {
    val element = get(SpanContext.KEY)
    if (element is SpanContext) {
        return element.span
    }

    return null
}