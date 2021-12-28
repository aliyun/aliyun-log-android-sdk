package com.aliyun.sls.android.plugin.network_diagnosis.sender;

import android.text.TextUtils;

import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.SLSLog;
import com.aliyun.sls.android.plugin.ISender;
import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerClient;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.LogProducerException;
import com.aliyun.sls.android.producer.utils.TimeUtils;
import com.aliyun.sls.android.scheme.Scheme;

import java.io.File;
import java.util.Map;

/**
 * @author gordon
 * @date 2021/08/26
 */
public class SLSNetDataSender implements ISender {
    private static final String TAG = "SLSNetDataSender";

    private LogProducerConfig producerConfig;
    private LogProducerClient producerClient;
    private SLSConfig slsConfig;

    @Override
    public void init(SLSConfig config) {
        this.slsConfig = config;

        final String endpoint = "https://cn-shanghai.log.aliyuncs.com";
        final String logProjectName = "sls-aysls-network-monitor";
        final String logStoreName = "central-logsotre";

        if (config.debuggable) {
            SLSLog.v(TAG, SLSLog.format("init, logProjectName: %s, logStoreName: %s", logProjectName, logStoreName));
        }

        try {
            if (!TextUtils.isEmpty(config.securityToken)) {
                producerConfig = new LogProducerConfig(config.context
                        , endpoint
                        , logProjectName
                        , logStoreName
                        , config.accessKeyId
                        , config.accessKeySecret
                        , config.securityToken);
            } else {
                producerConfig = new LogProducerConfig(config.context
                        , endpoint
                        , logProjectName
                        , logStoreName
                        , config.accessKeyId
                        , config.accessKeySecret);
            }
            producerConfig.setTopic("network_monitor");
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

            final File rootPath = new File(new File(config.context.getFilesDir(), "sls_network_monitor"), "sls_logs");
            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }
            producerConfig.setPersistentFilePath(rootPath + "/network_log.dat");
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
            producerConfig.setUseWebtracking(true);
//            producerConfig.logProducerDebug();

//            producerClient = new LogProducerClient(producerConfig);
            producerClient = new LogProducerClient(producerConfig,
                (resultCode, reqId, errorMessage, logBytes, compressedBytes) -> {
                    if (config.debuggable) {
                        // @formatter:off
                        SLSLog.d(TAG, SLSLog.format(
                            "client onCall. resultCode: %d, reqId: %s, errorMessage: %s, logByres: %d, compressedByres: %d",
                            resultCode, reqId, errorMessage, logBytes, compressedBytes));
                    }
                });

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
        for (Map.Entry<String,String> entry : data.toMap().entrySet()) {
            log.putContent(entry.getKey(), entry.getValue());
        }

        TimeUtils.fixTime(log);
        final boolean res =  producerClient.addLog(log).isLogProducerResultOk();
        if (slsConfig.debuggable) {
            SLSLog.v(TAG, "send log success.");
        }

        return res;
    }

    @Override
    public void resetSecurityToken(String accessKeyId, String accessKeySecret, String securityToken) {

    }

    @Override
    public void resetProject(String endpoint, String project, String logstore) {

    }
}
