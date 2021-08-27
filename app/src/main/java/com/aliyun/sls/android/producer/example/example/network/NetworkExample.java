package com.aliyun.sls.android.producer.example.example.network;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.aliyun.sls.android.SLSAdapter;
import com.aliyun.sls.android.SLSConfig;
//import com.aliyun.sls.android.plugin.network_monitor.SLSNetwork;
//import com.aliyun.sls.android.plugin.network_monitor.SLSNetworkMonitorPlugin;
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

//        SLSAdapter adapter = SLSAdapter.getInstance();
//        adapter.addPlugin(new SLSNetworkMonitorPlugin());
//
//        SLSConfig config = new SLSConfig(this);
//        config.endpoint = this.endpoint;
//        config.accessKeyId = this.accessKeyId;
//        config.accessKeySecret = this.accessKeySecret;
//        config.debuggable = true;
//
//        adapter.init(config);
//
//        // 测试发送日志的按钮
//        findViewById(R.id.example_send_one_text).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SLSNetwork.getInstance().ping();
//            }
//        });
    }


}
