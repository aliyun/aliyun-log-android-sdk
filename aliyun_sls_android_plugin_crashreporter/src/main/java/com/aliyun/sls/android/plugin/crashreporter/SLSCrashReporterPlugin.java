package com.aliyun.sls.android.plugin.crashreporter;

import java.io.File;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.aliyun.sls.android.JsonUtil;
import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.SLSLog;
import com.aliyun.sls.android.plugin.AbstractPlugin;
import com.aliyun.sls.android.plugin.crashreporter.parser.UCSimpleITraceFileParser;
import com.aliyun.sls.android.plugin.crashreporter.sender.SLSReportSender;
import com.aliyun.sls.android.scheme.Scheme;
import com.uc.crashsdk.export.CrashApi;
import com.uc.crashsdk.export.ICrashClient;
import org.json.JSONObject;
import static com.aliyun.sls.android.plugin.crashreporter.BuildConfig.VERSION_NAME;

/**
 * @author gordon
 * @date 2021/04/14
 */
public class SLSCrashReporterPlugin extends AbstractPlugin implements ICrashClient {
    private static final String TAG = "SLSCrashReporterPlugin";
    private static final String PATH_ROOT = "sls_crash_reporter";
    private static final String PATH_ITRACE_LOGS = PATH_ROOT + File.separator + "itrace/logs";
    private static final String PATH_ITRACE_TAGS = PATH_ROOT + File.separator + "itrace/tags";

    private Context context;
    private SLSConfig config;

    private CrashApi crashApi;

    private String currentActivityName;
    private int startCount = 0;
    private boolean isForeground = false;

    private IReportSender reportSender;
    private ITraceFileParser traceFileParser;

    @Override
    public String name() {
        return "crash_reporter";
    }

    @Override
    public String version() {
        return VERSION_NAME;
    }

    @Override
    public void resetSecurityToken(String accessKeyId, String accessKeySecret, String securityToken) {
        super.resetSecurityToken(accessKeyId, accessKeySecret, securityToken);
        reportSender.resetSecurityToken(accessKeyId, accessKeySecret, securityToken);
    }

    @Override
    public void resetProject(String endpoint, String project, String logstore) {
        super.resetProject(endpoint, project, logstore);
        reportSender.resetProject(endpoint, project, logstore);
    }

    @Override
    public void updateConfig(SLSConfig config) {
        super.updateConfig(config);

        if (!TextUtils.isEmpty(config.channel)) {
            this.config.channel = config.channel;
        }
        if (!TextUtils.isEmpty(config.channelName)) {
            this.config.channelName = config.channelName;
        }
        if (!TextUtils.isEmpty(config.userNick)) {
            this.config.userNick = config.userNick;
        }
        if (!TextUtils.isEmpty(config.longLoginNick)) {
            this.config.longLoginNick = config.longLoginNick;
        }
        if (!TextUtils.isEmpty(config.userId)) {
            this.config.userId = config.userId;
        }
        if (!TextUtils.isEmpty(config.longLoginUserId)) {
            this.config.longLoginUserId = config.longLoginUserId;
        }
        if (!TextUtils.isEmpty(config.loginType)) {
            this.config.loginType = config.loginType;
        }

        traceFileParser.updateConfig(this.config);
    }

