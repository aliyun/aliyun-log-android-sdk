package com.aliyun.sls.android.producer.example.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

//import android.content.Context;
import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.utdid.Utdid;
import com.aliyun.sls.android.crashreporter.CrashReporter;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.HttpRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.MtrRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.PingRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.TcpPingRequest;
import com.aliyun.sls.android.network_diagnosis.NetworkDiagnosis;
import com.aliyun.sls.android.producer.utils.ThreadUtils;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author yulong.gyl
 * @date 2023/12/20
 */
public class ExampleHelper {
    private static final String TAG = "ExampleHelper";

    private static class Constants {
        static final String V2X_CONTROL_URL =
            "http://sls-mall.cfa82911e541341a1b9d21d527075cbfe.cn-hangzhou.alicontainer"
                + ".com/v2x/control?controlId=xxxxxx&method=open";
    }

    private static class Utils {
        private static Random random = new Random();

        public static boolean randomSucc(double rate) {
            double d = random.nextDouble();
            boolean succ = d > rate;
            return succ;
        }
    }

    public static class NetworkLink {
        private static final String ERROR_URL
            = "http://sls-mall.cfa82911e541341a1b9d21d527075cbff.cn-hangzhou.alicontainer.com";

        public void start() {
            // 子线程链接游戏
            new Thread(() -> connect()).start();
        }

        private boolean error = false;
        private int errorToken = 0;
        private final Random random = new Random();

        private void connect() {
            Tracer tracer = GlobalOpenTelemetry.get().getTracer("network_example");

            error = random.nextInt(5) >= 3;
            //error = true;
            errorToken = random.nextInt(6);
            //errorToken = 1;
            SLSLog.d(TAG, "connect. error index: " + error + ", token: " + errorToken);

            final Span span = tracer.spanBuilder("玩家连接").startSpan();
            io.opentelemetry.context.Context.current().with(span).makeCurrent();
            try (Scope ignored = span.makeCurrent()) {
                boolean shouldNext = login();
                if (!shouldNext) {
                    return;
                }
                shouldNext = connectProxy();
                if (!shouldNext) {
                    return;
                }

                shouldNext = downloadCdn();
                if (!shouldNext) {
                    return;
                }

                shouldNext = downloadChf();
                if (!shouldNext) {
                    return;
                }

                shouldNext = connectGate();
                if (!shouldNext) {
                    return;
                }

                connectIM();
            } finally {
                span.end();
            }
        }

        private boolean login() {
            return httpRequest("玩家登录账号", 1, 0);
        }

        private boolean connectProxy() {
            return httpRequest("连接网关", 2, 1);
        }

        private boolean downloadCdn() {
            return httpRequest("下载CDN资源", 0, 2);
        }

        private boolean downloadChf() {
            return httpRequest("下载游戏CHF资源", 0, 3);
        }

        private boolean connectGate() {
            return httpRequest("进入游戏", 3, 4);
        }

        private boolean connectIM() {
            return httpRequest("连接IM", 2, 5);
        }

        private boolean httpRequest(String bizName, int type, int token) {
            io.opentelemetry.api.trace.Span span = GlobalOpenTelemetry.getTracer("Android")
                .spanBuilder(bizName)
                .startSpan();

            String url
                = "http://sls-mall.cfa82911e541341a1b9d21d527075cbfe.cn-hangzhou.alicontainer"
                + ".com/mall/api/productcategory/list?limit=100&name=&offset=0";
            final boolean error = this.error && this.errorToken == token;
            if (error) {
                url = ERROR_URL;
            }

            try (Scope ignored = span.makeCurrent()) {
                return request(url, new Request.Builder().url(url).build(), span, type);
            } finally {
                span.end();
            }
        }

