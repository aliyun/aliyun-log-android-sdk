package com.aliyun.sls.android.producer.example.example

import android.util.Log
import com.aliyun.sls.android.ot.*
import com.aliyun.sls.android.ot.context.ContextManager
import com.aliyun.sls.android.trace.Tracer
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * @author gordon
 * @date 2022/11/2
 */

suspend fun <T : Any?> withTracerContext(
    operationName: String,
    block: suspend () -> T
): T {
    return withContext(operationName) {
        block()
    }
}

 suspend fun logStart(operationName: String, active: Boolean = true, context: CoroutineContext? = null) {
    var parent = context?.getTraceContext()
    if (null == parent) {
        parent = ContextManager.INSTANCE.activeSpan()
    }

    val builder: SpanBuilder = Tracer.spanBuilder(operationName).setParent(parent)


    // 非协程
    if (active) {
        builder.setActive(active)
        val span = builder.build()
        SpanExtension.cache.plus(Pair(operationName, span))


        // 协程外面，但内部没有协程
        val parent1 = Tracer.spanBuilder("parent 1").setActive(true).build()

        Tracer.startSpan("child 1").end()

        parent1.end()

        // 协程外面，但内部没有协程
        val parent2 = Tracer.spanBuilder("parent 2").setActive(true).build()

        val scope = CoroutineScope(CoroutineName("test"))
        scope.launch(parent2.asContext()) {

            val child1 = Tracer.startSpan("test child in coroutine")

            scope.async {
                Tracer.startSpan("test child 2 in coroutine").end()

            }

            launch(child1.asContext()) {

            }

        }

        parent2.end()
    }
    else
    // 协程
    {
        // 协程环境下，需要更新当前协程CoroutineContext
        // 需要更新当前协程CoroutineContext 是在协程创建时传入的
        // CoroutineContext在协程创建后不可变
        // 因此，想实现协程下的span自动关联，需要新起一个协程，并把后续的业务放在该协程调度
        val span = builder.build()
        SpanExtension.cache.plus(Pair(operationName, span))
        kotlinx.coroutines.withContext(span.asContext()) {

        }
    }
}

fun logEnd(operationName: String) {
    val span = SpanExtension.cache[operationName] ?: return
    span.end()
}


suspend fun test() {
    delay(1000)

    withContext(Tracer.startSpan("span in suspend fun").asContext()) {

    }
}


fun log(operationName: String) {
    Tracer.startSpan(operationName).end()
}

object SpanExtension {
    val cache = mapOf<String, Span>()
}