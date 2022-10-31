package com.aliyun.sls.android.producer.example.example

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aliyun.sls.android.okhttp.OKHttp3Tracer
import com.aliyun.sls.android.ot.*
import com.aliyun.sls.android.producer.example.R
import com.aliyun.sls.android.trace.Tracer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class TraceDemoKotlinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_trace_ktx_demo)

        findViewById<View>(R.id.trace_ktx_runblocking).setOnClickListener {
            runBlocking()
        }
        findViewById<View>(R.id.trace_ktx_launch).setOnClickListener {
            launch()
        }
        findViewById<View>(R.id.trace_ktx_with_context).setOnClickListener {
            withContextDemo()
        }
        findViewById<View>(R.id.trace_ktx_async).setOnClickListener {
            async()
        }
        findViewById<View>(R.id.trace_ktx_await).setOnClickListener {
            await()
        }
        findViewById<View>(R.id.trace_ktx_demo_start_engine).setOnClickListener {
            startEngineDemo()
        }
    }

    private fun runBlocking() {
        withRunBlocking("run blocking") {
            Tracer.startSpan("blocking start span").end()

            withAsync("start async") {
                simpleTest("runBlocking")
            }.withAwait("start await")

            Tracer.startSpan("blocking end span").end()
        }
    }

    private fun launch() {
        withCoroutineScope("in launch")
            .withLaunch("running launch") {
                simpleTest("launch")
            }
    }

    private fun withContextDemo() {
        withRunBlocking("in with context") {
            contextDemo()
        }
    }

    private suspend fun contextDemo() {
        withContext("running with context") {
            simpleTest("withContext")
        }
    }

    private fun async() {
        withCoroutineScope("in async")
            .withAsync("running async") {
                simpleTest("async")
            }
    }

    private fun await() {
        withRunBlocking("in await") {
            withAsync("running async") {
                simpleTest("await")
            }.withAwait("running await")
        }
    }

    private fun startEngineDemo() {
        val combined = withCoroutineScope("启动引擎")
        // 状态检查
        combined.withLaunch("1. 状态检查") {
            when (checkPowerStatus()) {
                true -> Tracer.startSpan("电源状态正常").end()
                false -> Tracer.startSpan("电源状态异常").setStatus(Span.StatusCode.ERROR).end()
            }

            when (checkOilPressureStatus()) {
                true -> Tracer.startSpan("油压状态正常").end()
                false -> Tracer.startSpan("油压状态异常").setStatus(Span.StatusCode.ERROR).end()
            }

            when (checkTirePressureStatus()) {
                true -> Tracer.startSpan("胎压状态正常").end()
                false -> Tracer.startSpan("胎压状态异常").setStatus(Span.StatusCode.ERROR).end()
            }
        }

        // 引擎启动
        combined.withLaunch("2. 启动引擎") {
            when (startEngine()) {
                true -> Tracer.startSpan("引擎启动成功").end()
                false -> Tracer.startSpan("引擎启动失败").setStatus(Span.StatusCode.ERROR).end()
            }
        }

        // 上报状态
        combined.withLaunch("3. 上报状态") {
            withAsync("状态上报中") {
                val result = try {
                    httpRequest()
                } catch (e: Throwable) {
                    print(e)
                }
            }.withAwait("等待状态上报完成")
        }
    }


    private suspend fun simpleTest(fnName: String) {
        Tracer.startSpan("coroutine $fnName start").end()
        withinSpan("working 1 in $fnName ....") {
            delay(1000)
        }

        withinSpan("working 2 in $fnName ....") {
            delay(1000)
        }

        withinSpan("working 3 in $fnName ....") {
            delay(1000)
        }
        Tracer.startSpan("coroutine $fnName end").end()
    }

    private suspend fun checkPowerStatus(): Boolean {
        return withContext("检查电源状态", context = Dispatchers.IO) {
            checkPowerVoltageStatus() && checkPowerElectricityStatus()
        }
    }

    private suspend fun checkPowerVoltageStatus(): Boolean {
        return withinSpan("检查电压状态") {
            delay(300)
            true
        }
    }

    private suspend fun checkPowerElectricityStatus(): Boolean {
        return withinSpan("检查电流状态") {
            delay(500)
            true
        }
    }

    private suspend fun checkOilPressureStatus(): Boolean {
        return withContext("检查油压状态", context = Dispatchers.IO) {
            true
        }
    }

    private fun checkTirePressureStatus(): Boolean {
        return true
    }

    private suspend fun startEngine(): Boolean {
        return withContext("启动引擎") {
            withAsync("引擎启动中") {
                delay(1000)
                true
            }.withAwait("等待引擎启动完成")
        }
    }

    private suspend fun httpRequest(): String? {
        withinSpan("http request") {
            delay(1000)
            val response: Response = OKHttp3Tracer.newCallFactory(OkHttpClient.Builder().build()).newCall(
                Request.Builder()
                    .url("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com/catalogue")
                    .build()
            ).execute()

            return response.body()?.string()
        }
    }

    companion object {
        private const val TAG = "TraceDemoKotlinActivity"
    }

}