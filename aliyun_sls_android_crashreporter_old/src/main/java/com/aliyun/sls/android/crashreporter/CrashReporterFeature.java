package com.aliyun.sls.android.crashreporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.feature.SdkFeature;
import com.aliyun.sls.android.core.utdid.Utdid;
import com.aliyun.sls.android.core.utils.AppUtils;
import com.aliyun.sls.android.crashreporter.CrashReporter.LogLevel;
import com.aliyun.sls.android.crashreporter.utils.IOUtils;
import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.Span.StatusCode;
import com.aliyun.sls.android.ot.SpanBuilder;
import com.aliyun.sls.android.ot.context.ContextManager;
import com.aliyun.sls.android.trace.Tracer;
import com.uc.crashsdk.export.CrashApi;
import com.uc.crashsdk.export.CustomInfo;
import com.uc.crashsdk.export.CustomLogInfo;
import com.uc.crashsdk.export.ICrashClient;
import com.uc.crashsdk.export.LogType;

/**
 * @author gordon
 * @date 2022/7/19
 */
public class CrashReporterFeature extends SdkFeature {
    private static final String TAG = "CrashReporterFeature";

    private static final String PATH_ROOT = "sls_crash_reporter";
    private static final String PATH_ITRACE_LOGS = PATH_ROOT + File.separator + "itrace/logs";
    private static final String PATH_ITRACE_TAGS = PATH_ROOT + File.separator + "itrace/tags";

    private static final String LOG_SECTION_SEP = "--- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---";
    private static final String LINE_SEP = "\n";

    private Configuration configuration;
    private Credentials credentials;
    private CrashApi crashApi;
    private int startCount = 0;
    private boolean isForeground = false;

    private LastCachedSpan lastCachedSpan;

    public CrashReporterFeature() {
    }

    @Override
    public String name() {
        return "crash_reporter";
    }

    @Override
    public String version() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    protected void onPreInit(Context context, Credentials credentials, Configuration configuration) {
        this.configuration = configuration;
        this.credentials = credentials;

        this.initCrashApi(context, credentials, configuration);
        CrashReporter.setCrashReporterFeature(this);
    }

