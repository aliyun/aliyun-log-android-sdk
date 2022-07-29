package com.aliyun.sls.android.producer.example.example.network;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.aliyun.sls.android.SLSLog;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_example);

        //SLSAdapter adapter = SLSAdapter.getInstance();
        //adapter.addPlugin(new SLSNetDiagnosisPlugin());
        //
        //SLSConfig config = new SLSConfig(this);
        //config.pluginAppId = this.pluginAppId;
        //config.endpoint = this.endpoint;
        //config.accessKeyId = this.accessKeyId;
        //config.accessKeySecret = this.accessKeySecret;
        ////config.siteId = "cn";
        //
        //config.userId = "test_user_id";
        //config.userNick = "test_nick";
        //config.addCustom("custom_key", "custom_value");
        //config.addCustom("custom_key2", "custom_value2");
        //
        //config.debuggable = true;
        //
        //adapter.init(config);

        findViewById(R.id.example_send_http_text).setOnClickListener(v -> {
            printStatus("start http...");
            NetworkDiagnosis.getInstance().http("https://www.aliyun.com", (type, ret) -> {
                SLSLog.d(TAG, String.format("http result: %s", ret));
                printStatus(String.format("http result: %s", ret));
            });
        });

        findViewById(R.id.example_send_ping_text).setOnClickListener(v -> {
            printStatus("start ping...");
            NetworkDiagnosis.getInstance().ping("www.aliyun.com", (type, ret) -> {
                SLSLog.d(TAG, String.format("ping result: %s", ret));
                printStatus(String.format("ping result: %s", ret));
            });
        });

        findViewById(R.id.example_send_tcpping_text).setOnClickListener(v -> {
            printStatus("start tcp ping...");
            NetworkDiagnosis.getInstance().tcpPing("www.aliyun.com", 80, (type, ret) -> {
                SLSLog.d(TAG, String.format("tcp ping result: %s", ret));
                printStatus(String.format("tcp ping result: %s", ret));
            });
        });

        findViewById(R.id.example_send_mtr_text).setOnClickListener(v -> {
            printStatus("start mtr...");
            NetworkDiagnosis.getInstance().mtr("www.aliyun.com", (type, ret) -> {
                SLSLog.d(TAG, String.format("mtr result: %s", ret));
                printStatus(String.format("mtr result: %s", ret));
            });
        });

        findViewById(R.id.example_send_dns_text).setOnClickListener(v -> {
            printStatus("start dns...");
            NetworkDiagnosis.getInstance().dns("www.aliyun.com", (type, ret) -> {
                SLSLog.d(TAG, String.format("dns result: %s", ret));
                printStatus(String.format("dns result: %s", ret));
            });
        });

        findViewById(R.id.example_send_auto_text).setOnClickListener(this::auto);

        findViewById(R.id.example_policy_text).setOnClickListener(v -> {
            startActivity(new Intent(NetworkExample.this, NetworkWithPolicy.class));
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

}