        private boolean request(final String url, Request httpRequest, Span span, int type) {
            try {
                Response res = new OkHttpClient.Builder().build().newCall(httpRequest).execute();
                if (res.code() % 200 != 0) {
                    throw new IOException(res.body().string());
                }

                return true;
            } catch (IOException e) {
                span.recordException(e);
                CrashReporter.reportException("接口调用失败", e, new HashMap<String, String>() {
                    {
                        put("url", url);
                        put("type", type + "");
                    }
                });
                return false;
            } finally {
                // 延迟探测
                if (type == 0) {
                    PingRequest request = new PingRequest();
                    request.domain = url;
                    NetworkDiagnosis.getInstance().ping(request);
                } else if (type == 1) {
                    // http 探测
                    HttpRequest request = new HttpRequest();
                    request.domain = url;
                    request.downloadBytesLimit = 1024;
                    NetworkDiagnosis.getInstance().http(request);
                } else if (type == 2) {
                    // tcp 探测
                    TcpPingRequest request = new TcpPingRequest();
                    request.domain = url;
                    request.port = 80;
                    NetworkDiagnosis.getInstance().tcpPing(request);
                } else if (type == 3) {
                    // mtr 探测
                    MtrRequest request = new MtrRequest();
                    request.domain = url;
                    NetworkDiagnosis.getInstance().mtr(request);
                }
            }
        }
    }

    public static class Device {
        private static final String[] sDeviceIds = {
            "5a3b4c6dc7e84f9g01h0ij2k3lmnopqrs",
            "9t0u1v2w3x4y5z6a7b8c9d0e1f2g3h4",
            "5i6j7k8l9m0n1o2p3q4r5s6t7u8v9w0",
            "2x3y4z5a6b7c8d9e0f1g2h3i4j5k6l7m",
            "3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c",
            "9d0e1f2g3h4i5j6k7l8m9n0o1p2q3r4s",
            "5t6u7v8w9x0y1z2a3b4c5d6e7f8g9h0i",
            "3j4k5l6m7n8o9p0q1r2s3t4u5v6w7x8y",
            "9z0a1b2c3d4e5f6g7h8i9j0k1l2m3n4o",
            "5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0",
            "2e3f4g5h6i7j8k9l0m1n2o3p4q5r6s7t",
            "4u5v6w7x8y9z0a1b2c3d4e5f6g7h8i9j",
            "2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z",
            "8a9b0c1d2e3f4g5h6i7j8k9l0m1n2o3p",
            "6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f",
            "4g5h6i7j8k9l0m1n2o3p4q5r6s7t8u9v",
            "2w3x4y5z6a7b8c9d0e1f2g3h4i5j6k7l",
            "7m8n9o0p1q2r3s4t5u6v7w8x9y0z1a2b",
            "3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r",
            "9s0t1u2v3w4x5y6z7a8b9c0d1e2f3g4h",
            "3i4j5k6l7m8n9o0p1q2r3s4t5u6v7w8x",
            "5y6z7a8b9c0d1e2f3g4h5i6j7k8l9m0n",
            "2o3p4q5r6s7t8u9v0w1x2y3z4a5b6c7d",
            "1e2f3g4h5i6j7k8l9m0n1o2p3q4r5s6t",
            "7u8v9w0x1y2z3a4b5c6d7e8f9g0h1i2j",
            "9k0l1m2n3o4p5q6r7s8t9u0v1w2x3y4z",
            "5a6b7c8d9e0f1g2h3i4j5k6l7m8n9o0p",
            "8q9r0s1t2u3v4w5x6y7z8a9b0c1d2e3f",
            "4g5h6i7j8k9l0m1n2o3p4q5r6s7t8u9v",
            "1w2x3y4z5a6b7c8d9e0f1g2h3i4j5k6l"
        };

        public static String getDeviceId(android.content.Context context) {
            Random random = new Random();
            int index = random.nextInt(100);
            if (index < sDeviceIds.length) {
                return sDeviceIds[index];
            }

            return Utdid.getInstance().getUtdid(context);
        }
    }

