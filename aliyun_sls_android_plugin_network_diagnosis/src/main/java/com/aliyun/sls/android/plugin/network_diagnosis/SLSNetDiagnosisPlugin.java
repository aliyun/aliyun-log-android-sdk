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

    private ISender sender;
    private SLSNetDiagnosis netDiagnosis;

    public SLSNetDiagnosisPlugin() {
        netDiagnosis = SLSNetDiagnosis.getInstance();
    }

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
        sender = new SLSNetDataSender();
        sender.init(config);
        netDiagnosis.init(config, sender);
    }

    @Override
    public void updateConfig(SLSConfig config) {
        super.updateConfig(config);
        netDiagnosis.updateConfig(config);
    }
}
