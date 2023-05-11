package com.aliyun.sls.android.producer.example.example.network;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.DnsRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.HttpRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.MtrRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.MtrRequest.Protocol;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.PingRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.TcpPingRequest;
import com.aliyun.sls.android.network_diagnosis.NetworkDiagnosis;
import com.aliyun.sls.android.producer.example.BaseActivity;
import com.aliyun.sls.android.producer.example.R;

/**
 * @author gordon
 * @date 2021/08/26
 */
public class NetworkExample extends BaseActivity {
    private static final String TAG = "NetworkExample";
    private List<String> endpoints = new ArrayList<String>() {
        {
            add("cn-hangzhou.log.aliyuncs.com");
            add("cn-hangzhou-finance.log.aliyuncs.com");
            add("cn-shanghai.log.aliyuncs.com");
            add("cn-shanghai-finance-1.log.aliyuncs.com");
            add("cn-qingdao.log.aliyuncs.com");
            add("cn-beijing.log.aliyuncs.com");
            add("cn-north-2-gov-1.log.aliyuncs.com");
            add("cn-zhangjiakou.log.aliyuncs.com");
            add("cn-huhehaote.log.aliyuncs.com");
            add("cn-wulanchabu.log.aliyuncs.com");
            add("cn-shenzhen.log.aliyuncs.com");
            add("cn-shenzhen-finance.log.aliyuncs.com");
            add("cn-heyuan.log.aliyuncs.com");
            add("cn-guangzhou.log.aliyuncs.com");
            add("cn-chengdu.log.aliyuncs.com");
            add("cn-hongkong.log.aliyuncs.com");
            add("ap-northeast-1.log.aliyuncs.com");
            add("ap-southeast-1.log.aliyuncs.com");
            add("ap-southeast-2.log.aliyuncs.com");
            add("ap-southeast-3.log.aliyuncs.com");
            add("ap-southeast-6.log.aliyuncs.com");
            add("ap-southeast-5.log.aliyuncs.com");
            add("me-east-1.log.aliyuncs.com");
            add("us-west-1.log.aliyuncs.com");
            add("eu-central-1.log.aliyuncs.com");
            add("us-east-1.log.aliyuncs.com");
            add("ap-south-1.log.aliyuncs.com");
            add("eu-west-1.log.aliyuncs.com");
        }
    };
    private int index = 0;
    private int extIndex = 0;

    @RequiresApi(api = VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_example);

        final NetworkDiagnosis diagnosis = NetworkDiagnosis.getInstance();

        // 开启多网卡支持
        diagnosis.setMultiplePortsDetect(true);
        // 注册全局回调
        diagnosis.registerCallback(response -> SLSLog.d(TAG,
            String.format("global callback: {type: %s, ret: %s}", response.type.value, response.content)));

        //final HttpCredential credential = new HttpCredential(getSSLContext(NetworkExample.this), null);
        //NetworkDiagnosis.getInstance().registerHttpCredentialCallback((url, context) -> credential);

        // http 探测
        findViewById(R.id.example_send_http_text).setOnClickListener(v -> {
            printStatus("start http...");

            HttpRequest request = new HttpRequest();
            // 可选参数
            request.context = "<your http context id>";
            request.headerOnly = true;
            request.downloadBytesLimit = 1024;

            request.domain = "https://www.aliyun.com";
            diagnosis.http(request, response -> {
                SLSLog.d(TAG, String.format("http result: %s", response.content));
                printStatus(String.format("http result: %s", response.content));

            });

            //request.credential = credential;
            //diagnosis.http(request, response -> {
            //    SLSLog.d(TAG, String.format("http with credential result: %s", response.content));
            //    printStatus(String.format("http with credential result: %s", response.content));
            //});
        });

