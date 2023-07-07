package com.aliyun.sls.android.producer.example.example

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aliyun.sls.android.okhttp.OKHttp3Tracer
import com.aliyun.sls.android.ot.*
import com.aliyun.sls.android.producer.example.R
import com.aliyun.sls.android.trace.Tracer
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlin.coroutines.EmptyCoroutineContext


class TraceDemoKotlinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_trace_ktx_demo)

        findViewById<View>(R.id.trace_ktx_runblocking).setOnClickListener {
            runBlocking()
        }
        findViewById<View>(R.id.trace_ktx_launch).setOnClickListener {
            launchDemo()
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
//            test()
        }
        findViewById<View>(R.id.trace_ktx_demo_start_air_conditioner).setOnClickListener {
            openAirConditionerDemo()
        }
        findViewById<View>(R.id.trace_ktx_demo_crash).setOnClickListener {
            val scope = withCoroutineScope("下单崩溃-场景关联")
            scope.launch { crashDemo() }
        }
//        running()
    }

    private fun running() {
        val scope = CoroutineScope(EmptyCoroutineContext)

        scope.async {
            delay(10)
            running()
        }
    }

    val scope = CoroutineScope(Dispatchers.Default)
    var index: Int = 0
    private fun test() {
        scope.withLaunch("test") {
            index += 1
            log("test start, index: $index")
            delay(500)
            log("test after delay(500), index: $index")
            suspendRequest()
            log("test end, index: $index")
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

    private fun launchDemo() {
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

    private fun openAirConditionerDemo() {
        val remoteSpan = Tracer.startSpan("收到指令  <<== 远程打开空调").addLink(Link.create("00000015386363220000001465262928", "0000001362508910"))
        val scope = withCoroutineScope("远程启动空调", remoteSpan)

        scope.withLaunch("1. 状态检查") {
            when(checkPowerStatus()) {
                true -> Tracer.startSpan("电源状态正常").end()
                false -> Tracer.startSpan("电源状态异常").setStatus(Span.StatusCode.ERROR).end()
            }
        }

        scope.withLaunch("2. 打开空调") {
            when(openAirConditioner()) {
                true -> Tracer.startSpan("空调打开成功").end()
                false -> Tracer.startSpan("空调打开失败").setStatus(Span.StatusCode.ERROR).end()
            }
        }

        scope.withLaunch("3. 上报状态") {
            withAsync("状态上报中") {
                val result = try {
                    httpRequest()
                    remoteSpan.end()
                } catch (e: Throwable) {
                    print(e)
                }
            }.withAwait("等待状态上报完成")
        }
    }

    private suspend fun crashDemo() = coroutineScope {
        withLaunch("去下单") {
            Tracer.startSpan("检查商品信息").end()
            Tracer.startSpan("检查店铺信息").end()
            Tracer.startSpan("构造下单参数").end()
            withAsync("请求下单接口") {
                delay(600)
                val str = ""
                str.subSequence(0, 10)
            }.await()
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

    private suspend fun openAirConditioner(): Boolean {
        return coroutineScope {
            withAsync("空调启动中") {
                delay(2000)
                true
            }.withAwait("等待空调启动完成")
        }
    }

    private suspend fun httpRequest(): String? {
        withinSpan("http request") {
            delay(1000)
//            val response: Response = OKHttp3Tracer.newCallFactory(OkHttpClient.Builder().build()).newCall(
            val response: Response = OkHttpClient.Builder().build().newCall(
                Request.Builder()
                    .url("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com/catalogue")
                    .build()
            ).execute()

            return response.body?.string()
        }
    }

    private suspend fun suspendRequest() : List<Catalogue>? {
        return coroutineScope {
            withContext("request from suspendRequest") {
                retrofitHttpRequest()
            }
        }
//        return launch {
//            withContext("request from suspendRequest") {
//                retrofitHttpRequest()
//            }
//        }

    }

    val api = retrofit.create(Api::class.java)
    private suspend fun retrofitHttpRequest(): List<Catalogue>? {
        api.catalogue().enqueue(object : Callback<List<Catalogue>> {
            override fun onResponse(call: Call<List<Catalogue>>?, response: retrofit2.Response<List<Catalogue>>?) {
                response?.body()
            }

            override fun onFailure(call: Call<List<Catalogue>>?, t: Throwable?) {
                t?.printStackTrace()
            }
        })
//        return try {
//            api.catalogue().execute().body()
//        } catch (e: Throwable) {
//            e.printStackTrace()
//            null
//        }
        return null
    }

    companion object {
        private const val TAG = "TraceDemoKotlinActivity"
        private val retrofit = Retrofit.Builder()
            .baseUrl("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com")
//            .callFactory(OKHttp3Tracer.newCallFactory(OkHttpClient.Builder().build()))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private fun log(msg: String) {
            println("[${Thread.currentThread().name}] $TAG $msg")
        }
    }

    data class Catalogue(
        var id: String?,
        var name: String?,
        var description: String?,
        var price: Double?,
        var count: Int
    )

    interface Api {
        //http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com/catalogue
        @GET("/catalogue")
        fun catalogue(): Call<List<Catalogue>>
    }

}