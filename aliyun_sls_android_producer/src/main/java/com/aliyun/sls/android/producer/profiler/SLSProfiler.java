package com.aliyun.sls.android.producer.profiler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerClient;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.LogProducerException;
import com.aliyun.sls.android.producer.LogProducerResult;

/**
 * @author gordon
 * @date 2021/05/06
 */
public class SLSProfiler {

    private LogProducerClient logProducerClient;

    private SLSProfiler() {
        //no instance
    }

    public static SLSProfiler getInstance() {
        return Holder.INSTANCE;
    }

    private static Log createLog(final boolean success, LogProducerResult result) {
        final Log log = new Log();
        // @formatter:off
        log.putContent("local_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:SSS", Locale.getDefault()).format(new Date()));
        log.putContent("success", (success ? "1" : "0"));
        log.putContent("code", result.name());
        return log;
    }

    public synchronized void init(Context context, LogProducerConfig config) {
        if (null != logProducerClient) {
            return;
        }

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
        config.setPersistent(1);
        // 持久化的文件名，需要保证文件所在的文件夹已创建。配置多个客户端时，不应设置相同文件
        config.setPersistentFilePath(context.getFilesDir() + "/log_profiler.dat");
        // 是否每次AddLog强制刷新，高可靠性场景建议打开
        config.setPersistentForceFlush(1);
        // 持久化文件滚动个数，建议设置成10。
        config.setPersistentMaxFileCount(10);
        // 每个持久化文件的大小，建议设置成1-10M
        config.setPersistentMaxFileSize(1024 * 1024);
        // 本地最多缓存的日志数，不建议超过1M，通常设置为65536即可
        config.setPersistentMaxLogCount(65536);

        config.setNtpTimeOffset(3);
        //对于超过 `max_log_delay_time` 日志的处理策略
        //0 不丢弃，把日志时间修改为当前时间; 1 丢弃，默认为 1 （丢弃）
        config.setDropDelayLog(0);
        //是否丢弃鉴权失败的日志，0 不丢弃，1丢弃
        //默认为 0，即不丢弃
        config.setDropUnauthorizedLog(0);

        // profiler config 关闭
        config.setEnableProfiler(false);
        config.setTopic("profiler");
        config.addTag("os", "Android");

        try {
            logProducerClient = new LogProducerClient(config, null);
        } catch (LogProducerException e) {
            e.printStackTrace();
        }
    }

    public void reportAddLog(boolean success, LogProducerResult result) {
        if (null != logProducerClient) {
            final Log log = createLog(success, result);
            log.putContent("type", "add");
            logProducerClient.addLog(log);
        }
    }

    public void reportSendLog(boolean success, LogProducerResult result) {
        if (null != logProducerClient) {
            final Log log = createLog(success, result);
            log.putContent("type", "send");
            logProducerClient.addLog(log);
        }
    }

    private static class Holder {
        private static final SLSProfiler INSTANCE = new SLSProfiler();
    }
}
