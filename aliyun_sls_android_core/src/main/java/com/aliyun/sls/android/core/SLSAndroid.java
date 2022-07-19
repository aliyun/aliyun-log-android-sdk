package com.aliyun.sls.android.core;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.configuration.UserInfo;
import com.aliyun.sls.android.core.feature.Feature;
import com.aliyun.sls.android.core.sender.SdkSender;
import com.aliyun.sls.android.core.utdid.Utdid;
import com.aliyun.sls.android.core.utils.AppUtils;
import com.aliyun.sls.android.core.utils.DeviceUtils;
import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.ISpanProvider;
import com.aliyun.sls.android.ot.Resource;

/**
 * @author gordon
 * @date 2022/7/18
 */
public final class SLSAndroid {
    private static final String TAG = "SLSAndroid";

    // region initialize
    private final static AtomicBoolean hasInitialized = new AtomicBoolean(false);

    public static boolean initialize(
        final Context context,
        final Credentials credentials,
        final OptionConfiguration optionConfiguration
    ) {
        if (null == optionConfiguration) {
            SLSLog.w(TAG, "OptionConfiguration must not be null.");
            return false;
        }

        if (hasInitialized.get()) {
            SLSLog.w(TAG, "SLSAndroid has been initialized.");
            return false;
        }

        Configuration configuration = new Configuration();
        optionConfiguration.onConfiguration(configuration);
        initializeDefaultSpanProvider(configuration, credentials, context);
        initializeDefaultSpanProcessor(context, credentials, configuration);

        initCrashReporterFeature(context, credentials, configuration);

        hasInitialized.set(true);

        Runtime.getRuntime().addShutdownHook(new Thread(SLSAndroid::stop, "SLS_ANDROID_SHUTDOWN"));
        return true;
    }

    private static void initializeDefaultSpanProvider(final Configuration configuration, final Credentials credentials,
        final Context context) {
        final ISpanProvider userSpanProvider = configuration.spanProvider;
        configuration.spanProvider = new ISpanProvider() {
            @Override
            public Resource provideResource() {
                Resource resource = Resource.of(
                    Pair.create("device.id", Utdid.getInstance().getUtdid(context)),
                    Pair.create("app.version", AppUtils.getAppVersion(context)),
                    Pair.create("app.versionCode", AppUtils.getAppVersionCode(context)),
                    Pair.create("app.name", AppUtils.getAppName(context)),
                    Pair.create("device.resolution", DeviceUtils.getResolution(context)),
                    Pair.create("net.access", DeviceUtils.getAccessName(context)),
                    Pair.create("net.access_subtype", DeviceUtils.getAccessSubTypeName(context)),
                    Pair.create("carrier", DeviceUtils.getCarrier(context)),
                    Pair.create("os.root", DeviceUtils.isRoot())
                );

                if (null != userSpanProvider) {
                    Resource r = userSpanProvider.provideResource();
                    if (null != r) {
                        resource.merge(r);
                    }
                }

                return resource;
            }

            @Override
            public List<Attribute> provideAttribute() {
                List<Attribute> attributes = Attribute.of(
                    Pair.create("page.name", AppUtils.getTopActivity()),
                    Pair.create("foreground", AppUtils.isForeground()),
                    Pair.create("instance", credentials.instanceId),
                    Pair.create("env", TextUtils.isEmpty(configuration.env) ? "default" : configuration.env)
                );

                if (null != configuration.userInfo) {
                    provideUserInfo(attributes, configuration.userInfo);
                }

                if (null != userSpanProvider) {
                    List<Attribute> userAttributes = userSpanProvider.provideAttribute();
                    if (null != userAttributes) {
                        attributes.addAll(userAttributes);
                    }
                }

                return attributes;
            }

            private void provideUserInfo(List<Attribute> attributes, UserInfo userInfo) {
                if (!TextUtils.isEmpty(userInfo.uid)) {
                    attributes.add(Attribute.of("user.uid", userInfo.uid));
                }
                if (!TextUtils.isEmpty(userInfo.channel)) {
                    attributes.add(Attribute.of("user.channel", userInfo.channel));
                }
                if (userInfo.ext.size() != 0) {
                    for (Entry<String, String> entry : userInfo.ext.entrySet()) {
                        String k = entry.getKey();
                        String v = entry.getValue();
                        if (TextUtils.isEmpty(k)) {
                            continue;
                        }
                        attributes.add(Attribute.of("user." + k, v));
                    }
                }
            }
        };
    }

    private static void initializeDefaultSpanProcessor(
        final Context context,
        final Credentials credentials,
        final Configuration configuration
    ) {
        final SdkSender spanProcessor = new SdkSender(context);
        spanProcessor.initialize(credentials);
        configuration.spanProcessor = spanProcessor;
    }

    private static void initCrashReporterFeature(
        final Context context,
        final Credentials credentials,
        final Configuration configuration
    ) {
        if (!configuration.enableCrashReporter) {
            return;
        }

        try {
            Feature feature = (Feature)Class
                .forName("com.aliyun.sls.android.crashreporter.CrashReporterFeature")
                .newInstance();
            feature.initialize(context, credentials, configuration);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    // endregion

    /**
     * Sets this library log level
     *
     * @param level one of the Android {@link android.util.Log} constants
     *              ({@link android.util.Log#VERBOSE}, {@link android.util.Log#DEBUG}, {@link android.util.Log#INFO},
     *              {@link android.util.Log#WARN}, {@link android.util.Log#ERROR}, {@link android.util.Log#ASSERT}).
     */
    public void setLogLevel(int level) {
        SLSLog.setLevel(level);
    }

    public static void setCredentials(Credentials credentials) {

    }

    public static void stop() {

    }

    public interface OptionConfiguration {
        void onConfiguration(Configuration configuration);
    }
}
