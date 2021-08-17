package com.aliyun.sls.android.plugin.crashreporter.sender;

import java.io.File;
import java.util.Map.Entry;

import android.text.TextUtils;
import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.SLSLog;
import com.aliyun.sls.android.plugin.crashreporter.IReportSender;
import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerClient;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.LogProducerException;
import com.aliyun.sls.android.producer.utils.TimeUtils;
import com.aliyun.sls.android.scheme.Scheme;

/**
 * @author gordon
 * @date 2021/04/15
 */
public class SLSReportSender implements IReportSender {
    private static final String TAG = "SLSReportSender";

    private LogProducerConfig producerConfig;
    private LogProducerClient producerClient;
    private SLSConfig slsConfig;

    @Override
    public void resetSecurityToken(String accessKeyId, String accessKeySecret, String securityToken) {
        if (TextUtils.isEmpty(securityToken)) {
            producerConfig.setAccessKeyId(accessKeyId);
            producerConfig.setAccessKeySecret(accessKeySecret);
        } else {
            producerConfig.resetSecurityToken(accessKeyId, accessKeySecret, securityToken);
        }
    }

    @Override
    public void resetProject(String endpoint, String project, String logstore) {
        if (!TextUtils.isEmpty(endpoint)) {
            producerConfig.setEndpoint(endpoint);
        }

        if (!TextUtils.isEmpty(project)) {
            producerConfig.setProject(project);
        }

        // do not reset logstore, this logstore should be LOGSTORE which value is sls-alysls-track-base.
        //if (!TextUtils.isEmpty(logstore)) {
        //    producerConfig.setLogstore(logstore);
        //}
    }

    @Override
    public void init(final SLSConfig config) {
        if (TextUtils.isEmpty(config.pluginLogproject)) {
            SLSLog.e(TAG, "pluginLogproject should not be null or empty. you can reset it with slsAdapter.resetProject() method.");
        }

        this.slsConfig = config;

        final String logProjectName = config.pluginLogproject;
        final String logStoreName = LOGSTORE;

        if (config.debuggable) {
            SLSLog.v(TAG, SLSLog.format("init, logProjectName: %s, logStoreName: %s", logProjectName, logStoreName));
        }

        try {
            if (!TextUtils.isEmpty(config.securityToken)) {
                producerConfig = new LogProducerConfig(config.context
                    , config.endpoint
                    , logProjectName
                    , logStoreName
                    , config.accessKeyId
                    , config.accessKeySecret
                    , config.securityToken);
            } else {
                producerConfig = new LogProducerConfig(config.context
                    , config.endpoint
                    , logProjectName
                    , logStoreName
                    , config.accessKeyId
                    , config.accessKeySecret);
            }
            producerConfig.setTopic("crash_report");
            producerConfig.addTag("crash_report", "Android");
            producerConfig.setPacketLogBytes(1024 * 1024 * 5);
            // 每个缓存的日志包中包含日志数量的最大值，取值为1~4096，默认为1024
            producerConfig.setPacketLogCount(4096);
            // 被缓存日志的发送超时时间，如果缓存超时，则会被立即发送，单位为毫秒，默认为3000
            //        config.setPacketTimeout(3000);
            // 单个Producer Client实例可以使用的内存的上限，超出缓存时add_log接口会立即返回失败
            // 默认为64 * 1024 * 1024
            producerConfig.setMaxBufferLimit(200 * 1024 * 1024);
            // 发送线程数，默认为1
            producerConfig.setSendThreadCount(1);

            // 1 开启断点续传功能， 0 关闭
            // 每次发送前会把日志保存到本地的binlog文件，只有发送成功才会删除，保证日志上传At Least Once
            producerConfig.setPersistent(1);
            // 持久化的文件名，需要保证文件所在的文件夹已创建。配置多个客户端时，不应设置相同文件

            final File rootPath = new File(new File(config.context.getFilesDir(), "sls_crash_reporter"), "sls_logs");
            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }
            producerConfig.setPersistentFilePath(rootPath + "/crash_log.dat");
            // 是否每次AddLog强制刷新，高可靠性场景建议打开
            producerConfig.setPersistentForceFlush(1);
            // 持久化文件滚动个数，建议设置成10。
            producerConfig.setPersistentMaxFileCount(10);
            // 每个持久化文件的大小，建议设置成1-10M
            producerConfig.setPersistentMaxFileSize(1024 * 1024 * 10);
            // 本地最多缓存的日志数，不建议超过1M，通常设置为65536即可
            producerConfig.setPersistentMaxLogCount(65536);
            producerConfig.setDropDelayLog(0);
            producerConfig.setDropUnauthorizedLog(0);

            producerClient = new LogProducerClient(producerConfig);
            //producerClient = new LogProducerClient(producerConfig,
            //    (resultCode, reqId, errorMessage, logBytes, compressedBytes) -> {
            //        if (config.debuggable) {
            //            // @formatter:off
            //            SLSLog.e(TAG, SLSLog.format(
            //                "client onCall. resultCode: %d, reqId: %s, errorMessage: %s, logByres: %d, compressedByres: %d",
            //                resultCode, reqId, errorMessage, logBytes, compressedBytes));
            //        }
            //    });
            //producerClient.setAddLogInterceptor(log -> {
            //    Scheme scheme = Scheme.createDefaultScheme(config.context);
            //    for (Entry<String,String> entry : scheme.toMap().entrySet()) {
            //        log.putContent(entry.getKey(), entry.getValue());
            //    }
            //});

            if (config.debuggable) {
                SLSLog.v(TAG, "init success.");
            }

        } catch (LogProducerException e) {
            e.printStackTrace();
            SLSLog.e(TAG, SLSLog.format("init error. error: ", e.getMessage()));
        }
    }

    @Override
    public boolean send(Scheme data) {
        if (null == producerClient) {
            SLSLog.e(TAG, "LogProducerClient is not init or exception caused.");
            return false;
        }

        if(null == data) {
            SLSLog.e(TAG, "TCData must not be null.");
            return false;
        }

        Log log = new Log();
        for (Entry<String,String> entry : data.toMap().entrySet()) {
            log.putContent(entry.getKey(), entry.getValue());
        }

        TimeUtils.fixTime(log);
        final boolean res =  producerClient.addLog(log).isLogProducerResultOk();
        if (slsConfig.debuggable) {
            SLSLog.v(TAG, "send log success.");
        }

        return res;
        //return true;
    }
}
