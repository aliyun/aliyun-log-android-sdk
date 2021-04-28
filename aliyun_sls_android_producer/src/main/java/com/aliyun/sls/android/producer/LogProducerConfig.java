package com.aliyun.sls.android.producer;

import android.content.Context;
import com.aliyun.sls.android.producer.utils.SoLoader;

public class LogProducerConfig {

    static {
        System.loadLibrary("sls_producer");
    }

    private final long config;

    private static boolean hasSoLoaded = false;

    private LogProducerConfig(Context context, String endpoint, String project, String logstore)
        throws LogProducerException {
        if (!hasSoLoaded) {
            SoLoader.instance().loadLibrary(context, "sls_producer");
            hasSoLoaded = true;
        }

        config = create_log_producer_config();
        if (config == 0) {
            throw new LogProducerException("Can not create log producer config");
        }
        log_producer_config_set_endpoint(config, endpoint);
        log_producer_config_set_project(config, project);
        log_producer_config_set_logstore(config, logstore);

        setPacketTimeout(3000);
        setPacketLogCount(1024);
        setPacketLogBytes(1024 * 1024);
        setSendThreadCount(1);

        setGetTimeUnixFunc(new LogProducerTimeUnixFunc() {
            @Override
            public long getTimeUnix() {
                long deltaTime = LogProducerHttpTool.localServerDeltaTime.get();
                long sysTime = System.currentTimeMillis() / 1000;
                return sysTime + deltaTime;
            }
        });
    }

    @Deprecated
    public LogProducerConfig(String endpoint, String project, String logstore, String accessKeyID,
        String accessKeySecret) throws LogProducerException {
        this((Context)null, endpoint, project, logstore, accessKeyID, accessKeySecret);
    }

    public LogProducerConfig(Context context, String endpoint, String project, String logstore, String accessKeyID,
        String accessKeySecret) throws LogProducerException {
        this(context, endpoint, project, logstore);
        log_producer_config_set_access_id(config, accessKeyID);
        log_producer_config_set_access_key(config, accessKeySecret);
    }

    @Deprecated
    public LogProducerConfig(String endpoint, String project, String logstore, String accessKeyID,
        String accessKeySecret, String securityToken) throws LogProducerException {
        this(null, endpoint, project, logstore, accessKeyID, accessKeySecret, securityToken);
    }

    public LogProducerConfig(Context context, String endpoint, String project, String logstore, String accessKeyID,
        String accessKeySecret, String securityToken) throws LogProducerException {
        this(context, endpoint, project, logstore);
        this.resetSecurityToken(accessKeyID, accessKeySecret, securityToken);
    }

    public void setTopic(String topic) {
        log_producer_config_set_topic(config, topic);
    }

    public void addTag(String key, String value) {
        log_producer_config_add_tag(config, key, value);
    }

    public void setPacketLogBytes(int num) {
        log_producer_config_set_packet_log_bytes(config, num);
    }

    public void setPacketLogCount(int num) {
        log_producer_config_set_packet_log_count(config, num);
    }

    public void setPacketTimeout(int num) {
        log_producer_config_set_packet_timeout(config, num);
    }

    public void setMaxBufferLimit(int num) {
        log_producer_config_set_max_buffer_limit(config, num);
    }

    public void setSendThreadCount(int num) {
        log_producer_config_set_send_thread_count(config, num);
    }

    public void setPersistent(int num) {
        log_producer_config_set_persistent(config, num);
    }

    public void setPersistentFilePath(String path) {
        log_producer_config_set_persistent_file_path(config, path);
    }

    public void setPersistentForceFlush(int num) {
        log_producer_config_set_persistent_force_flush(config, num);
    }

    public void setPersistentMaxFileCount(int num) {
        log_producer_config_set_persistent_max_file_count(config, num);
    }

    public void setPersistentMaxFileSize(int num) {
        log_producer_config_set_persistent_max_file_size(config, num);
    }

    public void setPersistentMaxLogCount(int num) {
        log_producer_config_set_persistent_max_log_count(config, num);
    }

    public void setUsingHttp(int num) {
        log_producer_config_set_using_http(config, num);
    }

    public void setNetInterface(String netInterface) {
        log_producer_config_set_net_interface(config, netInterface);
    }

