package com.aliyun.sls.android.core;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.aliyun.sls.android.core.sender.Sender;
import com.aliyun.sls.android.core.utdid.Utdid;
import com.aliyun.sls.android.core.utils.AppUtils;
import com.aliyun.sls.android.core.utils.DeviceUtils;
import com.aliyun.sls.android.core.utils.JsonUtil;
import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.ISpanProvider;
import com.aliyun.sls.android.ot.Resource;

/**
 * @author gordon
 * @date 2022/7/18
 */
public final class SLSAndroid {
    private static final String TAG = "SLSAndroid";
    private static Configuration configuration;
    private static Credentials credentials;
    private static final List<Feature> features = new ArrayList<>();
    private static final ExtraProvider extraProvider = new ExtraProvider();

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

        SLSAndroid.credentials = credentials;
        configuration = new Configuration(new SdkSender(context));
        optionConfiguration.onConfiguration(configuration);

        initializeDefaultSpanProvider(context);
        if (configuration.enableCrashReporter || configuration.enableBlockDetection) {
            initializeSdkSender(context);
        }

        initCrashReporterFeature(context, credentials, configuration);
        initBlockDetectionFeature(context, credentials, configuration);
        initNetworkDiagnosisFeature(context, credentials, configuration);

        hasInitialized.set(true);

        Runtime.getRuntime().addShutdownHook(new Thread(SLSAndroid::stop, "SLS_ANDROID_SHUTDOWN"));
        return true;
    }

    private static void initializeDefaultSpanProvider(final Context context) {
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

                provideExtras(attributes);

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

            @SuppressWarnings("rawtypes")
            private void provideExtras(List<Attribute> attributes) {
                final Map<String, Object> extras = new LinkedHashMap<>(extraProvider.extras);
                if (extras.isEmpty()) {
                    return;
                }

                for (Entry<String, Object> entry : extras.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        attributes.add(Attribute.of("extras." + entry.getKey(), JsonUtil.fromMap((Map)entry.getValue())));
                    } else {
                        attributes.add(Attribute.of("extras." + entry.getKey(), entry.getValue().toString()));
                    }
                }
            }
        };
    }

    private static void initializeSdkSender(final Context context) {
        final SdkSender spanProcessor = (SdkSender)configuration.spanProcessor;
        spanProcessor.initialize(credentials);
    }

    private static void initCrashReporterFeature(
        final Context context,
        final Credentials credentials,
        final Configuration configuration
    ) {
        if (!configuration.enableCrashReporter) {
            return;
        }

        initFeature(context, credentials, configuration, "com.aliyun.sls.android.crashreporter.CrashReporterFeature");
    }

    private static void initBlockDetectionFeature(
        final Context context,
        final Credentials credentials,
        final Configuration configuration
    ) {
        if (!configuration.enableBlockDetection) {
            return;
        }

        initFeature(context, credentials, configuration, "com.aliyun.sls.android.blockdetection.BlockDetectionFeature");
    }

    private static void initNetworkDiagnosisFeature(
        final Context context,
        final Credentials credentials,
        final Configuration configuration
    ) {
        if (!configuration.enableNetworkDiagnosis) {
            return;
        }

        initFeature(context, credentials, configuration, "com.aliyun.sls.android.network_diagnosis.NetworkDiagnosisFeature");
    }

    private static boolean initFeature(
        final Context context,
        final Credentials credentials,
        final Configuration configuration,
        final String clazzName
    ) {
        try {
            Feature feature = (Feature)Class.forName(clazzName).newInstance();
            if (null == feature) {
                return false;
            }
            feature.initialize(context, credentials, configuration);
            features.add(feature);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    // endregion

    // region setter

    /**
     * Sets this library log level
     *
     * @param level one of the Android {@link android.util.Log} constants
     *              ({@link android.util.Log#VERBOSE}, {@link android.util.Log#DEBUG}, {@link android.util.Log#INFO},
     *              {@link android.util.Log#WARN}, {@link android.util.Log#ERROR}, {@link android.util.Log#ASSERT}).
     */
    public static void setLogLevel(int level) {
        SLSLog.setLevel(level);
    }

    public static void setCredentials(Credentials credentials) {
        if (null == credentials) {
            return;
        }

        // set credentials field if not empty
        if (!TextUtils.isEmpty(credentials.instanceId)) {
            SLSAndroid.credentials.instanceId = credentials.instanceId;
        }
        if (null != credentials.endpoint) {
            SLSAndroid.credentials.endpoint = credentials.endpoint;
        }
        if (!TextUtils.isEmpty(credentials.project)) {
            SLSAndroid.credentials.project = credentials.project;
        }
        if (!TextUtils.isEmpty(credentials.accessKeyId)
            || !TextUtils.isEmpty(credentials.accessKeySecret)
            || !TextUtils.isEmpty(credentials.securityToken)
        ) {
            SLSAndroid.credentials.accessKeyId = credentials.accessKeyId;
            SLSAndroid.credentials.accessKeySecret = credentials.accessKeySecret;
            SLSAndroid.credentials.securityToken = credentials.securityToken;
        }

        // set default sender's credentials
        if (configuration.spanProcessor instanceof Sender) {
            ((Sender)configuration.spanProcessor).setCredentials(credentials);
        }

        // set all Feature's credentials
        for (Feature feature : features) {
            feature.setCredentials(credentials);
        }
    }

    public static void setUserInfo(UserInfo info) {
        if (null == configuration) {
            return;
        }

        configuration.userInfo = info;
    }

    public static void setExtra(String key, Map<String, String> values) {
        extraProvider.setExtra(key, values);
    }

    public static void setExtra(String key, String value) {
        extraProvider.setExtra(key, value);
    }

    public static void removeExtra(String key) {
        extraProvider.removeExtra(key);
    }

    public static void clearExtra() {
        extraProvider.clearExtra();
    }
    // endregion

    // region stop
    public static void stop() {

    }
    // endregion

    public interface OptionConfiguration {
        void onConfiguration(Configuration configuration);
    }

    private static class ExtraProvider {
        private final Map<String, Object> extras = new LinkedHashMap<>();

        void setExtra(String key, Map<String, String> values) {
            if (TextUtils.isEmpty(key) || null == values) {
                return;
            }

            extras.put(key, new LinkedHashMap<>(values));
        }

        void setExtra(String key, String value) {
            if (TextUtils.isEmpty(key)) {
                return;
            }

            if (null == value) {
                value = "null";
            }
            extras.put(key, value);
        }

        @SuppressWarnings("RedundantCollectionOperation")
        void removeExtra(String key) {
            if (extras.containsKey(key)) {
                extras.remove(key);
            }
        }

        void clearExtra() {
            extras.clear();
        }
    }
}
