package com.aliyun.sls.android.network_diagnosis.trace;

import com.alibaba.netspeed.network.Diagnosis;

import com.aliyun.sls.android.exporter.otlp.OtlpSLSSpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

/**
 * @author yulong.gyl
 * @date 2023/11/30
 */
public class NetworkDiagnosisHelper {

    private static String sEndpoint;
    private static String sProject;
    private static String sLogstore;

    private NetworkDiagnosisHelper() {
        //no instance
    }

    public static void updateWorkspace(String endpoint, String project, String logstore) {
        sEndpoint = endpoint;
        sProject = project;
        sLogstore = logstore;
    }

    public static void setupTracer(SdkTracerProviderBuilder builder) {
        OtlpSLSSpanExporter exporter = Diagnosis.genExporter(
            sEndpoint,
            sProject,
            String.format("ipa-%s-raw", sLogstore),
            "", "", null);
        builder.addSpanProcessor(BatchSpanProcessor.builder(exporter).build());
    }
}
