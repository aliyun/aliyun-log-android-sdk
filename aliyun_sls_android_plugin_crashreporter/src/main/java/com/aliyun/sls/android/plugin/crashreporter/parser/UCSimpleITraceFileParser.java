package com.aliyun.sls.android.plugin.crashreporter.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.text.TextUtils;
import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.SLSLog;
import com.aliyun.sls.android.plugin.crashreporter.IReportSender;
import com.aliyun.sls.android.plugin.crashreporter.ITraceFileParser;
import com.aliyun.sls.android.plugin.trace.SLSTracePlugin;
import com.aliyun.sls.android.scheme.Scheme;
import com.aliyun.sls.android.utdid.Utdid;

import org.json.JSONException;
import org.json.JSONObject;

import static com.aliyun.sls.android.SLSLog.d;
import static com.aliyun.sls.android.SLSLog.e;
import static com.aliyun.sls.android.SLSLog.format;
import static com.aliyun.sls.android.SLSLog.v;
import static com.aliyun.sls.android.SLSLog.w;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

/**
 * @author gordon
 * @date 2021/05/08
 */
public class UCSimpleITraceFileParser implements ITraceFileParser {
    private static final String TAG = "UCITraceFileParser";

    private SLSConfig config;
    private IReportSender reportSender;

    public UCSimpleITraceFileParser(SLSConfig config, IReportSender reportSender) {
        this.config = config;
        this.reportSender = reportSender;
    }

    @Override
    public void updateConfig(SLSConfig config) {
        this.config = config;
    }

    @Override
    public void parseTraceFile(String type, File file) {
        if (TextUtils.isEmpty(type)) {
            w(TAG, format("parseTraceFile, type is empty. type: %s", type));
            return;
        }

        if (null == file || !file.exists()) {
            w(TAG, format("parseTraceFile, file is not exists or null. type: %s, file: %s"
                , type
                , (null != file ? file.getAbsolutePath() : "null"), type));
            return;
        }

        BufferedReader bufferedReader = null;
        try {
            final FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);

            String line;
            StringBuilder buffer = new StringBuilder();
            String time = null;
            while (null != (line = bufferedReader.readLine())) {
                if (TextUtils.isEmpty(line)) {
                    continue;
                }

                if (line.startsWith("Basic Information") && line.contains("time:")) {
                    time = obtainTime(line);
                }

                buffer.append(line);
                buffer.append("\n");
            }

            onLogParsedEnd(time, type, file, buffer.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // @formatter:off
            e(TAG, format("parseTraceFile. file not found. file: %s, type: %s"
                , file.getAbsolutePath()
                , type));
        } catch (IOException e) {
            e.printStackTrace();
            // @formatter:off
            e(TAG, format("parseTraceFile. exception. file: %s, type: %s, error: %s"
                , file.getAbsolutePath()
                , type
                , e.getMessage()));
        } catch (Throwable t) {
            t.printStackTrace();
            // @formatter:off
            e(TAG, format("parseTraceFile. exception. file: %s, type: %s, error: %s"
                , file.getAbsolutePath()
                , type
                , t.getMessage()));
        } finally {
            Utils.close(bufferedReader);
        }
    }

    private String[] reportTraceIfNeed(File file, String logType) {
        // pre-check trace plugin exist
        boolean enable;
        try {
            enable = null != Class.forName("com.aliyun.sls.android.plugin.trace.SLSTracePlugin");
        } catch (Throwable t) {
            enable = false;
        }
        if (!enable) {
            return new String[]{null, null};
        }

        final Context context = config.context;
        final Tracer tracer = SLSTracePlugin.getInstance().getTelemetrySdk().getTracer("crash_reporter");
        Span span = tracer.spanBuilder("crash_reporter")
                .setParent(io.opentelemetry.context.Context.current())
                .startSpan()
                .setAttribute("exception.file", file.getName())
                .setAttribute("exception.type", logType)
                .setAttribute("exception.project", config.pluginLogproject)
                .setAttribute("exception.logstore", "sls-alysls-track-android")
                .setAttribute("exception.appid", config.pluginAppId)
                .setAttribute("exception.utdid", Utdid.getInstance().getUtdid(context));
        final String uuid = Utils.getUUID(context);
        if (!TextUtils.isEmpty(uuid)) {
            span.setAttribute("exception.uuid", uuid);
        }
//        if (null != crashApi.getUncaughtException()) {
//            span.setAttribute("exception.type", crashApi.getUncaughtException().getClass().getCanonicalName());
//            span.setAttribute("exception.message", crashApi.getUncaughtException().getMessage());
//            span.recordException(crashApi.getUncaughtException());
//        }
        span.setStatus(StatusCode.ERROR);
        span.end();

        return new String[]{span.getSpanContext().getTraceId(), span.getSpanContext().getSpanId()};
    }

    private void onLogParsedEnd(final String time, final String type, final File file, final String content) {
        if (config.debuggable) {
            v(TAG, SLSLog.format("onLogParsedEnd. type: %s, content length: %d", type, content.length()));
        }

        final String[] trace = reportTraceIfNeed(file, type);

        Scheme data = Scheme.createDefaultScheme(config);
        data.app_version = Scheme.returnDashIfNull(config.appVersion);
        data.app_name = Scheme.returnDashIfNull(config.appName);
        data.traceId = TextUtils.isEmpty(trace[0]) ? "-" : trace[0];
        data.spanId = TextUtils.isEmpty(trace[1]) ? "-" : trace[1];

        if (!TextUtils.isEmpty(time)) {
            data.local_timestamp = parseTime(time);
            data.local_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault()).format(new Date(toLongTime(data.local_timestamp)));
        }

        if ("java".equals(type)) {
            data.event_id = "61001";
        } else if ("jni".equals(type)) {
            data.event_id = "61002";
        } else if ("anr".equals(type)) {
            data.event_id = "61003";
        } else if ("unexp".equals(type)) {
            data.event_id = "61004";
        }

        data.reserve6 = content;

        JSONObject reserves = new JSONObject();
        try {
            reserves.putOpt("trace_file_name", file.getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        data.reserves = reserves.toString();

        if (config.debuggable) {
            v(TAG, SLSLog.format("onLogParsedEnd. ready to sent."));
        }

        final boolean res = reportSender.send(data);
        if (!res) {
            e(TAG, "report crash error.");
        } else {
            d(TAG, "report crash success");
            final boolean ret = file.delete();
            if (config.debuggable) {
                v(TAG, "report crash. delete file ret: " + ret + ", file: " + file.getAbsolutePath());
            }
        }
    }

    private String obtainTime(String info) {
        try {
            String time = info.split("time:")[1].trim();
            return time.substring(0, time.length() -1);
        }  catch (Throwable t) {
            return null;
        }
    }

    private long toLongTime(String time) {
        try {
            return Long.parseLong(time);
        } catch (Throwable t) {
            return System.currentTimeMillis();
        }
    }

    private String parseTime(String time) {
        try {
            return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).parse(time).getTime() + "";
        } catch (Throwable e) {
            return String.valueOf(System.currentTimeMillis());
        }
    }
}
