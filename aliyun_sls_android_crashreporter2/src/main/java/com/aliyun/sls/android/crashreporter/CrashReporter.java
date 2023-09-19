package com.aliyun.sls.android.crashreporter;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import com.aliyun.sls.android.crashreporter.otel.CrashReporterOTel;
import com.aliyun.sls.android.crashreporter.parser.CrashFileHelper;
import com.aliyun.sls.android.otel.common.AttributesHelper;
import com.aliyun.sls.android.otel.common.utils.AppUtils;
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

    private CrashApi crashApi;

    private static final class Holder {
        private static final CrashReporter INSTANCE = new CrashReporter();
    }

    private CrashReporter() {
        //no instance
    }

    public static void init(Application application) {
        CrashReporter.init(application, false);
    }

    public static void init(Application application, boolean debuggable) {
        Holder.INSTANCE.initInternal(application, debuggable);
    }

    private void initInternal(Application application, boolean debuggable) {
        final Context context = application.getApplicationContext();

        CrashReporterOTel.getInstance().initOtel(context);

        String fileDirName = context.getFilesDir().getName();

        final Bundle args = new Bundle();
        args.putString("mAppId", "sls-inside");
        args.putBoolean("mDebug", debuggable && AppUtils.debuggable(context));
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

        //final String appId = options.instanceId;
        crashApi = CrashApi.createInstanceEx(context,
            "sls-inside",
            false,
            args,
            new InternalCrashClient(context, debuggable)
        );
        //crashApi.setCrashStatReporter((uuid, stat) -> {
        //    CrashFileHelper.scanAndReport(context, debuggable);
        //    return true;
        //});
        crashApi.setForeground(true);

        CrashReporterOTel.spanBuilder("app.start")
            .setAttribute("t", "pv")
            .setAllAttributes(AttributesHelper.create(context))
            .startSpan()
            .end();

        CrashFileHelper.scanAndReport(context, debuggable);
    }

    private static class InternalCrashClient implements ICrashClient {
        private final Context context;
        private final boolean debuggable;

        public InternalCrashClient(Context context, boolean debuggable) {
            this.context = context;
            this.debuggable = debuggable;
        }

        @Override
        public void onLogGenerated(File file, String s) {
            CrashFileHelper.parseCrashFile(this.context, file, s, debuggable);
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
