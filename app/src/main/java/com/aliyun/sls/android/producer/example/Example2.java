package com.aliyun.sls.android.producer.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerClient;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.LogProducerException;
import com.aliyun.sls.android.producer.LogProducerResult;
import com.aliyun.sls.android.producer.test.R;

/**
 * 开启离线缓存
 */
public class Example2 extends AppCompatActivity {

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
            // 1 开启断点续传功能， 0 关闭
            // 每次发送前会把日志保存到本地的binlog文件，只有发送成功才会删除，保证日志上传At Least Once
            config.setPersistent(1);
            // 持久化的文件名，需要保证文件所在的文件夹已创建。配置多个客户端时，不应设置相同文件
            config.setPersistentFilePath(getFilesDir() + "/log.dat");
            // 是否每次AddLog强制刷新，高可靠性场景建议打开
            config.setPersistentForceFlush(1);
            // 持久化文件滚动个数，建议设置成10。
            config.setPersistentMaxFileCount(10);
            // 每个持久化文件的大小，建议设置成1-10M
            config.setPersistentMaxFileSize(1024 * 1024);
            // 本地最多缓存的日志数，不建议超过1M，通常设置为65536即可
            config.setPersistentMaxLogCount(65536);

            client = new LogProducerClient(config);
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
            android.util.Log.d("LogProducerResult",String.format("%s %s", res, res.isLogProducerResultOk()));
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
