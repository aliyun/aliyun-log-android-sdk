package com.aliyun.sls.android.plugin.trace;

import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.plugin.AbstractPlugin;

/**
 * @author gordon
 * @date 2021/07/29
 */
public class SLSTracePlugin extends AbstractPlugin {

    private SLSTelemetry slsTelemetry;
    private SLSSpanExporter spanExporter;

    private static class Holder {
        private static final SLSTracePlugin INSTANCE = new SLSTracePlugin();
    }

    public static SLSTracePlugin getInstance() {
        return Holder.INSTANCE;
    }

    private SLSTracePlugin() {
        //no instance
    }

    @Override
    public String name() {
        return "trace";
    }

    @Override
    public String version() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public void init(SLSConfig config) {
        spanExporter = new SLSSpanExporter();
        spanExporter.init(config);

        slsTelemetry = new SLSTelemetry(config, spanExporter);
    }

    public SLSTelemetry getSLSTelemetry() {
        return slsTelemetry;
    }

    @Override
    public void resetSecurityToken(String accessKeyId, String accessKeySecret, String securityToken) {
        super.resetSecurityToken(accessKeyId, accessKeySecret, securityToken);
        spanExporter.resetSecurityToken(accessKeyId, accessKeySecret, securityToken);
    }

    @Override
    public void resetProject(String endpoint, String project, String logstore) {
        super.resetProject(endpoint, project, logstore);
        spanExporter.resetProject(endpoint, project, logstore);
    }

    @Override
    public void updateConfig(SLSConfig config) {
        super.updateConfig(config);
        spanExporter.updateConfig(config);
    }
}


