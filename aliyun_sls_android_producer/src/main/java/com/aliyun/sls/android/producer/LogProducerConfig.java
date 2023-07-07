package com.aliyun.sls.android.producer;

import java.io.File;

import android.content.Context;
import android.text.TextUtils;
import com.aliyun.sls.android.producer.internal.HttpHeader;
import com.aliyun.sls.android.producer.internal.LogProducerHttpHeaderInjector;
import com.aliyun.sls.android.producer.utils.ProcessUtils;
import com.aliyun.sls.android.producer.utils.TimeUtils;
import com.aliyun.sls.android.producer.utils.Utils;

@SuppressWarnings({"AlibabaLowerCamelCaseVariableNaming", "unused"})
public class LogProducerConfig {
    static {
        System.loadLibrary("sls_producer");
    }

    private final long config;
    private final Context context;
    private String endpoint;
    private String project;
    private String logstore;
    private boolean enablePersistent = false;

    public LogProducerConfig() throws LogProducerException {
        this(Utils.getContext());
    }

    public LogProducerConfig(String endpoint, String project, String logstore) throws LogProducerException {
        this(Utils.getContext(), endpoint, project, logstore);
    }

    // @formatter:off
    public LogProducerConfig(String endpoint, String project, String logstore, String accessKeyId, String accessKeySecret) throws LogProducerException {
        this(endpoint, project, logstore, accessKeyId, accessKeySecret, null);
    }

    // @formatter:off
    public LogProducerConfig(String endpoint, String project, String logstore, String accessKeyId, String accessKeySecret, String securityToken) throws LogProducerException {
        this(Utils.getContext(), endpoint, project, logstore, accessKeyId, accessKeySecret, securityToken);
    }

    public LogProducerConfig(Context context) throws LogProducerException {
        this(context, null, null, null);
    }

    // @formatter:off
    public LogProducerConfig(Context context, String endpoint, String project, String logstore) throws LogProducerException {
        this(context, endpoint, project, logstore, null, null);
    }

    // @formatter:off
    public LogProducerConfig(Context context, String endpoint, String project, String logstore, String accessKeyId, String accessKeySecret) throws LogProducerException {
        this(context, endpoint, project, logstore, accessKeyId, accessKeySecret, null);
    }

    // @formatter:off
    public LogProducerConfig(Context context, String endpoint, String project, String logstore, String accessKeyId, String accessKeySecret, String securityToken) throws LogProducerException {
        this.context = context;
        config = create_log_producer_config();
        if (config == 0) {
            throw new LogProducerException("Can not create log producer config");
        }

        // default configuration
        setSource("Android");
        setPacketTimeout(3000);
        setPacketLogCount(1024);
        setPacketLogBytes(1024 * 1024);
        setSendThreadCount(1);
        setDropUnauthorizedLog(0);
        setDropDelayLog(0);
        setGetTimeUnixFunc(new LogProducerTimeUnixFunc() {
            @Override
            public long getTimeUnix() {
                return TimeUtils.getTimeInMillis();
            }
        });
        setHttpHeaderInjector(new LogProducerHttpHeaderInjector() {
            @Override
            public String[] injectHeaders(String[] srcHeaders, int count) {
                return HttpHeader.getHeadersWithUA(srcHeaders);
            }
        });

        // user configuration
        setEndpoint(endpoint);
        setProject(project);
        setLogstore(logstore);
        setAccessKeyId(accessKeyId);
        setAccessKeySecret(accessKeySecret);

        if (!TextUtils.isEmpty(securityToken)) {
            resetSecurityToken(accessKeyId, accessKeySecret, securityToken);
        }
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

    private static native void log_producer_config_set_callback_from_sender_thread(long config, int num);

    private static native void log_producer_config_set_source(long config, String source);

    private static native int log_producer_config_is_valid(long config);

    private static native int log_producer_persistent_config_is_enabled(long config);

    private static native void log_producer_config_set_http_header_inject(long config, LogProducerHttpHeaderInjector injector);

    public Context getContext() {
        return context;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        log_producer_config_set_endpoint(config, endpoint);
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setProject(String project) {
        this.project = project;
        log_producer_config_set_project(config, project);
    }

    public String getProject() {
        return project;
    }

    public void setLogstore(String logstore) {
        this.logstore = logstore;
        log_producer_config_set_logstore(config, logstore);
    }

    public String getLogstore() {
        return logstore;
    }

    public void setTopic(String topic) {
        log_producer_config_set_topic(config, topic);
    }

    public void addTag(String key, String value) {
        log_producer_config_add_tag(config, key, value);
    }

    public void setAccessKeyId(String accessId) {
        log_producer_config_set_access_id(config, accessId);
    }

    public void setAccessKeySecret(String accessKeySecret) {
        log_producer_config_set_access_key(config, accessKeySecret);
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
        // force set 1 send thread
        if (enablePersistent && 1 != num) {
            num = 1;
        }
        log_producer_config_set_send_thread_count(config, num);
    }

    public void setPersistent(int num) {
        this.enablePersistent = 1 == num;
        log_producer_config_set_persistent(config, num);

        // force set 1 send thread
        if (enablePersistent) {
            setSendThreadCount(1);
        }
    }

    public void setPersistentFilePath(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        // create parent folder if no exists
        createParentFolderIfNotExists(path);

        path = createNewPathIfInProcess(path);
        log_producer_config_set_persistent_file_path(config, path);
    }

    private void createParentFolderIfNotExists(String path) {
        File parent = new File(path.substring(0, path.lastIndexOf(File.separator)));
        if (!parent.exists()) {
            boolean ignored = parent.mkdirs();
        }
    }

    private String createNewPathIfInProcess(String path) {
        if (null == context || ProcessUtils.isMainProcess(context)) {
            return path;
        }

        String processName = ProcessUtils.getCurrentProcessName(context);
        if (TextUtils.isEmpty(processName)) {
            return path;
        }

        File parent = new File(path.substring(0, path.lastIndexOf(File.separator)), processName);
        String lastPath = path.substring(path.lastIndexOf(File.separator)+1);
        if (!parent.exists()) {
            boolean ignored = parent.mkdirs();
        }

        return new File(parent, lastPath).getAbsolutePath();
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

    @Deprecated
    public void setCompressType(int num) {
        log_producer_config_set_compress_type(config, num);
    }

    public void setCompressType(CompressType type) {
        log_producer_config_set_compress_type(config, type.type);
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

    public void setCallbackFromSenderThread(boolean enable) {
        log_producer_config_set_callback_from_sender_thread(config, enable ? 1 : 0);
    }

    public void setGetTimeUnixFunc(LogProducerTimeUnixFunc func) {
        log_producer_config_set_get_time_unix_func(func);
    }

    public void setSource(String source) {
        log_producer_config_set_source(config, source);
    }

    public void resetSecurityToken(String accessKeyID, String accessKeySecret, String securityToken) {
        log_producer_config_reset_security_token(config, accessKeyID, accessKeySecret, securityToken);
    }

    @Deprecated
    public void setUseWebtracking(boolean enable) {
        // no longer support
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

    public void setHttpHeaderInjector(LogProducerHttpHeaderInjector injector) {
        log_producer_config_set_http_header_inject(config, injector);
    }

    public enum CompressType {
        LZ4(1),
        ZSTD(2);

        final int type;
        CompressType(int type) {
            this.type = type;
        }
    }
}