    @Override
    protected void onInitialize(Context context, Credentials credentials, Configuration configuration) {
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

    @Override
    public void setCredentials(Credentials credentials) {
        super.setCredentials(credentials);
        if (null == credentials || null == crashApi) {
            return;
        }

        if (TextUtils.isEmpty(credentials.instanceId)) {
            return;
        }

        crashApi.updateCustomInfo(createCrashBundle(configuration, credentials));
        crashApi.disableLog(0x10000000);
    }

    @Override
    public void setFeatureEnabled(boolean enable) {
        super.setFeatureEnabled(enable);
        if (!enable && null != crashApi) {
            crashApi.disableLog(LogType.JAVA);
            crashApi.disableLog(LogType.NATIVE);
            crashApi.disableLog(LogType.ANR);
            crashApi.disableLog(LogType.UNEXP);
            SLSLog.d(TAG, "CrashReporterFeature disabled.");
        }
    }

    public void reportCustomLog(final String type, final String log) {
        SpanBuilder builder = newSpanBuilder("crash");
        builder.addAttribute(
            Attribute.of(
                Pair.create("t", "custom"),
                Pair.create("ex.type", TextUtils.isEmpty(type) ? "log" : type),
                Pair.create("ex.origin", TextUtils.isEmpty(log) ? "" : log)
            )
        );

        builder.build().end();
    }

    public void reportError(final String type, final LogLevel logLevel, final  String message, final String stacktrace) {
        if (null == crashApi || TextUtils.isEmpty(type)) {
            return;
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("k_ct:exception").append(LINE_SEP);
        buffer.append("k_ac:").append(type).append(LINE_SEP);

        buffer.append(LOG_SECTION_SEP).append(LINE_SEP);
        buffer.append("Exception message: ").append(TextUtils.isEmpty(message) ? "" : message).append(LINE_SEP);
        buffer.append("Back traces starts.").append(LINE_SEP);
        buffer.append("level: ").append(logLevel.ordinal()).append(LINE_SEP);
        buffer.append("stack traceback:").append(LINE_SEP).append(TextUtils.isEmpty(stacktrace) ? "" : stacktrace).append(LINE_SEP);
        buffer.append("Back traces ends.");

        CustomLogInfo errorInfo = new CustomLogInfo(buffer, "exception");
        errorInfo.mUploadNow = true;

        crashApi.generateCustomLog(errorInfo);
    }

    private Bundle createCrashBundle(Configuration configuration, Credentials credentials) {
        final String appId = credentials.instanceId;
        //final File rootPath = new File(context.getFilesDir(), "sls_crash_reporter");
        String fileDirName = context.getFilesDir().getName();

        final Bundle args = new Bundle();
        args.putString("crver", "2.0");
        args.putString("mAppId", getAppIdByInstanceId(appId));
        args.putBoolean("mDebug", configuration.debuggable && AppUtils.debuggable(context));
        // 路径配置
        args.putBoolean("mBackupLogs", false);
        args.putString("mTagFilesFolderName", fileDirName + File.separator + PATH_ITRACE_TAGS);
        args.putString("mCrashLogsFolderName", fileDirName + File.separator + PATH_ITRACE_LOGS);

        // crash 类型配置
        // 开启 Java 监控
        args.putBoolean("enableJavaLog", true);
        // 开启 native 监控
        args.putBoolean("enableNativeLog", true);
        // 关闭 unexp 监控, itrace 还不稳定, 用户理解成本会有点高
        args.putBoolean("enableUnexpLog", false);
        // anr监控策略，所有 rom 生效
        args.putInt("mAnrTraceStrategy", 2);

        // crash 不再冒泡
        args.putBoolean("mCallJavaDefaultHandler", false);
        args.putBoolean("mCallNativeDefaultHandler", true);

        // 关闭加密
        args.putBoolean("mEncryptLog", false);
        // 关闭压缩
        args.putBoolean("mZipLog", false);
        args.putBoolean("useApplicationContext", true);

        // 不上报, 通过 sls 上报
        args.putBoolean("mEnableStatReport", true);
        args.putBoolean("mSyncUploadSetupCrashLogs", false);
        args.putBoolean("mSyncUploadLogs", false);
        args.putInt("uploadLogDelaySeconds", -1);
        args.putInt("mInfoSaveFrequency", 3);

        // 防止 uc 计算 crc
        args.putString("mBuildId", AppUtils.getAppVersion(context));

        // 错误分析文件数量限制设置
        args.putInt("mMaxCustomLogFilesCount", Integer.MAX_VALUE);
        args.putInt("mMaxCustomLogCountPerTypePerDay", Integer.MAX_VALUE);
        args.putInt("mMaxUploadCustomLogCountPerDay", Integer.MAX_VALUE);

        return args;
    }

    private void initCrashApi(Context context, Credentials credentials, Configuration configuration) {
        final String appId = credentials.instanceId;
        final Bundle args = createCrashBundle(configuration, credentials);

        crashApi = CrashApi.createInstanceEx(context, getAppIdByInstanceId(appId), false, args, new ICrashClient() {
            @Override
            public void onLogGenerated(File file, String logType) {
                SLSLog.v(TAG,
                    SLSLog.format("onLogGenerated, logType: %s, fileName: %s", logType, file.getAbsolutePath()));

                // onLogGenerated method called while the java & anr crash log file generated
                if (configuration.enableTracer) {
                    Span activeSpan = ContextManager.INSTANCE.activeSpan();
                    if (null == activeSpan) {
                        activeSpan = ContextManager.INSTANCE.getGlobalActiveSpan();
                    }

                    if (null != activeSpan) {
                        lastCachedSpan = new LastCachedSpan(activeSpan);
                    }
                }

                parseCrashFile(file, logType);
            }

            @Override
            public void onClientProcessLogGenerated(String processName, File file, String logType) {
                SLSLog.v(TAG,
                    SLSLog.format("onClientProcessLogGenerated, processName: %s, logType: %s, fileName: %s",
                        processName,
                        logType,
                        file.getAbsolutePath()
                    )
                );
            }

            @Override
            public File onBeforeUploadLog(File file) {
                SLSLog.v(TAG, SLSLog.format("onBeforeUploadLog, fileName: %s", file.getAbsolutePath()));
                return file;
            }

            @Override
            public void onCrashRestarting(boolean b) {
                SLSLog.v(TAG, SLSLog.format("onCrashRestarting, b: %b", b));
            }

            @Override
            public void onAddCrashStats(String processName, int key, int count) {
                SLSLog.v(TAG,
                    SLSLog.format("onLogGenerated, processName: %s, key: %d, count: %d", processName, key, count));
            }

            @Override
            public String onGetCallbackInfo(String category, boolean isForClientProcess) {
                SLSLog.v(TAG,
                    SLSLog.format("onGetCallbackInfo, category: %s, isForClientProcess: %b",
                        category,
                        isForClientProcess
                    )
                );
                return null;
            }
        });

        crashApi.disableLog(0x10000000);
        crashApi.setCrashStatReporter((uuid, stat) -> {
            reportState(uuid, stat);
            reportCrash();
            return true;
        });
        initActivityLifecycleCallback(context);
    }

    private String getAppIdByInstanceId(String instanceId) {
        return String.format("sls-%s", instanceId);
    }

    private void initActivityLifecycleCallback(Context context) {
        Application.ActivityLifecycleCallbacks callbacks = new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@SuppressWarnings("NullableProblems") Activity activity,
                Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@SuppressWarnings("NullableProblems") Activity activity) {
                startCount++;

                if (!isForeground) {
                    isForeground = true;
                    crashApi.setForeground(true);
                }
            }

            @Override
            public void onActivityResumed(@SuppressWarnings("NullableProblems") Activity activity) {

            }

            @Override
            public void onActivityPaused(@SuppressWarnings("NullableProblems") Activity activity) {

            }

            @Override
            public void onActivityStopped(@SuppressWarnings("NullableProblems") Activity activity) {
                startCount--;
                if (startCount <= 0 && isForeground) {
                    startCount = 0;
                    isForeground = false;
                    crashApi.setForeground(false);
                }
            }

            @Override
            public void onActivitySaveInstanceState(
                @SuppressWarnings("NullableProblems") Activity activity
                , @SuppressWarnings("NullableProblems") Bundle outState
            ) {

            }

            @Override
            public void onActivityDestroyed(@SuppressWarnings("NullableProblems") Activity activity) {

            }
        };

        Application application;
        if (context instanceof Application) {
            application = (Application)context;
        } else {
            application = (Application)context.getApplicationContext();
        }

        if (null != application) {
            application.registerActivityLifecycleCallbacks(callbacks);
        }
    }

