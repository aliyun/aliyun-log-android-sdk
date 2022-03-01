package com.aliyun.sls.android.producer.example;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.multidex.MultiDexApplication;
import com.aliyun.sls.android.SLSAdapter;
import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.plugin.crashreporter.SLSCrashReporterPlugin;
import com.aliyun.sls.android.plugin.trace.SLSTracePlugin;
import com.aliyun.sls.android.producer.example.utils.PreferenceUtils;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

/**
 * @author gordon
 * @date 2021/08/31
 */
public class SLSDemoApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        SLSGlobal.application = this;
        SLSGlobal.applicationContext = this.getApplicationContext();

        if (BuildConfig.CONFIG_ENABLE) {
            PreferenceUtils.overrideConfig(this);
        }

        // trace plugin
        SLSConfig config = new SLSConfig(this);
        config.endpoint = PreferenceUtils.getEndpoint(this);
        config.pluginLogproject = PreferenceUtils.getLogProject(this);
        config.pluginAppId = PreferenceUtils.getPluginAppId(this);
        config.debuggable = true;
        config.accessKeyId = PreferenceUtils.getAccessKeyId(this);
        config.accessKeySecret = PreferenceUtils.getAccessKeySecret(this);
        config.securityToken = PreferenceUtils.getAccessKeyToken(this);

        config.pluginTraceEndpoint = "https://cn-beijing.log.aliyuncs.com";
        config.pluginTraceLogProject = "qs-demos";
        config.pluginTraceLogStore = "sls-mall-traces";

        config.addCustom("custom_key", "custom_value");

        SLSAdapter adapter = SLSAdapter.getInstance();
        adapter.addPlugin(new SLSCrashReporterPlugin());
        adapter.addPlugin(SLSTracePlugin.getInstance());
        adapter.init(config);

//        setupTrace();
    }

    private void setupTrace() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            private String getActivityName(Activity a) {
                return a.getClass().getSimpleName();
            }

            private String getSpanName(Activity a, String method) {
                return getActivityName(a) + "." + method;
            }

            private void startSpan(Activity activity, String method) {
                Tracer tracer = SLSTracePlugin.getInstance().getSLSTelemetry().getTracer(getActivityName(activity));

                if (TextUtils.equals("Paused", method)) {
                    tracer.spanBuilder(getSpanName(activity, "Page_disappear")).startSpan()
                            .setAttribute("component", "Client_Page")
                            .setAttribute("page.name", getActivityName(activity))
                            .setAttribute("page.method", method)
                            .end();

                    final View v = activity.getWindow().getDecorView();
                    Span span = (Span) v.getTag(R.id.sls_trace_root_span_id);
                    span.end();
                    v.setTag(R.id.sls_trace_root_span_id, null);

                    Scope scope = (Scope) v.getTag(R.id.sls_trace_root_scope_id);
                    scope.close();
                    v.setTag(R.id.sls_trace_root_scope_id, null);
                    return;
                }

                if (TextUtils.equals("PreCreated", method) || TextUtils.equals("Resumed", method)) {
                    final View v = activity.getWindow().getDecorView();
                    if (null == v.getTag(R.id.sls_trace_root_span_id)) {
                        Span span = tracer.spanBuilder(getSpanName(activity, "Page_appear"))
                                .startSpan()
                                .setAttribute("component", "Client_Page")
                                .setAttribute("page.name", getActivityName(activity))
                                .setAttribute("page.method", method);

                        v.setTag(R.id.sls_trace_root_span_id, span);
                        v.setTag(R.id.sls_trace_root_scope_id, span.makeCurrent());
                    }
                    return;
                }
//
//
//                Span span = tracer.spanBuilder(getSpanName(activity, method)).startSpan()
//                        .setAttribute("component", "Client_Page")
//                        .setAttribute("page.name", getActivityName(activity))
//                        .setAttribute("page.method", method);
//
//                if (TextUtils.equals("PreCreated", method)) {
//                    final View v = activity.getWindow().getDecorView();
//                    v.setTag(R.id.sls_trace_root_span_id, span);
//                    v.setTag(R.id.sls_trace_root_scope_id, span.makeCurrent());
//                } else if (TextUtils.equals("Resumed", method)) {
//                    final View v = activity.getWindow().getDecorView();
//                    if (null == v.getTag(R.id.sls_trace_root_span_id)) {
//                        v.setTag(R.id.sls_trace_root_span_id, span);
//                        v.setTag(R.id.sls_trace_root_scope_id, span.makeCurrent());
//                    }
//                } else {
//                    span.end();
//                }
            }

            @Override
            public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                startSpan(activity, "PreCreated");
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
//                startSpan(activity, "Started");
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                startSpan(activity, "Resumed");
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                startSpan(activity, "Paused");
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
//                startSpan(activity, "Stopped");
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
//                startSpan(activity, "SaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
//                startSpan(activity, "Destroyed");
            }
        });
    }
}
