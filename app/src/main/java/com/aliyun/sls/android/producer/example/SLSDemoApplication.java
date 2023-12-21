package com.aliyun.sls.android.producer.example;

import java.io.File;
import java.io.IOException;

import android.os.Build;
import android.os.Handler;
import android.util.Log;
import androidx.multidex.MultiDexApplication;
import com.aliyun.sls.android.core.SLSAndroid;
import com.aliyun.sls.android.core.SLSAndroid.OptionConfiguration;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.configuration.Credentials.NetworkDiagnosisCredentials;
import com.aliyun.sls.android.core.utdid.Utdid;
import com.aliyun.sls.android.crashreporter.CrashReporter;
import com.aliyun.sls.android.exporter.otlp.OtlpSLSSpanExporter;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.TcpPingRequest;
import com.aliyun.sls.android.network_diagnosis.NetworkDiagnosis;
import com.aliyun.sls.android.okhttp.instrumentation.OkHttpConfiguration;
import com.aliyun.sls.android.otel.common.AccessKey;
import com.aliyun.sls.android.otel.common.ConfigurationManager;
import com.aliyun.sls.android.otel.common.Environment;
import com.aliyun.sls.android.otel.common.Workspace;
import com.aliyun.sls.android.producer.BuildConfig;
import com.aliyun.sls.android.producer.LogProducerResult;
import com.aliyun.sls.android.producer.example.example.ExampleHelper;
import com.aliyun.sls.android.producer.example.example.ExampleHelper.Device;
import com.aliyun.sls.android.producer.example.example.ExampleHelper.NetworkLink;
import com.aliyun.sls.android.producer.example.utils.PreferenceUtils;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

        final String deviceId = ExampleHelper.Device.getDeviceId(getApplicationContext());
        Utdid.getInstance().setUtdid(getApplicationContext(), deviceId);

        if (BuildConfig.CONFIG_ENABLE) {
            PreferenceUtils.overrideConfig(this);
        }

        // 初始化配置管理器。配置管理器主要用来处理AK过期，参数动态更新
        ConfigurationManager.getInstance().setProvider(
            // 处理AK过期
            scope -> {
                if ("ipa".equalsIgnoreCase(scope)) {
                    return AccessKey.accessKey(
                        BuildConfig.ACCESS_KEYID2,
                        BuildConfig.ACCESS_KEY_SECRET2,
                        BuildConfig.ACCESS_KEY_TOKEN2
                    );
                }

                return AccessKey.accessKey(
                    BuildConfig.ACCESS_KEYID,
                    BuildConfig.ACCESS_KEY_SECRET,
                    BuildConfig.ACCESS_KEY_TOKEN
                );
            },
            // 动态参数更新
            scope -> {
                if ("ipa".equalsIgnoreCase(scope)) {
                    return Workspace.workspace(
                        "https://cn-shanghai.log.aliyuncs.com",
                        "network-analyzer-sls",
                        "ipa-zzsdmuwj6yydwrhcarwmvx-raw"
                    );
                }

                return Workspace.workspace(
                    BuildConfig.UEM_ENDPOINT,
                    BuildConfig.UEM_PROJECT,
                    BuildConfig.UEM_INSTANCEID
                );
            },
            // 环境信息
            scope -> {
                Environment environment = Environment.environment();
                environment.setEnv("dev"); // 环境
                environment.setUid("123456789"); // 用户id
                environment.setChannel("official"); // 渠道
                // 可选
                //environment.setUtdid(sDeviceId); // 设备id
                return environment;
            }
        );

        // 初始化用户体验监控
        CrashReporter.init(this, true);

        // 初始化网络质量分析器
        Credentials credentials = new Credentials();
        credentials.accessKeyId = BuildConfig.ACCESS_KEYID;
        credentials.accessKeySecret = BuildConfig.ACCESS_KEY_SECRET;
        credentials.securityToken = BuildConfig.ACCESS_KEY_TOKEN;

        NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.getNetworkDiagnosisCredentials();
        networkDiagnosisCredentials.secretKey = BuildConfig.NETWORK_SECKEY;
        networkDiagnosisCredentials.endpoint = "https://cn-shanghai.log.aliyuncs.com";
        networkDiagnosisCredentials.project = "network-analyzer-sls";

        SLSAndroid.setLogLevel(Log.VERBOSE);
        final OptionConfiguration optionConfiguration = configuration -> {
            configuration.debuggable = true;
            configuration.enableNetworkDiagnosis = true;
        };

        // 预初始化，功能可正常使用，但敏感信息不会采集
        SLSAndroid.preInit(this, credentials, optionConfiguration);

        // 10 秒后完整初始化，模拟获得用户授权
        new Handler(getMainLooper()).postDelayed(
            () -> SLSAndroid.initialize(SLSDemoApplication.this, credentials, optionConfiguration),
            1 * 1000
        );

        SLSAndroid.setExtra("extra_key", "extra_value");
        SLSAndroid.setExtra("extra_key2", "extra_value2");
        SLSAndroid.registerCredentialsCallback((feature, result) -> {
            SLSLog.v("DEBUGGGG", "feature: " + feature + ", result: " + result);
            if (LogProducerResult.LOG_PRODUCER_SEND_UNAUTHORIZED == result ||
                LogProducerResult.LOG_PRODUCER_PARAMETERS_INVALID == result) {
                // 处理token过期，AK失效等鉴权类问题
                Credentials credentials1 = new Credentials();
                credentials1.accessKeyId = PreferenceUtils.getAccessKeyId(SLSDemoApplication.this);
                credentials1.accessKeySecret = PreferenceUtils.getAccessKeySecret(SLSDemoApplication.this);
                credentials1.securityToken = PreferenceUtils.getAccessKeyToken(SLSDemoApplication.this);

                // 如果是仅更新 AK 的话，可以不对NetworkDiagnosisCredentials进行更新
                //NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials
                // .getNetworkDiagnosisCredentials();
                //networkDiagnosisCredentials.endpoint = "https://cn-shanghai.log.aliyuncs.com";
                //networkDiagnosisCredentials.project = "network-analyzer-sls";

                SLSAndroid.setCredentials(credentials1);
            }
        });

        //redirectLog();

        initOTel();
        //new Thread(() -> httpRequest()).start();
        new NetworkLink().start();
    }

    private void redirectLog() {
        final File logFile = new File(getCacheDir() + "/logfile.log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String cmd = "logcat -b all -f " + logFile.getAbsolutePath() + "\n";
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void httpRequest() {
        io.opentelemetry.api.trace.Span span = GlobalOpenTelemetry.getTracer("Android")
            .spanBuilder("httpRequest")
            .startSpan();

        try (Scope ignored = span.makeCurrent()) {
            try {
                new OkHttpClient.Builder().build().newCall(
                    new Request.Builder()
                        .url("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com/catalogue")
                        .build()
                ).execute();
            } catch (IOException e) {
                span.recordException(e);

                TcpPingRequest request = new TcpPingRequest();
                request.domain = "sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com";
                request.port = 80;
                NetworkDiagnosis.getInstance().tcpPing(request);
            }
        } finally {
            span.end();
        }
    }

    private void initOTel() {
        //OtlpGrpcSpanExporter grpcSpanExporter = OtlpGrpcSpanExporter.builder()
        //    .setEndpoint("https://cn-beijing.log.aliyuncs.com:10010")
        //    .addHeader("x-sls-otel-project", "qs-demos")
        //    .addHeader("x-sls-otel-instance-id", "sls-mall")
        //    .addHeader("x-sls-otel-ak-id", PreferenceUtils.getAccessKeyId(this))
        //    .addHeader("x-sls-otel-ak-secret", PreferenceUtils.getAccessKeySecret(this))
        //    .build();

        OtlpSLSSpanExporter exporter = OtlpSLSSpanExporter.builder()
            .setScope("trace")
            .setEndpoint(BuildConfig.UEM_ENDPOINT)
            .setProject(BuildConfig.UEM_PROJECT)
            .setLogstore(BuildConfig.UEM_INSTANCEID + "-traces")
            .setAccessKey(PreferenceUtils.getAccessKeyId(this), PreferenceUtils.getAccessKeySecret(this), null)
            .build();

        SdkTracerProviderBuilder builder = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
            .setResource(io.opentelemetry.sdk.resources.Resource.create(Attributes.builder()
                .put(ResourceAttributes.SERVICE_NAME, "Android Demo App")
                .put(ResourceAttributes.SERVICE_NAMESPACE, "Android")
                .put(ResourceAttributes.SERVICE_VERSION, BuildConfig.VERSION_NAME)
                .put(ResourceAttributes.HOST_NAME, Build.HOST)
                .put(ResourceAttributes.OS_NAME, "Android")
                .put(ResourceAttributes.OS_TYPE, "Android")
                .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, "dev")
                .put(ResourceAttributes.DEVICE_ID, Utdid.getInstance().getUtdid(this))
                .build()));

        NetworkDiagnosis.getInstance().setupTracer(builder);

        SdkTracerProvider tracerProvider = builder.build();

        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();

        NetworkDiagnosis.getInstance().setOpenTelemetrySdk(GlobalOpenTelemetry.get());

        OkHttpConfiguration.setOpenTelemetry(GlobalOpenTelemetry.get());
        OkHttpConfiguration.setCaptureRequestHeaders(true);
        OkHttpConfiguration.setCaptureResponseHeaders(true);
        OkHttpConfiguration.setCaptureRequestBody(true);
        OkHttpConfiguration.setCaptureResponseBody(true);
        OkHttpConfiguration.addAttributesExtractor(
            new AttributesExtractor<Request, Response>() {
                @Override
                public void onStart(AttributesBuilder attributes, Context parentContext, Request request) {

                }

                @Override
                public void onEnd(AttributesBuilder attributes, Context context, Request request, Response response,
                    Throwable error) {

                }
            }
        );
    }
}
