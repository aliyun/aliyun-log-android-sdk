package com.aliyun.sls.android.plugin.trace;

import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.plugin.AbstractPlugin;
import com.aliyun.sls.android.plugin.trace.BuildConfig;

/**
 * @author gordon
 * @date 2021/07/29
 */
public class SLSTracePlugin extends AbstractPlugin {

    private SLSTelemetrySdk telemetrySdk;

    private static class Holder {
        private static SLSTracePlugin INSTANCE = new SLSTracePlugin();
    }

    public static SLSTracePlugin getInstance() {
        return Holder.INSTANCE;
    }

    private SLSTracePlugin() {
        //no instance
    }

    @Override
    public String name() {
        return "SLSTracePlugin";
    }

    @Override
    public String version() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public void init(SLSConfig config) {
        telemetrySdk = new SLSTelemetrySdk(config);
    }

    public SLSTelemetrySdk getTelemetrySdk() {
        return telemetrySdk;
    }
}


