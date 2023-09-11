package com.aliyun.sls.android.crashreporter;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import com.aliyun.sls.android.crashreporter.otel.CrashReporterOTel;
import com.aliyun.sls.android.crashreporter.parser.CrashFileHelper;
import com.uc.crashsdk.export.CrashApi;
import com.uc.crashsdk.export.ICrashClient;

/**
 * @author yulong.gyl
 * @date 2023/9/7
 */
public final class CrashReporter {
    private static final String TAG = "CrashReporter";
    private static final String PATH_ROOT = "sls_rum" + File.separator + "crashreporter";
    public static final String PATH_ITRACE_LOGS = PATH_ROOT + File.separator + "itrace_logs";
    public static final String PATH_ITRACE_TAGS = PATH_ROOT + File.separator + "itrace_tags";

    private final Application application;

    private CrashApi crashApi;

    public CrashReporter(Application application) {
        this.application = application;
    }

    public void init(boolean debuggable) {
        CrashReporterOTel.getInstance().initOtel(application.getApplicationContext());

        String fileDirName = application.getFilesDir().getName();
        final Bundle args = new Bundle();
        // 路径配置
        args.putBoolean("mBackupLogs", false);
        args.putBoolean("mBackupLogs", debuggable);
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
        //args.putString("mBuildId", AppUtils.getAppVersion(application));

        //final String appId = options.instanceId;
        crashApi = CrashApi.createInstanceEx(application, "sls-internal", false, args,
            new InternalCrashClient(application));
        crashApi.setCrashStatReporter((uuid, stat) -> {
            //if (options.debuggable) {
            //    SLSLog.v(TAG, "report dau stat, stat: " + stat);
            //}

            CrashFileHelper.scanAndReport(application);

            CrashReporterOTel.spanBuilder("app.start")
                .setAttribute("t", "startup.pv")
                .startSpan()
                .end();

            return true;
        });
        crashApi.setForeground(true);
    }

    private static class InternalCrashClient implements ICrashClient {
        private Context context;

        public InternalCrashClient(Context context) {
            this.context = context;
        }

        @Override
        public void onLogGenerated(File file, String s) {
            CrashFileHelper.parseCrashFile(this.context, file, s);
        }

        @Override
        public void onClientProcessLogGenerated(String s, File file, String s1) {

        }

        @Override
        public File onBeforeUploadLog(File file) {
            return file;
        }

        @Override
        public void onCrashRestarting(boolean b) {

        }

        @Override
        public void onAddCrashStats(String s, int i, int i1) {

        }

        @Override
        public String onGetCallbackInfo(String s, boolean b) {
            return null;
        }
    }
}
