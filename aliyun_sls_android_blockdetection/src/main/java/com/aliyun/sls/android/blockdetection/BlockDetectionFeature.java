package com.aliyun.sls.android.blockdetection;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.feature.SdkFeature;
import com.aliyun.sls.android.core.utils.AppUtils;
import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.SpanBuilder;
import com.efs.sdk.base.EfsReporter;
import com.efs.sdk.base.IConfigRefreshAction;
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
        final EfsReporter reporter = new EfsReporter.Builder(context, "sls-" + credentials.instanceId,  "0123456789112345")
            //.uid(configuration.userInfo.uid)
            .debug(true)
            .printLogDetail(true)
            .enableSendLog(false)
            .build();

        reporter.getWPKReporter().addLogListener(iLogProtocol -> {
            SLSLog.v(TAG, "jank type: " + iLogProtocol.getLogType() + ", file path: " + iLogProtocol.getFilePath()
                + ", jank log: " + iLogProtocol.generateString());

            SpanBuilder builder = newSpanBuilder("block");

            final String type = iLogProtocol.getLogType();
            if (TextUtils.equals("patrace", type) || TextUtils.equals("patracepv", type)) {
                builder.addAttribute(
                  Attribute.of(
                      Pair.create("t", "block"),
                      Pair.create("ex.type", type),
                      Pair.create("ex.origin", iLogProtocol.generateString())
                  )
                );
            } else {
                return;
            }

            builder.build().end();
        });
        reporter.getWPKReporter().setConfigRefreshAction(new IConfigRefreshAction() {
            @Override
            public String refresh() {
                return null;
            }
        });

        PAFactory.Builder builder = new Builder(context, reporter::getWPKReporter);
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