        // ping 探测
        findViewById(R.id.example_send_ping_text).setOnClickListener(v -> {
            printStatus("start ping...");
            PingRequest request = new PingRequest();
            request.domain = "www.aliyun.com";
            // 可选参数
            request.multiplePortsDetect = true; // 多网卡探测 s
            request.context = "<your ping context id>";

            diagnosis.ping(request, response -> {
                SLSLog.d(TAG, String.format("ping result: %s", response.content));
                printStatus(String.format("ping result: %s", response.content));

            });
        });

        // tcp ping 探测
        findViewById(R.id.example_send_tcpping_text).setOnClickListener(v -> {
            printStatus("start tcp ping...");
            TcpPingRequest request = new TcpPingRequest();
            request.domain = "www.aliyun.com";
            request.port = 80;
            // 可选参数
            request.context = "<your tcp ping context id>";
            diagnosis.tcpPing(request, response -> {
                SLSLog.d(TAG, String.format("tcp ping result: %s", response.content));
                printStatus(String.format("tcp ping result: %s", response.content));

            });
        });

        // mtr 探测
        findViewById(R.id.example_send_mtr_text).setOnClickListener(v -> {
            printStatus("start mtr...");
            MtrRequest request = new MtrRequest();
            request.domain = "www.aliyun.com";
            // 可选参数
            request.protocol = Protocol.ICMP;
            request.context = "<your mtr context id>";

            diagnosis.mtr(request, response -> {
                SLSLog.d(TAG, String.format("mtr result: %s", response.content));
                printStatus(String.format("mtr result: %s", response.content));

            });
        });

        // dns 探测
        findViewById(R.id.example_send_dns_text).setOnClickListener(v -> {
            printStatus("start dns...");
            DnsRequest request = new DnsRequest();
            request.domain = "www.aliyun.com";
            // 可选参数
            request.context = "<your dns context id>";
            diagnosis.dns(request, response -> {
                SLSLog.d(TAG, String.format("dns result: %s", response.content));
                printStatus(String.format("dns result: %s", response.content));
            });
        });

        findViewById(R.id.example_send_auto_text).setOnClickListener(this::auto);

        findViewById(R.id.example_policy_text).setOnClickListener(v -> {
            startActivity(new Intent(NetworkExample.this, NetworkWithPolicy.class));
        });

        findViewById(R.id.example_extension_update).setOnClickListener(v -> {
            NetworkDiagnosis.getInstance().updateExtensions(new HashMap<String, String>() {
                {
                    put("ext_key", "ext_value " + (extIndex += 1));
                }
            });
        });
    }

    private void auto(View v) {
        printStatus("start auto. index: " + index);

        final String domain = endpoints.get(index);
        final String url = "https://" + domain;
        //SLSNetDiagnosis.getInstance().ping(domain);
        //SLSNetDiagnosis.getInstance().http(url);
        //SLSNetDiagnosis.getInstance().tcpPing(domain, 80);
        //SLSNetDiagnosis.getInstance().mtr(domain);

        index += 1;
        if (index >= endpoints.size()) {
            index = 0;
        }

        v.postDelayed(() -> auto(v), 5000);
    }

    @RequiresApi(api = VERSION_CODES.O)
    private SSLContext getSSLContext(Context context) {
        try {
            // 服务器端需要验证的客户端证书
            String KEY_STORE_TYPE_P12 = "PKCS12";
            KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE_P12);

            String p12Str = "";
            byte[] p12Data = Base64.getDecoder().decode(p12Str);

            InputStream ksIn = new ByteArrayInputStream(p12Data);
            try {
                keyStore.load(ksIn, "123".toCharArray());
            } catch (Exception e) {
                Log.e("Exception", e.getMessage(), e);
            } finally {
                try {
                    ksIn.close();
                } catch (Exception ignore) {
                }
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore)null);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            keyManagerFactory.init(keyStore, "123".toCharArray());
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (Exception e) {
            Log.e("tag", e.getMessage(), e);
        }
        return null;
    }

}
