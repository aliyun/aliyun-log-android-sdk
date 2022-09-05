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
import com.efs.sdk.pa.PA;
import com.efs.sdk.pa.PAFactory;
import com.efs.sdk.pa.PAFactory.Builder;
import com.efs.sdk.pa.config.PackageLevel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2022/7/21
 */
@SuppressWarnings("unused")
public class BlockDetectionFeature extends SdkFeature {
    private static final String TAG = "BlockDetectionFeature";
    // 20% 的卡顿采样率
    private static final int DEFAULT_SAMPLING = 20;

    private PA pa;

    private String createConfig(String appId) {
        JSONObject config = new JSONObject();
        try {
            config.put("config_type", "client_core");
            config.put("app_id", appId);
            config.put("update_tm", System.currentTimeMillis() / 1000);
            config.put("cver", 1);
            config.put("config", new JSONObject());
            config.getJSONObject("config").put("app_configs", new JSONArray());
            config.getJSONObject("config").getJSONArray("app_configs").put(new JSONObject());

            config.getJSONObject("config").getJSONArray("app_configs").getJSONObject(0).put("conditions",
                new JSONArray());
            config.getJSONObject("config").getJSONArray("app_configs").getJSONObject(0).getJSONArray("conditions").put(
                new JSONObject());
            config.getJSONObject("config").getJSONArray("app_configs").getJSONObject(0).getJSONArray("conditions")
                .getJSONObject(0).put("fld", "ver");
            config.getJSONObject("config").getJSONArray("app_configs").getJSONObject(0).getJSONArray("conditions")
                .getJSONObject(0).put("opc", ">=");
            config.getJSONObject("config").getJSONArray("app_configs").getJSONObject(0).getJSONArray("conditions")
                .getJSONObject(0).put("val", "0.0.0.0");

            config.getJSONObject("config").getJSONArray("app_configs").getJSONObject(0).put("actions", new JSONArray());
            config.getJSONObject("config").getJSONArray("app_configs").getJSONObject(0).getJSONArray("actions").put(
                new JSONObject());
            config.getJSONObject("config").getJSONArray("app_configs").getJSONObject(0).getJSONArray("actions")
                .getJSONObject(0).put("opt", "apm_patrace_switch_rate");
            config.getJSONObject("config").getJSONArray("app_configs").getJSONObject(0).getJSONArray("actions")
                .getJSONObject(0).put("set", DEFAULT_SAMPLING);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return config.toString();
    }

    @Override
    protected void onInitialize(Context context, Credentials credentials, Configuration configuration) {
        final String appId = "sls-" + credentials.instanceId;
        final boolean debuggable = configuration.debuggable && AppUtils.debuggable(context);

        final EfsReporter reporter = new EfsReporter.Builder(context, appId, "a91b2c72c00bb50d")
            //.uid(configuration.userInfo.uid)
            .debug(debuggable)
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
                        Pair.create("ex.uuid", IdGenerator.generateSpanId())
                    )
                );
            } else {
                return;
            }

            builder.build().end();
        });
        reporter.getWPKReporter().setConfigRefreshAction(() -> {
            if (debuggable) {
                return "";
            } else {
                return createConfig(appId);
            }
        });

        PAFactory.Builder builder = new Builder(context, reporter::getWPKReporter);
        builder.packageLevel(PackageLevel.TRIAL);
        builder.serial(AppUtils.getAppVersion(context));
        builder.timeoutTime(2000);
        PAFactory factory = builder.build();
        pa = factory.getPaInstance();
        pa.start();

        BlockDetection.setBlockDetectionFeature(this);
    }

    @Override
    public void setFeatureEnabled(boolean enable) {
        super.setFeatureEnabled(enable);
        if (null == pa) {
            return;
        }

        if (enable) {
            pa.start();
            SLSLog.d(TAG, "BlockDetectionFeature enabled.");
        } else {
            pa.stop();
            SLSLog.d(TAG, "BlockDetectionFeature disabled.");
        }
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