    private void reportState(String uuid, String state) {
        SLSLog.v(TAG, "report dau stat. state: " + state);
        SpanBuilder spanBuilder = newSpanBuilder("state");

        spanBuilder.addAttribute(Attribute.of(
            Pair.create("t", "error"),
            Pair.create("ex.type", "state"),
            Pair.create("ex.origin", state),
            Pair.create("ex.uuid", uuid)
        ));

        final boolean ret = spanBuilder.build().end();
        if (ret) {
            SLSLog.v(TAG, "report dau stat success.");
        } else {
            SLSLog.w(TAG, "report dau stat fail.");
        }
    }

    private void reportCrash() {
        final File file = new File(context.getFilesDir(), PATH_ITRACE_LOGS);
        SLSLog.v(TAG, "crash log folder: " + file.getAbsolutePath());

        if (!file.exists()) {
            return;
        }

        File[] files = file.listFiles();
        if (null == files) {
            SLSLog.v(TAG, "crash log folder is empty.");
            return;
        }

        for (File f : files) {
            String type;
            if (f.getName().endsWith("jni.log")) {
                type = "jni";
            } else if (f.getName().endsWith("anr.log")) {
                type = "anr";
            } else if (f.getName().endsWith("java.log")) {
                type = "java";
            } else if (f.getName().endsWith("unexp.log")) {
                type = "unexp";
            } else {
                // unknown type should be ignored.
                // no impact on error analysis

                //noinspection ResultOfMethodCallIgnored
                f.delete();
                continue;
            }
            //else {
            //    //sls-androd-dev-f1a8_1.0_1.0_sdk-gphone64-arm64_12_166418712090940564_20220926181213_fg_custom.log
            //
            //}

            parseCrashFile(f, type);
        }
    }

