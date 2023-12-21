package com.aliyun.sls.android.producer.example.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import android.content.Context;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.utdid.Utdid;
import com.aliyun.sls.android.crashreporter.CrashReporter;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.HttpRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.MtrRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.PingRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.TcpPingRequest;
import com.aliyun.sls.android.network_diagnosis.NetworkDiagnosis;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author yulong.gyl
 * @date 2023/12/20
 */
public class ExampleHelper {
    private static final String TAG = "ExampleHelper";

    public static class NetworkLink {
        private static final String ERROR_URL
            = "http://sls-mall.cfa82911e541341a1b9d21d527075cbff.cn-hangzhou.alicontainer.com";

        public void start() {
            // 子线程链接游戏
            new Thread(() -> connect()).start();
        }

        private boolean error = false;
        private int errorToken = 0;
        private Random random = new Random();

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
                CrashReporter.reportException("接口调用失败", e, new HashMap<String, String>(){
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
        private static String[] sDeviceIds = {
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

        public static String getDeviceId(Context context) {
            Random random = new Random();
            int index = random.nextInt(100);
            if (index < sDeviceIds.length) {
                return sDeviceIds[index];
            }

            return Utdid.getInstance().getUtdid(context);
        }
    }
}
