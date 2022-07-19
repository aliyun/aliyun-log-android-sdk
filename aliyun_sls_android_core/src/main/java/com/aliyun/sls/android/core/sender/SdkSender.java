package com.aliyun.sls.android.core.sender;

import java.io.File;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.ot.ISpanProcessor;
import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerCallback;
import com.aliyun.sls.android.producer.LogProducerClient;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.LogProducerException;
import com.aliyun.sls.android.producer.LogProducerResult;

/**
 * @author gordon
 * @date 2022/7/19
 */
public class SdkSender implements Sender, ISpanProcessor {
    private static final String TAG = "SdkSender";

    private final Context context;
    private LogProducerConfig config;
    private LogProducerClient client;

    public SdkSender(Context context) {
        this.context = context;
    }

    @Override
    public void initialize(Credentials credentials) {
        initLogProducer(credentials);
    }

    private void initLogProducer(Credentials credentials) {
        final String accessKeyId = credentials.accessKeyId;
        final String accessKeySecret = credentials.accessKeySecret;
        final String accessToken = credentials.securityToken;

        final String endpoint = null != credentials.endpoint ? credentials.endpoint.endpoint : "";
        final String project = credentials.project;
        final String logstore = getLogstoreByInstanceId(credentials.instanceId);

        try {
            config = new LogProducerConfig(
                context,
                endpoint,
                project,
                logstore,
                accessKeyId,
                accessKeySecret,
                accessToken
            );
        } catch (LogProducerException e) {
            SLSLog.e(TAG, "init LogProducer error. error: " + e.getMessage());
            return;
        }

        config.setTopic("sls_android");
        config.setPacketLogBytes(1024 * 1024);
        config.setPacketLogCount(4096);
        config.setPacketTimeout(2000);
        config.setMaxBufferLimit(64 * 1024 * 1024);
        // 发送线程数，默认为1
        config.setSendThreadCount(1);

        // 1 开启断点续传功能， 0 关闭
        // 每次发送前会把日志保存到本地的binlog文件，只有发送成功才会删除，保证日志上传At Least Once
        config.setPersistent(1);
        // 持久化的文件名，需要保证文件所在的文件夹已创建。配置多个客户端时，不应设置相同文件

        final File rootPath = new File(new File(context.getFilesDir(), "sls"), "logs");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        config.setPersistentFilePath(rootPath + File.separator + "data.dat");
        // 是否每次AddLog强制刷新，高可靠性场景建议打开
        config.setPersistentForceFlush(0);
        // 持久化文件滚动个数，建议设置成10。
        config.setPersistentMaxFileCount(10);
        // 每个持久化文件的大小，建议设置成1-10M
        config.setPersistentMaxFileSize(1024 * 1024 * 10);
        // 本地最多缓存的日志数，不建议超过1M，通常设置为65536即可
        config.setPersistentMaxLogCount(65536);
        config.setDropDelayLog(0);
        config.setDropUnauthorizedLog(0);

        try {
            client = new LogProducerClient(config, new LogProducerCallback() {
                @Override
                public void onCall(
                    int resultCode,
                    String reqId,
                    String errorMessage,
                    int logBytes,
                    int compressedBytes
                ) {
                    SLSLog.v(TAG, "resultCode: " + resultCode + ", errorMessage: " + errorMessage);
                }
            });
        } catch (LogProducerException e) {
            SLSLog.e(TAG, "init LogProducerClient error. error: " + e.getMessage());
        }
    }

    private String getLogstoreByInstanceId(String instanceId) {
        if (TextUtils.isEmpty(instanceId)) {
            return "";
        }
        
        return String.format("%s-track-raw", instanceId);
    }

    @Override
    public boolean send(Log data) {
        if (null == client) {
            return false;
        }

        return client.addLog(data) == LogProducerResult.LOG_PRODUCER_OK;
    }

    @Override
    public void setCredentials(Credentials credentials) {
        if (null == credentials || null == config) {
            return;
        }

        // update ak
        if (TextUtils.isEmpty(credentials.securityToken)) {
            if (!TextUtils.isEmpty(credentials.accessKeyId) && !TextUtils.isEmpty(credentials.accessKeySecret)) {
                config.setAccessKeyId(credentials.accessKeyId);
                config.setAccessKeySecret(credentials.accessKeySecret);
            }
        } else {
            if (!TextUtils.isEmpty(credentials.accessKeyId) && !TextUtils.isEmpty(credentials.accessKeySecret)) {
                config.resetSecurityToken(
                    credentials.accessKeyId,
                    credentials.accessKeySecret,
                    credentials.securityToken
                );
            }
        }

        // update endpoint, project, logstore
        if (null != credentials.endpoint && !TextUtils.isEmpty(credentials.endpoint.endpoint)) {
            config.setEndpoint(credentials.endpoint.endpoint);
        }
        if (!TextUtils.isEmpty(credentials.project)) {
            config.setProject(credentials.project);
        }
        if (!TextUtils.isEmpty(credentials.instanceId)) {
            config.setLogstore(getLogstoreByInstanceId(credentials.instanceId));
        }
    }

    @Override
    public boolean onEnd(Span span) {
        if (null == span) {
            return false;
        }

        Map<String, String> spanData = span.toData();
        Log log = new Log();
        log.putContents(spanData);

        return send(log);
    }
}
