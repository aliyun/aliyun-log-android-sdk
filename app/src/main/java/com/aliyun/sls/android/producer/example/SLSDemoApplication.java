package com.aliyun.sls.android.producer.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import androidx.multidex.MultiDexApplication;
import com.aliyun.sls.android.core.SLSAndroid;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.configuration.Credentials.NetworkDiagnosisCredentials;
import com.aliyun.sls.android.core.configuration.Credentials.TracerCredentials;
import com.aliyun.sls.android.core.configuration.Credentials.TracerCredentials.TracerLogCredentials;
import com.aliyun.sls.android.core.configuration.UserInfo;
import com.aliyun.sls.android.core.sender.Sender.Callback;
import com.aliyun.sls.android.okhttp.OKHttp3InstrumentationDelegate;
import com.aliyun.sls.android.okhttp.OKHttp3Tracer;
import com.aliyun.sls.android.okhttp.OKHttp3TracerInterceptor;
import com.aliyun.sls.android.okhttp.OkHttp3Configuration;
import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.ISpanProvider;
import com.aliyun.sls.android.ot.Resource;
import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.producer.LogProducerResult;
import com.aliyun.sls.android.producer.example.utils.PreferenceUtils;
import okhttp3.Request;

/**
 * @author gordon
 * @date 2021/08/31
 */
public class SLSDemoApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        SLSGlobal.application = this;
        SLSGlobal.applicationContext = this.getApplicationContext();

        if (BuildConfig.CONFIG_ENABLE) {
            PreferenceUtils.overrideConfig(this);
        }

        Credentials credentials = new Credentials();
        credentials.instanceId = "androd-dev-f1a8";
        credentials.endpoint = "https://cn-hangzhou.log.aliyuncs.com";
        credentials.project = "yuanbo-test-1";
        //credentials.accessKeyId = PreferenceUtils.getAccessKeyId(this);
        //credentials.accessKeySecret = PreferenceUtils.getAccessKeySecret(this);
        //credentials.securityToken = PreferenceUtils.getAccessKeyToken(this);

        TracerCredentials tracerCredentials = credentials.createTraceCredentials();
        //tracerCredentials.instanceId = "sls-mall";
        tracerCredentials.endpoint = "https://cn-beijing.log.aliyuncs.com";
        tracerCredentials.project = "qs-demos";
        // 自定义 Trace Logs 的写入位置
        //TracerLogCredentials logCredentials = tracerCredentials.createLogCredentials();
        //logCredentials.endpoint = "https://cn-beijing.log.aliyuncs.com";
        //logCredentials.project = "qs-demos";
        //logCredentials.logstore = "sls-mall-custom-logs";

        NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.getNetworkDiagnosisCredentials();
        networkDiagnosisCredentials.secretKey = PreferenceUtils.getNetworkSecKey(this);
        networkDiagnosisCredentials.endpoint = "https://cn-hangzhou.log.aliyuncs.com";
        networkDiagnosisCredentials.project = "zaiyun-test5";

        SLSAndroid.setUtdid(this, "123123131232");
        SLSAndroid.setLogLevel(Log.VERBOSE);
        SLSAndroid.initialize(
            this,
            credentials,
            configuration -> {
                configuration.debuggable = true;
                configuration.spanProvider = new ISpanProvider() {
                    @Override
                    public Resource provideResource() {
                        return Resource.of("other_resource_key", "other_resource_value");
                    }

                    @Override
                    public List<Attribute> provideAttribute() {
                        List<Attribute> attributes = new ArrayList<>();
                        attributes.add(Attribute.of("other_attribute_key", "other_attribute_value"));
                        return attributes;
                    }
                };

                //configuration.enableCrashReporter = true;
                configuration.enableNetworkDiagnosis = true;
                configuration.enableTracer = true;
                configuration.enableTracerLog = true;


                UserInfo info = new UserInfo();
                info.uid = "123321";
                info.channel = "dev";
                info.addExt("ext_key", "ext_value");
                configuration.userInfo = info;
            }
        );
        SLSAndroid.setExtra("extra_key", "extra_value");
        SLSAndroid.setExtra("extra_key2", "extra_value2");
        SLSAndroid.registerCredentialsCallback(new Callback() {
            @Override
            public void onCall(String feature, LogProducerResult result) {
                SLSLog.v("DEBUGGGG", "feature: " + feature + ", result: " + result);
                if (LogProducerResult.LOG_PRODUCER_SEND_UNAUTHORIZED == result ||
                    LogProducerResult.LOG_PRODUCER_PARAMETERS_INVALID == result) {
                    // 处理token过期，AK失效等鉴权类问题
                    Credentials credentials = new Credentials();
                    credentials.accessKeyId = PreferenceUtils.getAccessKeyId(SLSDemoApplication.this);
                    credentials.accessKeySecret = PreferenceUtils.getAccessKeySecret(SLSDemoApplication.this);
                    credentials.securityToken = PreferenceUtils.getAccessKeyToken(SLSDemoApplication.this);

                    TracerCredentials tracerCredentials = credentials.createTraceCredentials();
                    tracerCredentials.instanceId = "sls-mall";

                    TracerLogCredentials logCredentials = tracerCredentials.createLogCredentials();
                    logCredentials.endpoint = "https://cn-beijing.log.aliyuncs.com";
                    logCredentials.project = "qs-demos";
                    logCredentials.logstore = "sls-mall-custom-logs";

                    SLSAndroid.setCredentials(credentials);
                }
            }
        });
        OKHttp3Tracer.registerOKHttp3InstrumentationDelegate(new OKHttp3InstrumentationDelegate() {
            @Override
            public Map<String, String> injectCustomHeaders(Request request) {
                Map<String, String> headers = new HashMap<>();
                headers.put("h_key_1", "h_value_1");
                headers.put("h_key_2", "h_value_2");
                return headers;
            }

            @Override
            public String nameSpan(Request request) {
                // 自定义 http request span 的名称
                return request.method() + " " + request.url().encodedPath();
            }

            @Override
            public void customizeSpan(Request request, Span span) {
                // 自定义 http request span
                span.setService(request.url().encodedPath());
            }

            @Override
            public boolean shouldInstrument(Request request) {
                final String host = request.url().url().getHost();
                // 只有request符合预期时才植入trace信息
                return !host.contains("log.aliyuncs.com");
            }
        });

        final OkHttp3Configuration configuration = new OkHttp3Configuration();
        // 允许采集 http request header 信息，默认不采集
        configuration.captureHeaders = true;
        // 允许采集 http request body 信息，默认不采集
        configuration.captureBody = true;
        // 允许采集 http response 信息，默认不采集
        configuration.captureResponse = true;
        OKHttp3Tracer.updateOkHttp3Configuration(configuration);

        //OKHttp3TracerInterceptor okHttp3TracerInterceptor = new OKHttp3TracerInterceptor();
        //okHttp3TracerInterceptor.registerOKHttp3InstrumentationDelegate(new OKHttp3InstrumentationDelegate() {
        //    @Override
        //    public Map<String, String> injectCustomHeaders(Request request) {
        //        // 返回自定义Header信息，该信息会插入到http request header 中
        //        return null;
        //    }
        //
        //    @Override
        //    public boolean shouldInstrument(Request request) {
        //        // 是否对当前请求插入trace信息，返回true表示插入trace信息
        //        // 如下表示对host中包含log.aliyuncs.com的请求都插入trace信息
        //        return request.url().host().contains("log.aliyuncs.com");
        //    }
        //});
    }
}
