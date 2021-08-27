package com.aliyun.sls.android.plugin.network_monitor;

import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.plugin.AbstractPlugin;
import com.aliyun.sls.android.plugin.ISender;
import com.aliyun.sls.android.plugin.network_monitor.sender.SLSNetDataSender;

/**
 * @author gordon
 * @date 2021/08/26
 */
public class SLSNetworkMonitorPlugin extends AbstractPlugin {
    private static final String TAG = "SLSNetworkMonitorPlugin";

    @Override
    public String name() {
        return "network_monitor";
    }

    @Override
    public String version() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public void init(SLSConfig config) {
        ISender sender = new SLSNetDataSender();
        sender.init(config);
        SLSNetwork.getInstance().init(config, sender);
    }
}
