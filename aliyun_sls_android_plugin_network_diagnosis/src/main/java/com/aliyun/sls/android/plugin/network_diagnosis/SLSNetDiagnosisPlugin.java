package com.aliyun.sls.android.plugin.network_diagnosis;

import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.plugin.AbstractPlugin;
import com.aliyun.sls.android.plugin.ISender;
import com.aliyun.sls.android.plugin.network_diagnosis.sender.SLSNetDataSender;

/**
 * @author gordon
 * @date 2021/08/26
 */
public class SLSNetDiagnosisPlugin extends AbstractPlugin {
    private static final String TAG = "SLSNetDiagnosisPlugin";

    @Override
    public String name() {
        return "network_diagnosis";
    }

    @Override
    public String version() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public void init(SLSConfig config) {
        ISender sender = new SLSNetDataSender();
        sender.init(config);
        SLSNetDiagnosis.getInstance().init(config, sender);
    }
}