    private static class RandomIp {
        private static String IP = randomIp();

        public static String randomIp() {
            // ip范围
            int[][] range = {{607649792, 608174079},// 36.56.0.0-36.63.255.255
                {1038614528, 1039007743},// 61.232.0.0-61.237.255.255
                {1783627776, 1784676351},// 106.80.0.0-106.95.255.255
                {2035023872, 2035154943},// 121.76.0.0-121.77.255.255
                {2078801920, 2079064063},// 123.232.0.0-123.235.255.255
                {-1950089216, -1948778497},// 139.196.0.0-139.215.255.255
                {-1425539072, -1425014785},// 171.8.0.0-171.15.255.255
                {-1236271104, -1235419137},// 182.80.0.0-182.92.255.255
                {-770113536, -768606209},// 210.25.0.0-210.47.255.255
                {-569376768, -564133889}, // 222.16.0.0-222.95.255.255
            };

            Random rdint = new Random();
            int index = rdint.nextInt(10);
            String ip = num2ip(range[index][0] + new Random().nextInt(range[index][1] - range[index][0]));
            return ip;
        }

        public static String num2ip(int ip) {
            int[] b = new int[4];
            String x;

            b[0] = (ip >> 24) & 0xff;
            b[1] = (ip >> 16) & 0xff;
            b[2] = (ip >> 8) & 0xff;
            b[3] = ip & 0xff;
            x = b[0] + "." + b[1] + "." + b[2] + "." + b[3];
            return x;
        }

    }

    private static class SpanHelper {
        static SpanBuilder spanBuilder(Tracer tracer, String spanName) {
            return tracer.spanBuilder(spanName).setAttribute("client_ip", RandomIp.IP);
        }
    }

    // 车联网
    public static class V2XDemo {
        private OpenTelemetrySdk sdk;
        private Tracer tracer;
        private V2XVeh veh;

        public void start(android.content.Context context) {
            sdk = OTelHelper.initV2X4Mobile(context);
            tracer = sdk.getTracer("V2X-App");
            veh = new V2XVeh();
            veh.start(context);

            ThreadUtils.exec(() -> openAirConditioner());
        }

        private void openAirConditioner() {
            final Span span = SpanHelper.spanBuilder(tracer, "打开车机空调")
                .setAttribute("control_code", "mobile_user_click")
                .startSpan();
            try (Scope ignored = span.makeCurrent()) {
                if (checkUsePermission()) {
                    sendOpenSignal();
                }
            } catch (Throwable t) {
                span.recordException(
                    t,
                    Attributes.builder()
                        .put("", "")
                        .build()
                );
            } finally {
                span.end();
            }
        }

        private boolean checkUsePermission() {
            boolean succ = Utils.randomSucc(0.1);

            SpanHelper.spanBuilder(tracer, "1. 校验用户权限")
                .setAttribute("control_code", "mobile_check_privilege")
                .startSpan()
                .setStatus(succ ? StatusCode.OK : StatusCode.ERROR, succ ? "" : "用户权限校验失败")
                .end();

            return succ;
        }

        private void sendOpenSignal() {
            final Span span = SpanHelper.spanBuilder(tracer, "2. 发送指令 ==>> 打开空调").setAttribute("control_code",
                "mobile_send_signal").startSpan();
            try (Scope ignored = span.makeCurrent()) {
                Request request = new Request.Builder().url(Constants.V2X_CONTROL_URL).build();
                Response response = new OkHttpClient.Builder().build().newCall(request).execute();
                //tracer.spanBuilder("3. 指令发送成功").setAttribute("control_code", "mobile_signal_sent").startSpan()
                //    .end();

                veh.onOpenSignalReceived(response.header("traceparent"));
            } catch (Throwable e) {
                span.recordException(e);
                span.setStatus(StatusCode.ERROR, "发送控车指令失败");
            } finally {
                span.end();
            }
        }