    public void setConnectTimeoutSec(int num) {
        log_producer_config_set_connect_timeout_sec(config, num);
    }

    public void setSendTimeoutSec(int num) {
        log_producer_config_set_send_timeout_sec(config, num);
    }

    public void setDestroyFlusherWaitSec(int num) {
        log_producer_config_set_destroy_flusher_wait_sec(config, num);
    }

    public void setDestroySenderWaitSec(int num) {
        log_producer_config_set_destroy_sender_wait_sec(config, num);
    }

    public void setCompressType(int num) {
        log_producer_config_set_compress_type(config, num);
    }

    public void setNtpTimeOffset(int num) {
        log_producer_config_set_ntp_time_offset(config, num);
    }

    public void setMaxLogDelayTime(int num) {
        log_producer_config_set_max_log_delay_time(config, num);
    }

    public void setDropDelayLog(int num) {
        log_producer_config_set_drop_delay_log(config, num);
    }

    public void setDropUnauthorizedLog(int num) {
        log_producer_config_set_drop_unauthorized_log(config, num);
    }

    public void setGetTimeUnixFunc(LogProducerTimeUnixFunc func) {
        log_producer_config_set_get_time_unix_func(func);
    }

    public void resetSecurityToken(String accessKeyID, String accessKeySecret, String securityToken) {
        log_producer_config_reset_security_token(config, accessKeyID, accessKeySecret, securityToken);
    }

    public void logProducerDebug() {
        log_producer_debug();
    }

    long getConfig() {
        return config;
    }

    public int isValid() {
        return log_producer_config_is_valid(config);
    }

    public int isEnabled() {
        return log_producer_persistent_config_is_enabled(config);
    }

    private static native long create_log_producer_config();

    private static native void log_producer_debug();

    private static native void log_producer_config_set_endpoint(long config, String endpoint);

    private static native void log_producer_config_set_project(long config, String project);

    private static native void log_producer_config_set_logstore(long config, String logstore);

    private static native void log_producer_config_set_access_id(long config, String accessKeyID);

    private static native void log_producer_config_set_access_key(long config, String accessKeySecret);

    private static native void log_producer_config_reset_security_token(long config, String accessKeyID, String accessKeySecret, String securityToken);

    private static native void log_producer_config_set_topic(long config, String topic);

    private static native void log_producer_config_add_tag(long config, String key, String value);

    private static native void log_producer_config_set_packet_log_bytes(long config, int num);

    private static native void log_producer_config_set_packet_log_count(long config, int num);

    private static native void log_producer_config_set_packet_timeout(long config, int num);

    private static native void log_producer_config_set_max_buffer_limit(long config, int num);

    private static native void log_producer_config_set_send_thread_count(long config, int num);

    private static native void log_producer_config_set_persistent(long config, int num);

    private static native void log_producer_config_set_persistent_file_path(long config, String path);

    private static native void log_producer_config_set_persistent_force_flush(long config, int num);

    private static native void log_producer_config_set_persistent_max_file_count(long config, int num);

    private static native void log_producer_config_set_persistent_max_file_size(long config, int num);

    private static native void log_producer_config_set_persistent_max_log_count(long config, int num);

    private static native void log_producer_config_set_using_http(long config, int num);

    private static native void log_producer_config_set_net_interface(long config, String net_interface);

    private static native void log_producer_config_set_connect_timeout_sec(long config, int num);

    private static native void log_producer_config_set_send_timeout_sec(long config, int num);

    private static native void log_producer_config_set_destroy_flusher_wait_sec(long config, int num);

    private static native void log_producer_config_set_destroy_sender_wait_sec(long config, int num);

    private static native void log_producer_config_set_compress_type(long config, int num);

    private static native void log_producer_config_set_ntp_time_offset(long config, int num);

    private static native void log_producer_config_set_max_log_delay_time(long config, int num);

    private static native void log_producer_config_set_drop_delay_log(long config, int num);

    private static native void log_producer_config_set_get_time_unix_func(LogProducerTimeUnixFunc func);

    private static native void log_producer_config_set_drop_unauthorized_log(long config, int num);

    private static native int log_producer_config_is_valid(long config);

    private static native int log_producer_persistent_config_is_enabled(long config);

}
