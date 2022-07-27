package com.aliyun.sls.android.blockdetection;

import android.content.Context;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.feature.SdkFeature;
import com.aliyun.sls.android.core.utils.AppUtils;
import com.efs.sdk.base.EfsReporter;
import com.efs.sdk.base.WPKReporter;
import com.efs.sdk.base.listener.IWPKLogListener;
import com.efs.sdk.base.protocol.ILogProtocol;
import com.efs.sdk.pa.PAFactory;
import com.efs.sdk.pa.PAFactory.Builder;
import com.efs.sdk.pa.config.IWPKReporter;
import com.efs.sdk.pa.config.PackageLevel;

/**
 * @author gordon
 * @date 2022/7/21
 */
public class BlockDetectionFeature extends SdkFeature {
    private static final String TAG = "BlockDetectionFeature";

    @Override
    protected void onInitialize(Context context, Credentials credentials, Configuration configuration) {
        final EfsReporter reporter = new EfsReporter.Builder(context, "sls-" + credentials.instanceId,
            credentials.instanceId)
            .uid(configuration.userInfo.uid)
            .debug(true)
            .printLogDetail(true)
            //.enableSendLog(false)
            .build();

        //reporter.getWPKReporter().addLogListener(new IWPKLogListener() {
        //    @Override
        //    public void onLogGenerate(ILogProtocol iLogProtocol) {
        //        SLSLog.v(TAG, "jank type: " + iLogProtocol.getLogType() + ", file path: " + iLogProtocol.getFilePath()
        //            + ", jank log: " + iLogProtocol.generateString());
        //        // TODO: 2022/7/22 report to sls
        //    }
        //});

        PAFactory.Builder builder = new Builder(context, new IWPKReporter() {
            @Override
            public WPKReporter getReporter() {
                return reporter.getWPKReporter();
            }
        });
        builder.packageLevel(PackageLevel.TRIAL);
        builder.serial(AppUtils.getAppVersion(context));
        builder.timeoutTime(1000);
        PAFactory factory = builder.build();
        factory.getPaInstance().start();
    }

    @Override
    protected void onPostInitialize(Context context) {

    }

    @Override
    protected void onStop(Context context) {

    }

    @Override
    protected void onPostStop(Context context) {

    }
}
