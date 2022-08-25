package com.aliyun.sls.android.blockdetection;

import java.io.UnsupportedEncodingException;

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
import com.aliyun.sls.android.ot.utils.IdGenerator;
import com.efs.sdk.base.EfsReporter;
import com.efs.sdk.pa.PAFactory;
import com.efs.sdk.pa.PAFactory.Builder;
import com.efs.sdk.pa.config.PackageLevel;

/**
 * @author gordon
 * @date 2022/7/21
 */
@SuppressWarnings("unused")
public class BlockDetectionFeature extends SdkFeature {
    private static final String TAG = "BlockDetectionFeature";

    @Override
    protected void onInitialize(Context context, Credentials credentials, Configuration configuration) {
        final EfsReporter reporter = new EfsReporter.Builder(context, "sls-" + credentials.instanceId,  "a91b2c72c00bb50d")
            //.uid(configuration.userInfo.uid)
            .debug(true)
            .printLogDetail(true)
            .enableSendLog(false)
            .build();

        reporter.getWPKReporter().addLogListener(iLogProtocol -> {
            String data;
            try {
                data = new String(iLogProtocol.generate(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return;
            }

            SLSLog.v(TAG,
                "jank type: " + iLogProtocol.getLogType()
                + ", file path: " + iLogProtocol.getFilePath()
                + ", jank log: " + data
            );

            SpanBuilder builder = newSpanBuilder("block");

            final String type = iLogProtocol.getLogType();
            if (TextUtils.equals("patrace", type) || TextUtils.equals("patracepv", type)) {
                builder.addAttribute(
                  Attribute.of(
                      Pair.create("t", "error"),
                      Pair.create("ex.type", "block"),
                      Pair.create("ex.sub_type", type),
                      Pair.create("ex.origin", data),
                      Pair.create("ex.seq", IdGenerator.generateSpanId())
                  )
                );
            } else {
                return;
            }

            builder.build().end();
        });
        reporter.getWPKReporter().setConfigRefreshAction(() -> null);

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
