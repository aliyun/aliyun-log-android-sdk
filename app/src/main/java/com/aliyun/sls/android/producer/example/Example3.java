package com.aliyun.sls.android.producer.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerCallback;
import com.aliyun.sls.android.producer.LogProducerClient;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.LogProducerException;
import com.aliyun.sls.android.producer.LogProducerResult;
import com.aliyun.sls.android.producer.test.R;

/**
 * 设置回调
 */
public class Example3 extends AppCompatActivity {

    String endpoint = "http://cn-hangzhou.log.aliyuncs.com";
    String project = "k8s-log-c783b4a12f29b44efa31f655a586bb243";
    String logstore = "666";
    String accesskeyid = "";
    String accesskeysecret = "";
    String securityToken = "";
    LogProducerConfig config;
    LogProducerClient client;

    int x = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button send = findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });

        try {
            config = new LogProducerConfig(endpoint, project, logstore, accesskeyid, accesskeysecret);
            client = new LogProducerClient(config, new LogProducerCallback() {
                @Override
                public void onCall(int resultCode, String reqId, String errorMessage, int logBytes, int compressedBytes) {
                    // 回调
                    // resultCode       返回结果代码
                    // reqId            请求id
                    // errorMessage     错误信息，没有为null
                    // logBytes         日志大小
                    // compressedBytes  压缩后日志大小
                    System.out.printf("%s %s %s %s %s%n", LogProducerResult.fromInt(resultCode), reqId, errorMessage, logBytes, compressedBytes);
                }
            });
        } catch (LogProducerException e) {
            e.printStackTrace();
        }

    }

    void send() {
        Log log = oneLog();
        log.putContent("index", String.valueOf(x));
        x = x + 1;
        if (client != null) {
            LogProducerResult res = client.addLog(log);
            System.out.printf("%s %s%n", res, res.isLogProducerResultOk());
        }
    }

    Log oneLog() {
        Log log = new Log();
        log.putContent("content_key_1", "1abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_2", "2abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_3", "3abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_4", "4abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_5", "5abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_6", "6abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_7", "7abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_8", "8abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_9", "9abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("random", String.valueOf(Math.random()));
        log.putContent("content", "中文");
        log.putContent(null, "null");
        log.putContent("null", null);
        return log;
    }


}