    @Override
    public synchronized void init(SLSConfig config) {
        this.config = config;
        this.context = config.context;

        this.reportSender = new SLSReportSender();
        reportSender.init(config);

        this.traceFileParser = new UCSimpleITraceFileParser(config, reportSender);

        //final File rootPath = new File(context.getFilesDir(), "sls_crash_reporter");
        String fileDirName = context.getFilesDir().getName();

        //final File crashLogFile = new File(rootPath, "logs");
        //SLSLog.e("DEBUGGG", "crashLogFile context: " + context);
        //SLSLog.e("DEBUGGG", "crashLogFile: " + crashLogFile.getAbsolutePath());
        //SLSLog.e("DEBUGGG", "crashLogFile exists: " + crashLogFile.exists());
        final Bundle args = new Bundle();
        // 路径配置
        args.putBoolean("mBackupLogs", config.debuggable);
        //args.putString("mLogsBackupPathName", new File(rootPath, "backup").getAbsolutePath());
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
        //args.putString("mJavaCrashLogFileName", "java_" + System.currentTimeMillis() + "_java.log");
        //args.putString("mNativeCrashLogFileName", "native_" + System.currentTimeMillis() + "_jni.log");
        //args.putString("mUnexpCrashLogFileName", "unexp_" + System.currentTimeMillis() + "_unexp.log");

        args.putBoolean("useApplicationContext", true);

        // 不上报, 通过 sls 上报
        args.putBoolean("mEnableStatReport", true);
        args.putBoolean("mSyncUploadSetupCrashLogs", false);
        args.putBoolean("mSyncUploadLogs", false);
        //args.putLong("mDisableSignals", (1 << 14) | (1 << 1));
        //args.putLong("mDisableBackgroundSignals", 1 << 14);
        args.putInt("uploadLogDelaySeconds", -1);
        args.putInt("mInfoSaveFrequency", 3);

        // 防止 uc 计算 crc
        args.putString("mBuildId", config.appVersion);

        final String appId = config.pluginAppId;

        crashApi = CrashApi.createInstanceEx(context, appId, false, args, this);
        crashApi.setCrashStatReporter((uuid, stat) -> {
            if (config.debuggable) {
                SLSLog.v(TAG, "report dau stat, stat: " + stat);
            }

            Scheme data = Scheme.createDefaultScheme(config);
            data.event_id = "61030";
            data.app_version = Scheme.returnDashIfNull(config.appVersion);
            data.app_name = Scheme.returnDashIfNull(config.appName);
            data.reserve6 = stat;

            JSONObject reserves = new JSONObject();
            JsonUtil.putOpt(reserves, "trace_uuid", uuid);
            JsonUtil.putOpt(reserves, "trace_app_id", appId);
            data.reserves = reserves.toString();

            final boolean ret = reportSender.send(data);
            if (config.debuggable) {
                SLSLog.v(TAG, "report dau stat result: " + ret);
            }

            final File crashLogFile = new File(context.getFilesDir(), PATH_ITRACE_LOGS);
            scanAndReport(crashLogFile);
            return true;
        });
        isForeground = true;
        crashApi.setForeground(true);

        initActivityLifecycleCallback();
    }

    private void scanAndReport(File logFolder) {
        if (config.debuggable) {
            SLSLog.v(TAG, "scanAndReport. logFolder: " + logFolder.getAbsolutePath());
        }

        final File[] files = logFolder.listFiles();
        if (null == files) {
            if (config.debuggable) {
                SLSLog.v(TAG, "scanAndReport. folder is empty.");
            }
            return;
        } else if (config.debuggable) {
            SLSLog.v(TAG, "scanAndReport. file count: " + files.length);
        }

        for (File file : files) {
            if (config.debuggable) {
                SLSLog.v(TAG, "scanAndReport. file: " + file.getName() + ", path: " + file.getAbsolutePath());
            }

            String type = "unknown";
            if (file.getName().endsWith("jni.log")) {
                type = "jni";
            } else if (file.getName().endsWith("anr.log")) {
                type = "anr";
            } else if (file.getName().endsWith("java.log")) {
                type = "java";
            } else if (file.getName().endsWith("unexp.log")) {
                type = "unexp";
            }

            traceFileParser.parseTraceFile(type, file);
        }
    }

    private void initActivityLifecycleCallback() {
        Application.ActivityLifecycleCallbacks callbacks = new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                currentActivityName = activity.getClass().getName();
                startCount++;

                if (!isForeground) {
                    isForeground = true;
                    crashApi.setForeground(true);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                startCount--;
                if (startCount <= 0 && isForeground) {
                    startCount = 0;
                    isForeground = false;
                    crashApi.setForeground(false);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

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

    @Override
    public void onLogGenerated(File file, String logType) {
        if (config.debuggable) {
            SLSLog.v(TAG, SLSLog.format("onLogGenerated, logType: %s, fileName: %s", logType, file.getAbsolutePath()));
        }
        traceFileParser.parseTraceFile(logType, file);
    }

    @Override
    public void onClientProcessLogGenerated(String processName, File file, String logType) {
        if (config.debuggable) {
            SLSLog.v(TAG,
                SLSLog
                    .format("onClientProcessLogGenerated, processName: %s, logType: %s, fileName: %s",
                        processName,
                        logType,
                        file.getAbsolutePath()));
        }
    }

    @Override
    public File onBeforeUploadLog(File file) {
        if (config.debuggable) {
            SLSLog.v(TAG, SLSLog.format("onBeforeUploadLog, fileName: %s", file.getAbsolutePath()));
        }
        return file;
    }

    @Override
    public void onCrashRestarting(boolean b) {
        if (config.debuggable) {
            SLSLog.v(TAG, SLSLog.format("onCrashRestarting, b: %b", b));
        }
    }

    @Override
    public void onAddCrashStats(String processName, int key, int count) {
        if (config.debuggable) {
            SLSLog.v(TAG,
                SLSLog.format("onLogGenerated, processName: %s, key: %d, count: %d", processName, key, count));
        }
    }

    @Override
    public String onGetCallbackInfo(String category, boolean isForClientProcess) {
        if (config.debuggable) {
            SLSLog.v(TAG,
                SLSLog.format("onGetCallbackInfo, category: %s, isForClientProcess: %b", category, isForClientProcess));
        }
        return null;
    }
}
