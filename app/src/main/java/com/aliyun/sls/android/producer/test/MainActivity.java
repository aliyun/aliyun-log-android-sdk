package com.aliyun.sls.android.producer.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerCallback;
import com.aliyun.sls.android.producer.LogProducerClient;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.LogProducerException;
import com.aliyun.sls.android.producer.LogProducerResult;

public class MainActivity extends AppCompatActivity {

    String endpoint = "https://cn-hangzhou.log.aliyuncs.com";
    String project = "k8s-log-c783b4a12f29b44efa31f655a586bb243";
    String logstore = "666";
    String accesskeyid = "";
    String accesskeysecret = "";
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
            createClient();
        } catch (LogProducerException e) {
            e.printStackTrace();
        }

        Button test = findViewById(R.id.test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        send(2048);
                    }
                }).start();
            }
        });
    }


    void createClient() throws LogProducerException {
        // 指定sts token 创建config，过期之前调用resetSecurityToken重置token
        //config = new LogProducerConfig(endpoint, project, logstore, accesskeyid, accesskeysecret, securityToken);
        LogProducerConfig config = new LogProducerConfig(endpoint, project, logstore, accesskeyid, accesskeysecret);
        // 设置主题
        config.setTopic("test_topic");
        // 设置tag信息，此tag会附加在每条日志上
        config.addTag("test", "test_tag");
        // 每个缓存的日志包的大小上限，取值为1~5242880，单位为字节。默认为1024 * 1024
        config.setPacketLogBytes(1024 * 1024);
        // 每个缓存的日志包中包含日志数量的最大值，取值为1~4096，默认为1024
        config.setPacketLogCount(1024);
        // 被缓存日志的发送超时时间，如果缓存超时，则会被立即发送，单位为毫秒，默认为3000
        config.setPacketTimeout(3000);
        // 单个Producer Client实例可以使用的内存的上限，超出缓存时add_log接口会立即返回失败
        // 默认为64 * 1024 * 1024
        config.setMaxBufferLimit(64 * 1024 * 1024);
        // 发送线程数，默认为1
        config.setSendThreadCount(1);

        // 1 开启断点续传功能， 0 关闭
        // 每次发送前会把日志保存到本地的binlog文件，只有发送成功才会删除，保证日志上传At Least Once
        config.setPersistent(0);
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

        //网络连接超时时间，整数，单位秒，默认为10
        config.setConnectTimeoutSec(10);
        //日志发送超时时间，整数，单位秒，默认为15
        config.setSendTimeoutSec(10);
        //flusher线程销毁最大等待时间，整数，单位秒，默认为1
        config.setDestroyFlusherWaitSec(2);
        //sender线程池销毁最大等待时间，整数，单位秒，默认为1
        config.setDestroySenderWaitSec(2);
        //数据上传时的压缩类型，默认为LZ4压缩，0 不压缩，1 LZ4压缩，默认为1
        config.setCompressType(1);
        //设备时间与标准时间之差，值为标准时间-设备时间，一般此种情况用户客户端设备时间不同步的场景
        //整数，单位秒，默认为0；比如当前设备时间为1607064208, 标准时间为1607064308，则值设置为 1607064308 - 1607064208 = 10
        config.setNtpTimeOffset(3);
        //日志时间与本机时间之差，超过该大小后会根据 `drop_delay_log` 选项进行处理。
        //一般此种情况只会在设置persistent的情况下出现，即设备下线后，超过几天/数月启动，发送退出前未发出的日志
        //整数，单位秒，默认为7*24*3600，即7天
        config.setMaxLogDelayTime(7*24*3600);
        //对于超过 `max_log_delay_time` 日志的处理策略
        //0 不丢弃，把日志时间修改为当前时间; 1 丢弃，默认为 1 （丢弃）
        config.setDropDelayLog(0);

        client = new LogProducerClient(config, new LogProducerCallback() {
            @Override
            public void onCall(int resultCode, String reqId, String errorMessage, int logBytes, int compressedBytes) {
                System.out.printf("%s %s %s %s %s%n", LogProducerResult.fromInt(resultCode), reqId, errorMessage, logBytes, compressedBytes);
            }
        });
//        client = new LogProducerClient(config);
    }

    void send() {
        Log log = oneLog();
        log.putContent("index", String.valueOf(x));
        x = x + 1;
        if (client != null) {
            LogProducerResult res = client.addLog(log, 0);
            System.out.printf("%s %s%n", res, res.isLogProducerResultOk());
        }
    }

    void send(int logCountPerSecond) {
        while (true) {
            long time1 = System.currentTimeMillis();
            for (int i = 0; i < logCountPerSecond; i++) {
                Log log = oneLog();
                client.addLog(log);
            }
            long time2 = System.currentTimeMillis();
            if (time2 - time1 < 1000) {
                try {
                    Thread.sleep(1000 - (time2 - time1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
        log.putContent("content", "中文️");
        log.putContent(null, "null");
        log.putContent("null", null);
        return log;
    }

}
