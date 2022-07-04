package com.aliyun.sls.android.producer.example.example.producer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.aliyun.sls.android.producer.LogProducerCallback;
import com.aliyun.sls.android.producer.LogProducerClient;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.LogProducerException;
import com.aliyun.sls.android.producer.LogProducerResult;
import com.aliyun.sls.android.producer.example.BaseActivity;
import com.aliyun.sls.android.producer.example.R;

import java.io.File;

/**
 * 动态配置
 * @author gordon
 * @date 2021/08/18
 */
public class ProducerWithDynamicConfig extends BaseActivity {
    private static final String TAG = "ProducerWithDynamic";

    private LogProducerConfig config = null;
    private LogProducerClient client = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer_with_dynamic_config);
        initProducer();

        // 更新配置按钮
        findViewById(R.id.example_update_config_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateConfig();
            }
        });
        // 更新AK
        findViewById(R.id.example_update_config_ak_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAK();
            }
        });
        // 重置
        findViewById(R.id.example_reset_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
        // 测试发送日志的按钮
        findViewById(R.id.example_send_one_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLog();
            }
        });
    }

    private void initProducer() {
        try {
            config = new LogProducerConfig(this);
            config.logProducerDebug();

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
            config.setMaxLogDelayTime(7 * 24 * 3600);
            //对于超过 `max_log_delay_time` 日志的处理策略
            //0 不丢弃，把日志时间修改为当前时间; 1 丢弃，默认为 1 （丢弃）
            config.setDropDelayLog(0);
            //是否丢弃鉴权失败的日志，0 不丢弃，1丢弃
            //默认为 0，即不丢弃
            config.setDropUnauthorizedLog(0);
            // 是否使用主线程回调
            // false: 使用主线程回调。回调会在主线程上执行，且每个 client 都有自己单独的回调。
            // true: 使用 sender 线程回调。回调会在 sender 现呈上执行，每次执行回调时都会 attach 一个新的 java 线程，所有 client 共用一个回调。
            // 注意：默认使用 sender 线程回调。
            config.setCallbackFromSenderThread(false);

            /**
             * 以下为开启断点续传的配置, 按照如下配置开启断点续传功能后, 日志会先缓存到本地
             */
            // 1 开启断点续传功能， 0 关闭
            // 每次发送前会把日志保存到本地的binlog文件，只有发送成功才会删除，保证日志上传At Least Once
            config.setPersistent(1);
            // 持久化的文件名，需要保证文件所在的文件夹已创建。
            // !!!!!!!!!!!!!!!!!!!注意!!!!!!!!!!!!!!!!!!!
            // 配置多个客户端时，不应设置相同文件
            config.setPersistentFilePath(getFilesDir() + String.format("%slog_data.dat", File.separator));
            // 是否每次AddLog强制刷新，高可靠性场景建议打开
            config.setPersistentForceFlush(0);
            // 持久化文件滚动个数，建议设置成10。
            config.setPersistentMaxFileCount(10);
            // 每个持久化文件的大小，建议设置成1-10M
            config.setPersistentMaxFileSize(1024 * 1024);
            // 本地最多缓存的日志数，不建议超过1M，通常设置为65536即可
            config.setPersistentMaxLogCount(65536);

            /**
             * 以下为通过 LogProducerConfig 构造一个 LogProducerClient 实例
             */
            // callback为可选配置, 如果不需要关注日志的发送成功或失败状态, 可以不注册 callback
            final LogProducerCallback callback = new LogProducerCallback() {
                @Override
                public void onCall(int resultCode, String reqId, String errorMessage, int logBytes, int compressedBytes) {
                    // resultCode: 状态码, 详见 LogProducerResult
                    // reqId: 请求Id, 已经废弃
                    // errorMessage: 失败信息
                    // logBytes: 日志原始字节数
                    // compressedBytes: 日志压缩字节数
                    android.util.Log.e(TAG, String.format("resultCode: %d, reqId: %s, errorMessage: %s, logBytes: %d, compressedBytes: %d", resultCode, reqId, errorMessage, logBytes, compressedBytes));
                    printStatus(String.format("send log resultCode: %s, reqId: %s, errorMessage: %s, logBytes: %d, compressedBytes: %d", LogProducerResult.fromInt(resultCode), reqId, errorMessage, logBytes, compressedBytes));

                    LogProducerResult logProducerResult = LogProducerResult.fromInt(resultCode);
                    if (logProducerResult == LogProducerResult.LOG_PRODUCER_PARAMETERS_INVALID) {
                        // LogProducerClient 初始化参数不正确。
                        // 需要检查endpoint、project、logstore、accessKeyId、accessKeySecret、accessKeyToken是否无效
                        // errorMessage 也会有对应的说明:
                        // Invalid producer config destination params，表示：endpoint、project、logstore的配置可能有问题
                        // Invalid producer config authority params，表示：accessKeyId、accessKeySecret、accessKeyToken的配置可能有问题
                    }
                }
            };
            // 需要关注日志的发送成功或失败状态时, 第二个参数需要传入一个 callbak
            client = new LogProducerClient(config, callback);
        } catch (LogProducerException e) {
            e.printStackTrace();
        }
    }

    private void updateConfig() {
        // endpoint 必须是以 https:// 或 http:// 开头的链接
        final String endpoint = this.endpoint;
        final String project = this.logProject;
        final String logstore = this.logStore;

        config.setEndpoint(endpoint);
        config.setProject(project);
        config.setLogstore(logstore);
    }

    private void updateAK() {
        final String accessKeyId = this.accessKeyId;
        final String accessKeySecret = this.accessKeySecret;
        final String accessKeyToken = this.accessKeyToken;

        if (TextUtils.isEmpty(accessKeyToken)) {
            config.setAccessKeyId(accessKeyId);
            config.setAccessKeySecret(accessKeySecret);
        } else {
            config.resetSecurityToken(accessKeyId, accessKeySecret, accessKeyToken);
        }
    }

    private void reset() {
        config.setEndpoint("");
        config.setProject("");
        config.setLogstore("");

        config.setAccessKeyId("");
        config.setAccessKeySecret("");
    }

    private void sendLog() {
        com.aliyun.sls.android.producer.Log log = oneLog();
        LogProducerResult result = client.addLog(log);
        printStatus("addLog result: " + result);
    }

    private com.aliyun.sls.android.producer.Log oneLog() {
        return LogUtils.createLog();
    }
}
