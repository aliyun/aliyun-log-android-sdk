package com.aliyun.sls.android.webview.instrumentation;

import io.opentelemetry.api.GlobalOpenTelemetry;

/**
 * @author yulong.gyl
 * @date 2023/8/25
 */
public final class GlobalWebViewInstrumentation {
    private static final Object lock = new Object();

    private static WebViewInstrumentationConfiguration globalConfiguration;

    public static WebViewInstrumentationConfiguration getGlobalConfiguration() {
        WebViewInstrumentationConfiguration configuration = globalConfiguration;
        if (null == configuration) {
            synchronized (lock) {
                configuration = globalConfiguration;
                if (null == configuration) {
                    configuration = new WebViewInstrumentationConfiguration(GlobalOpenTelemetry.get());
                    setGlobalConfiguration(configuration);
                }
            }
        }
        return configuration;
    }

    public static void setGlobalConfiguration(WebViewInstrumentationConfiguration configuration) {
        globalConfiguration = configuration;
    }
}
