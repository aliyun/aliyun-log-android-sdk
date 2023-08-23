package com.aliyun.sls.android.ot

import com.aliyun.sls.android.ot.context.ContextManager
import com.aliyun.sls.android.trace.Tracer
import org.junit.Test
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Before

/**
 * @author gordon
 * @date 2022/10/26
 */
class KotlinCoroutinesExtensionsTest {

    class TestProcessorAndProvider: ISpanProcessor, ISpanProvider {
        override fun onEnd(span: Span?): Boolean {
            return true
        }

        override fun provideResource(): Resource {
            return Resource()
        }

        override fun provideAttribute(): MutableList<Attribute> {
            return mutableListOf()
        }

    }

    @Before
    fun setup() {
        val processorAndProvider = TestProcessorAndProvider()
        Tracer.spanProcessor = processorAndProvider
        Tracer.spanProvider = processorAndProvider
    }


    @Test
    fun runWithContext() {
//        val spanContext = Span().setName("span_context")
//        ContextManager.INSTANCE.makeCurrent(spanContext)
//        val context1 = ContextManager.INSTANCE.current()
        val rootSpan = Span().setName("root")

        runBlocking(Dispatchers.Default + rootSpan.asContext()) {
//            assertThat()
            assertEquals("root", ContextManager.INSTANCE.activeSpan().name)
            assertEquals(coroutineContext.getTraceContext(), ContextManager.INSTANCE.activeSpan())

            val span2 = Span().setName("child1")
//            ContextManager.INSTANCE.makeCurrent(span2)
//            val context2 = ContextManager.INSTANCE.current()
            withContext(span2.asContext()) {
                assertEquals( "child1", ContextManager.INSTANCE.activeSpan().name)
            }

            assertEquals("root", ContextManager.INSTANCE.activeSpan().name)

            async(Dispatchers.IO) {
                assertEquals("root", ContextManager.INSTANCE.activeSpan().name)
            }.await()

            coroutineScope {
                assertEquals("root", ContextManager.INSTANCE.activeSpan().name)
            }



            CoroutineScope(Dispatchers.IO).async {
                assertNull(ContextManager.INSTANCE.activeSpan())
            }.await()

            launch {
                assertEquals("root", ContextManager.INSTANCE.activeSpan().name)
            }

            launch {
                coroutineFun()
            }

            launch(rootSpan.asContext()) {

            }


        }
    }

    @Test
    fun customScopeTest() {
        val testScopeSpan = Span().setName("scope_span")
        val testScope = CoroutineScope(testScopeSpan.asContext())
        testScope.launch {
            assertEquals("testScopeSpan", ContextManager.INSTANCE.activeSpan().name)
            funInScope()
            assertEquals("testScopeSpan", ContextManager.INSTANCE.activeSpan().name)
        }
    }

    private suspend fun funInScope() {
        assertEquals("testScopeSpan", ContextManager.INSTANCE.activeSpan().name)
        funInScopeChild()
        assertEquals("testScopeSpan", ContextManager.INSTANCE.activeSpan().name)
    }

    private suspend fun funInScopeChild() {
        assertEquals("testScopeSpan", ContextManager.INSTANCE.activeSpan().name)
    }

    private suspend fun coroutineFun() {
        assertEquals("root", ContextManager.INSTANCE.activeSpan().name)
    }

    @Test
    fun runWithSpan() {
        val span = Span().setName("span")

        runBlocking(Dispatchers.Default + span.asContext()) {
            assertEquals(span, ContextManager.INSTANCE.activeSpan())
        }
    }

    @Test
    fun getTraceContextOutsideOfContext() {
        runBlocking(Dispatchers.Default) {
            assertEquals(null, coroutineContext.getTraceContext())
        }
    }

    @Test
    fun withinSpanTest() {
//        Tracer.withinSpan()
        withinTest()
    }

    private fun withinTest() {
        val parent = Tracer.spanBuilder("parent").build()
        withinSpan("test", parent) {
            assertEquals("test", ContextManager.INSTANCE.activeSpan().name)
            val child1 = Tracer.startSpan("child1")
            assertEquals(ContextManager.INSTANCE.activeSpan().traceID, child1.traceID)

            val child2 = Tracer.startSpan("child2")
            assertEquals(ContextManager.INSTANCE.activeSpan().traceID, child2.traceID)
        }
    }

    @Test
    fun withinSpanStressTest() {
        val root = Tracer.spanBuilder("root").build()
//        var viewScope = CoroutineScope(root.asContextElement())
//        viewScope.
        runBlocking(root.asContext()) {
            assertEquals(root, ContextManager.INSTANCE.activeSpan())
            delay(10)
            assertEquals(root, ContextManager.INSTANCE.activeSpan())

            for (i in 0 until 500) {
                launch(Dispatchers.IO) {
                    val name = "coroutine:$i"
//                    println("async, name$name, thread: ${Thread.currentThread().name}")

                    withContext(name) {
                //                        println("span1, name$name, thread: ${Thread.currentThread().name}")
                        assertEquals(name, ContextManager.INSTANCE.activeSpan().name)
                        val child = Tracer.startSpan("child in coroutine:$i")
                        assertEquals(ContextManager.INSTANCE.activeSpan().spanId, child.parentSpanId)

                        launch {
                            val child1 = Tracer.startSpan("child in nested coroutines")
                            assertEquals(ContextManager.INSTANCE.activeSpan().traceId, child1.traceId)
                        }

                        assertEquals(name, ContextManager.INSTANCE.activeSpan().name)
                    }
                }

                launch(Dispatchers.IO) {
                    val name = "coroutine2:$i"
//                    println("launch, name$name, thread: ${Thread.currentThread().name}")
                    withContext(name) {
                //                        println("span2, name$name, thread: ${Thread.currentThread().name}")
                        assertEquals(name, ContextManager.INSTANCE.activeSpan().name)
                        val child = Tracer.startSpan("child in coroutine2:$i")
                        assertEquals(ContextManager.INSTANCE.activeSpan().spanId, child.parentSpanId)

                        launch {
                            val child1 = Tracer.startSpan("child in nested coroutines")
                            assertEquals(ContextManager.INSTANCE.activeSpan().traceId, child1.traceId)
                        }

                        assertEquals(name, ContextManager.INSTANCE.activeSpan().name)
                    }
                }
            }
        }
    }

    @Test
    fun withinCoroutineScopeTest() {
        runBlocking {
            withContext("start") {
                assertEquals("start", ContextManager.INSTANCE.activeSpan().name)
                val startTraceId = ContextManager.INSTANCE.activeSpan().traceID

                val span1 = Tracer.startSpan("span1")
                assertEquals(startTraceId, span1.traceID)

                withinSpan("span2", active = false) {
                    val span3 = Tracer.startSpan("span3")
                    assertEquals(startTraceId, span3.traceID)
                }

                withinSpan("span4") {
                    assertNotEquals("start", ContextManager.INSTANCE.activeSpan().name)
                    val span5 = Tracer.startSpan("span5")
                    assertEquals(startTraceId, span5.traceID)
                }

//                val span6 = Tracer.startSpan("span6")
                launch {
                    withContext("nested coroutine scope") {
                        assertEquals("nested coroutine scope", ContextManager.INSTANCE.activeSpan().name)
                        assertEquals(startTraceId, ContextManager.INSTANCE.activeSpan().traceID)
                    }
                }
            }
        }
    }

}