    private void reportCrash(final String time, final String type, final File file, final String content) {
        SLSLog.v(TAG,
            "start report crash. type: " + type
                + ", time: " + time
                + ", content.length: " + content.length()
        );

        // if last cached span is null, should read active span from cache
        if (configuration.enableTracer && null == lastCachedSpan) {
            Span span = ContextManager.INSTANCE.getLastGlobalActiveSpan();
            if (null != span) {
                lastCachedSpan = new LastCachedSpan(span);
            }
        }

        SpanBuilder builder = newSpanBuilder("crash");
        builder.addAttribute(
            Attribute.of(
                Pair.create("t", "error"),
                Pair.create("ex.type", "crash"),
                Pair.create("ex.sub_type", type),
                Pair.create("ex.origin", content),
                Pair.create("ex.file", file.getName())
            )
        );

        if (configuration.enableTracer && null != lastCachedSpan) {
            builder.setParent(lastCachedSpan);
        }

        final Span crashSpan = builder.build();

        final boolean ret = crashSpan.end();
        if (ret) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            SLSLog.v(TAG, "report crash success.");
        } else {
            SLSLog.w(TAG, "report crash fail.");
        }

        // if tracer enabled, report crash to tracer
        if (configuration.enableTracer) {
            // error monitor not supported
            if (TextUtils.equals(type, "java") || TextUtils.equals(type, "jni")
                || TextUtils.equals(type, "anr") || TextUtils.equals(type, "unexp")) {
                final String classify = TextUtils.equals(type, "anr") ? "anr" : "crash";
                final String filterType = TextUtils.equals(type, "jni") ? "native" : (TextUtils.equals(type, "java") ? "java" : "");
                SLSLog.v(TAG, "report crash to tracer. type: " + type + ", time: " +time + ", classify: " + classify + ", filter type: " + filterType);

                Span span = Tracer.startSpan(TextUtils.equals(classify, "anr") ? "Application Not Responding" : ("Application Crashed - " + filterType))
                    .setSpanId(crashSpan.getSpanId())
                    .addAttribute(
                        Attribute.of("ex.file", file.getName()),
                        Attribute.of("ex.uuid", Utdid.getInstance().getUtdid(context)),
                        Attribute.of("ex.project", credentials.project),
                        Attribute.of("ex.time", time),
                        Attribute.of("ex.filter_time", time.substring(0, 10)),
                        Attribute.of("ex.filter_classify", classify),
                        Attribute.of("ex.filter_type", filterType)
                    )
                    .setStatus(StatusCode.ERROR);

                if (null != lastCachedSpan) {
                    span.setParent(lastCachedSpan);
                } else {
                    span.setTraceId(crashSpan.getTraceId());
                }

                span.end();
            }
        }
    }

    private void parseCrashFile(final File file, final String type) {
        if (TextUtils.isEmpty(type)) {
            SLSLog.w(TAG, "type is empty.");
            return;
        }

        if (null == file || !file.exists()) {
            SLSLog.w(TAG, "crash file is not exists. type: " + type);
            return;
        }
        BufferedReader bufferedReader = null;
        StringBuilder buffer = new StringBuilder();
        String time = null;
        try {
            final FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line;

            while (null != (line = bufferedReader.readLine())) {
                if (TextUtils.isEmpty(line)) {
                    continue;
                }
                if (line.startsWith("Basic Information") && line.contains("time:")) {
                    time = obtainTime(line);
                }

                // 替换日志文件里面的UUID
                if (line.startsWith("UUID:")) {
                    line = "UUID: " + Utdid.getInstance().getUtdid(context);
                }
                buffer.append(line);
                buffer.append("\n");
            }

        } catch (Throwable t) {
            SLSLog.e(TAG,
                "parse crash file error. type: " + type
                    + ", file: " + file.getAbsolutePath()
                    + ", error: " + t.getMessage());
            return;
        } finally {
            IOUtils.close(bufferedReader);
        }

        if (TextUtils.isEmpty(time)) {
            time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        }
        reportCrash(time, type, file, buffer.toString());
    }

    private String obtainTime(String info) {
        try {
            String time = info.split("time:")[1].trim();
            return time.substring(0, time.length() - 1);
        } catch (Throwable t) {
            return null;
        }
    }

    private static class LastCachedSpan extends Span {
        LastCachedSpan(Span span) {
            super();

            setTraceId(span.getTraceId());
            if (!TextUtils.isEmpty(span.getParentSpanId())) {
                setSpanId(span.getParentSpanId());
            } else {
                setSpanId(span.getSpanId());
            }
        }
    }
}