        private static SpanContext contextFromRemote(String traceparent) {
            final String traceId = traceparent.substring(3, 32);
            final String spanId = traceId.substring(36, 16);
            char firstTraceFlagsChar = traceparent.charAt(53);
            char secondTraceFlagsChar = traceparent.charAt(53 + 1);

            return SpanContext.createFromRemoteParent(
                traceId,
                spanId,
                TraceFlags.fromByte(OtelEncodingUtils.byteFromBase16(firstTraceFlagsChar, secondTraceFlagsChar)),
                TraceState.getDefault());
        }
    }

    private static class V2XVeh {
        private OpenTelemetrySdk sdk;
        private Tracer tracer;

        public void start(android.content.Context context) {
            sdk = OTelHelper.initV2X4Veh(context);
            tracer = sdk.getTracer("V2X-Veh");
        }

        private void onOpenSignalReceived(String traceparent) {
            Context context = sdk.getPropagators().getTextMapPropagator().extract(
                Context.current(), traceparent,
                new TextMapGetter<String>() {
                    @Override
                    public Iterable<String> keys(String carrier) {
                        return new ArrayList<String>() {{add("traceparent");}};
                    }

                    @Override
                    public String get(String carrier, String key) {
                        return carrier;
                    }
                }
            );

            final Span span = SpanHelper.spanBuilder(tracer, "收到指令 <<== 远程打开空调")
                .setAttribute("control_code", "veh_receive_signal")
                .addLink(Span.fromContext(context).getSpanContext())
                .startSpan();
            try (Scope ignored = span.makeCurrent()) {
                vehOpenAirConditioner();
            } catch (Throwable e) {
                span.recordException(e);
            } finally {
                span.end();
            }
        }

        private void vehOpenAirConditioner() {
            Context.current().wrap(() -> {
                    Span start = SpanHelper.spanBuilder(tracer, "远程启动空调")
                        .setAttribute("control_code", "veh_start_open")
                        .startSpan();
                    try (Scope ignored = start.makeCurrent()) {
                        Context.current().wrap(() -> {
                                Span span = SpanHelper.spanBuilder(tracer, "1. 状态检查")
                                    .setAttribute("control_code", "veh_check_status")
                                    .startSpan();
                                try (Scope ignored1 = span.makeCurrent()) {
                                    boolean succ = Utils.randomSucc(0.1);
                                    SpanHelper.spanBuilder(tracer, "电源状态正常")
                                        .setAttribute("control_code", "veh_status_checked")
                                        .startSpan()
                                        .setStatus(succ ? StatusCode.OK : StatusCode.ERROR, succ ? "" : "电源状态异常")
                                        .end();

                                    if (!succ) {
                                        return;
                                    }

                                } finally {
                                    span.end();
                                }

                                span = SpanHelper.spanBuilder(tracer, "2. 打开空调")
                                    .setAttribute("control_code", "veh_open_air")
                                    .startSpan();
                                try (Scope ignored1 = span.makeCurrent()) {
                                    boolean succ = Utils.randomSucc(0.2);
                                    SpanHelper.spanBuilder(tracer, "执行打开空调")
                                        .setAttribute("control_code", "veh_air_opened")
                                        .startSpan()
                                        .setStatus(succ ? StatusCode.OK : StatusCode.ERROR, succ ? "": "空调打开失败")
                                        .end();
                                } finally {
                                    span.end();
                                }

                                span = SpanHelper.spanBuilder(tracer, "2. 上报状态")
                                    .setAttribute("control_code", "veh_send_status")
                                    .startSpan();
                                try (Scope ignored1 = span.makeCurrent()) {
                                    SpanHelper.spanBuilder(tracer, "状态上报完成")
                                        .setAttribute("control_code", "veh_status_sent")
                                        .startSpan().end();
                                } finally {
                                    span.end();
                                }
                            })
                            .run();
                    } finally {
                        start.end();
                    }
                })
                .run();
        }
    }
}
