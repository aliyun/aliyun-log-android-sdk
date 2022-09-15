package com.aliyun.sls.android.producer.example.example;

import java.io.IOException;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.aliyun.sls.android.okhttp.OKHttp3Tracer;
import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.Resource;
import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.Span.StatusCode;
import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.example.R;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient.ApiCallback;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.example.example.trace.model.OrderModel;
import com.aliyun.sls.android.trace.Tracer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author gordon
 * @date 2022/9/9
 */
public class TraceDemoActivity extends AppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_demo);

        findViewById(R.id.trace_demo_startup).setOnClickListener(this);
        findViewById(R.id.trace_demo_air).setOnClickListener(this);
        findViewById(R.id.trace_simple_demo).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (R.id.trace_demo_startup == v.getId()) {
            startup();
        } else if (R.id.trace_demo_air == v.getId()) {
            openAirConditioner();
        } else if (R.id.trace_simple_demo == v.getId()) {
            simpleTraceDemo();
        }
    }

    // region simple trace demo
    private void simpleTraceDemo() {
        // single span
        Span span = Tracer.startSpan("span 1");
        span.addAttribute(Attribute.of("attr_key", "attr_value"))
            .addResource(Resource.of("res_key", "res_value"));
        span.end();

        // span with children
        span = Tracer.startSpan("span with children", true);
        Tracer.startSpan("child span 1").end();
        Tracer.startSpan("child span 2").end();
        span.end();

        // span with children (SpanBuilder)
        span = Tracer.spanBuilder("span with children (SpanBuilder)")
            .setActive(true)
            .addResource(Resource.of("res_key", "res_value"))
            .build();
        Tracer.startSpan("child span 1 (SpanBuilder)").end();
        Tracer.startSpan("child span 2 (SpanBuilder)").end();
        span.end();

        // span with function block
        Tracer.withinSpan("span with func block", new Runnable() {
            @Override
            public void run() {
                Tracer.startSpan("span within func block 1").end();
                Tracer.withinSpan("nested span with func block", new Runnable() {
                    @Override
                    public void run() {
                        Tracer.startSpan("nested span 1").end();
                        Tracer.startSpan("nested span 2").end();
                        // NPE
                        String s = null;
                        s.length();
                    }
                });
                Tracer.startSpan("span within func block 2").end();
            }
        });

        // http request with traceid
        Tracer.withinSpan("span with http request func", new Runnable() {
            @Override
            public void run() {
                OKHttp3Tracer.newCallFactory(new OkHttpClient.Builder().build()).newCall(
                    new Request.Builder()
                        .url("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com/catalogue")
                        .build()
                ).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                        }
                    }
                );
            }
        });
    }
    // endregion

    // region 启动引擎
    private void startup() {
        new Thread(() -> Tracer.withinSpan("执行启动引擎操作", true, () -> {
            // step1: connect power
            connectPower();

            // step2: start engine
            engineStart();

            // step3: report status to server
            reportStartupStatus();
        })).start();
    }

    private void connectPower() {
        Tracer.withinSpan("第一步：接通电源", this::threadSleep);
        Tracer.withinSpan("子步骤1：电气系统自检", true, () -> {
            Tracer.withinSpan("1.1 电池电压检查", this::threadSleep);
            Tracer.withinSpan("1.2 电气信号检查", this::threadSleep);
        });
    }

    private void engineStart() {
        Tracer.withinSpan("第二步：启动引擎", this::threadSleep);
    }

    private void reportStartupStatus() {
        Tracer.spanBuilder("第三步：上报状态").build().end();
        ApiClient.getCategory(new ApiCallback<List<ItemModel>>() {
            @Override
            public void onSuccess(List<ItemModel> itemModels) {

            }

            @Override
            public void onError(int code, String error) {

            }
        });
    }
    // endregion

    // region 打开空调
    private void openAirConditioner() {
        new Thread(() -> Tracer.withinSpan("执行打开空调操作", true, new Runnable() {
            @Override
            public void run() {
                connectAirPower();
                startFan();
                reportOpenAirConditionerStatus();
            }
        })).start();
    }

    private void connectAirPower() {
        Tracer.withinSpan("开空调：第一步：接通电源", this::threadSleep);
        Tracer.withinSpan("开空调：子步骤1：电气系统自检", true, () -> {
            Tracer.withinSpan("开空调：1.1 电池检查", true, () -> {
                Tracer.startSpan("电池电压检查").end();
                threadSleep();
                Span span = Tracer.startSpan("电池电流检查");
                span.setStatus(StatusCode.of("电池电流检查异常"));
                span.end();
                threadSleep();
                Tracer.startSpan("电池温度检查").end();

                Log log = new Log();
                log.putContent("content", "状态检查正常");
                Tracer.log(log);
            });
            Tracer.withinSpan("开空调：1.2 电气信号检查", this::threadSleep);
        });
    }

    private void startFan() {
        Span span = Tracer.startSpan("开空调：第二步：启动风扇");
        threadSleep();
        span.end();
    }

    private void reportOpenAirConditionerStatus() {
        ApiClient.getOrders(new ApiCallback<List<OrderModel>>() {
            @Override
            public void onSuccess(List<OrderModel> orderModels) {

            }

            @Override
            public void onError(int code, String error) {

            }
        });
    }
    // endregion

    private void threadSleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
