package com.aliyun.sls.android.core;

import java.util.ArrayList;
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
import com.aliyun.sls.android.core.sender.Sender.Callback;
import com.aliyun.sls.android.core.utdid.Utdid;
import com.aliyun.sls.android.core.utils.AppUtils;
import com.aliyun.sls.android.core.utils.DeviceUtils;
import com.aliyun.sls.android.core.utils.JsonUtil;
import com.aliyun.sls.android.core.utils.PrivacyUtils;
import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.ISpanProvider;
import com.aliyun.sls.android.ot.Resource;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.internal.HttpHeader;

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
    private final static AtomicBoolean hasPreInit = new AtomicBoolean(false);
    private final static AtomicBoolean hasInitialized = new AtomicBoolean(false);

    public static boolean preInit(
        final Context context,
        final Credentials credentials,
        final OptionConfiguration optionConfiguration) {
        return internalPreInit(context, credentials, optionConfiguration);
    }

    public static boolean initialize(
        final Context context,
        final Credentials credentials,
        final OptionConfiguration optionConfiguration
    ) {
        return internalInitialize(context, credentials, optionConfiguration);
    }

    private static boolean internalPreInit(
        final Context context,
        final Credentials credentials,
        final OptionConfiguration optionConfiguration) {
        PrivacyUtils.setEnablePrivacy(false);

        SLSLog.i(TAG, "start pre init SLS Android SDK.");
        if (null == optionConfiguration) {
            SLSLog.w(TAG, "OptionConfiguration must not be null.");
            return false;
        }

        if (hasPreInit.get()) {
            SLSLog.w(TAG, "SLSAndroid has been pre initialized.");
            return false;
        }

        final Context applicationContext = context.getApplicationContext();

        SLSAndroid.credentials = credentials;
        configuration = new Configuration(new SdkSender(applicationContext) {
            @Override
            protected void provideLogProducerConfig(LogProducerConfig config) {
                super.provideLogProducerConfig(config);
                config.setHttpHeaderInjector(
                    (srcHeaders, count) ->
                        HttpHeader.getHeadersWithUA(srcHeaders, String.format("apm/%s", BuildConfig.VERSION_NAME))
                );
            }
        });
        optionConfiguration.onConfiguration(configuration);

        initializeDefaultSpanProvider(applicationContext);
        if (configuration.enableCrashReporter || configuration.enableBlockDetection) {
            initializeSdkSender(applicationContext);
        }

        if (configuration.enableCrashReporter) {
            preInitFeature(applicationContext, credentials, configuration, "com.aliyun.sls.android.crashreporter.CrashReporterFeature");
        }
        if (configuration.enableBlockDetection) {
            preInitFeature(applicationContext, credentials, configuration, "com.aliyun.sls.android.blockdetection.BlockDetectionFeature");
        }
        if (configuration.enableNetworkDiagnosis) {
            preInitFeature(applicationContext, credentials, configuration, "com.aliyun.sls.android.network_diagnosis.NetworkDiagnosisFeature");
        }
        if (configuration.enableTracer) {
            preInitFeature(applicationContext, credentials, configuration, "com.aliyun.sls.android.trace.TraceFeature");
        }

        hasPreInit.set(true);

        Runtime.getRuntime().addShutdownHook(new Thread(SLSAndroid::stop, "SLS_ANDROID_SHUTDOWN"));
        SLSLog.i(TAG, "SLS Android pre initialize success.");
        return true;
    }

    private static boolean internalInitialize(
        final Context context,
        final Credentials credentials,
        final OptionConfiguration optionConfiguration) {
        internalPreInit(context, credentials, optionConfiguration);

        SLSLog.i(TAG, "start init SLS Android SDK.");
        if (null == optionConfiguration) {
            SLSLog.w(TAG, "OptionConfiguration must not be null.");
            return false;
        }

        PrivacyUtils.setEnablePrivacy(true);

        if (hasInitialized.get()) {
            SLSLog.w(TAG, "SLSAndroid has been initialized.");
            return false;
        }

        final Context applicationContext = context.getApplicationContext();
        initFeatures(applicationContext, credentials, configuration);
        hasInitialized.set(true);

        SLSLog.i(TAG, "SLS Android initialize success.");
        return true;
    }

    private static void initializeDefaultSpanProvider(final Context context) {
        final ISpanProvider userSpanProvider = configuration.spanProvider;
        configuration.spanProvider = new ISpanProvider() {
            @Override
            public Resource provideResource() {
                final boolean enablePrivacy = PrivacyUtils.isEnablePrivacy();
                Resource resource = Resource.of(
                    Pair.create("device.id", Utdid.getInstance().getUtdid(context)),
                    Pair.create("app.version", AppUtils.getAppVersion(context)),
                    Pair.create("app.versionCode", AppUtils.getAppVersionCode(context)),
                    Pair.create("app.name", AppUtils.getAppName(context)),
                    Pair.create("device.resolution", enablePrivacy ? DeviceUtils.getResolution(context) : ""),
                    Pair.create("net.access", enablePrivacy ? DeviceUtils.getAccessName(context) : ""),
                    Pair.create("net.access_subtype", enablePrivacy ? DeviceUtils.getAccessSubTypeName(context) : ""),
                    Pair.create("carrier", enablePrivacy ? DeviceUtils.getCarrier(context) : ""),
                    Pair.create("os.root", enablePrivacy ? DeviceUtils.isRoot() : "")
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
                        attributes.add(
                            Attribute.of("extras." + entry.getKey(), JsonUtil.fromMap((Map)entry.getValue())));
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

    private static void preInitFeature(
        final Context context,
        final Credentials credentials,
        final Configuration configuration,
        final String clazzName
    ) {
        SLSLog.i(TAG, "start pre init feature: " + clazzName);
        try {
            Feature feature = (Feature)Class.forName(clazzName).newInstance();
            if (null == feature) {
                return;
            }

            feature.preInit(context, credentials, configuration);
            features.add(feature);
            SLSLog.i(TAG, "pre init feature success, feature: " + clazzName);
        } catch (Throwable e) {
            e.printStackTrace();
            SLSLog.w(TAG, "pre init feature error. feature: " + clazzName + ", error: " + e.getMessage());
        }
    }

    private static void initFeatures(
        final Context context,
        final Credentials credentials,
        final Configuration configuration
    ) {
        SLSLog.i(TAG, "start init features");

        for (Feature feature : features) {
            feature.initialize(context, credentials, configuration);
            SLSLog.i(TAG, "init feature success, feature: " + feature.name());
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

    public static void registerCredentialsCallback(Callback callback) {
        if (null == callback) {
            return;
        }

        if (configuration.spanProcessor instanceof Sender) {
            ((Sender)configuration.spanProcessor).setCallback(callback);
        }

        for (Feature feature : features) {
            feature.setCallback(callback);
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

    public static void setUtdid(Context context, String utdid) {
        Utdid.getInstance().setUtdid(context, utdid);
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
