package com.aliyun.sls.android.producer.example.example.network;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.aliyun.sls.android.SLSAdapter;
import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.SLSLog;
import com.aliyun.sls.android.plugin.network_diagnosis.SLSNetDiagnosis;
import com.aliyun.sls.android.plugin.network_diagnosis.SLSNetDiagnosisPlugin;
import com.aliyun.sls.android.producer.example.BaseActivity;
import com.aliyun.sls.android.producer.example.R;

/**
 * @author gordon
 * @date 2021/08/26
 */
public class NetworkExample extends BaseActivity {
    private static final String TAG = "NetworkExample";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_example);

        SLSAdapter adapter = SLSAdapter.getInstance();
        adapter.addPlugin(new SLSNetDiagnosisPlugin());

        SLSConfig config = new SLSConfig(this);
        config.pluginAppId = this.pluginAppId;
        config.endpoint = this.endpoint;
        config.accessKeyId = this.accessKeyId;
        config.accessKeySecret = this.accessKeySecret;
        config.debuggable = true;

        adapter.init(config);

        findViewById(R.id.example_send_http_text).setOnClickListener(v -> {
            printStatus("start http...");
            SLSNetDiagnosis.getInstance().http("https://www.aliyun.com", result -> {
                SLSLog.d(TAG, String.format("http result: %s", result));
                printStatus(String.format("http result: %s", result));
            });
        });

        findViewById(R.id.example_send_ping_text).setOnClickListener(v -> {
            printStatus("start ping...");
            SLSNetDiagnosis.getInstance().ping("www.aliyun.com", result -> {
                SLSLog.d(TAG, String.format("ping result: %s", result));
                printStatus(String.format("ping result: %s", result));
            });
        });

        findViewById(R.id.example_send_tcpping_text).setOnClickListener(v -> {
            printStatus("start tcp ping...");
            SLSNetDiagnosis.getInstance().tcpPing("www.aliyun.com", 80,result -> {
                SLSLog.d(TAG, String.format("tcp ping result: %s", result));
                printStatus(String.format("tcp ping result: %s", result));
            });
        });

        findViewById(R.id.example_send_mtr_text).setOnClickListener(v -> {
            printStatus("start mtr...");
            SLSNetDiagnosis.getInstance().mtr("www.aliyun.com", result -> {
                SLSLog.d(TAG, String.format("mtr result: %s", result));
                printStatus(String.format("mtr result: %s", result));
            });
        });
    }


}
