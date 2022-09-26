package com.aliyun.sls.android.core.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author gordon
 * @date 2022/7/19
 */
public class Credentials {
    public String instanceId;
    public String endpoint;
    public String project;

    public String accessKeyId;
    public String accessKeySecret;
    public String securityToken;

    public NetworkDiagnosisCredentials networkDiagnosisCredentials;
    public Credentials crashReporterCredentials;
    public TracerCredentials tracerCredentials;

    public Credentials() {
    }

    public NetworkDiagnosisCredentials getNetworkDiagnosisCredentials() {
        if (null == networkDiagnosisCredentials) {
            networkDiagnosisCredentials = new NetworkDiagnosisCredentials(this);
        }
        return networkDiagnosisCredentials;
    }

    public TracerCredentials createTraceCredentials() {
        if (null == tracerCredentials) {
            tracerCredentials = new TracerCredentials(this);
        }
        return tracerCredentials;
    }

    public static class LogstoreCredentials extends Credentials {
        public String logstore;

        public LogstoreCredentials(Credentials credentials) {
            this.instanceId = credentials.instanceId;
            this.endpoint = credentials.endpoint;
            this.project = credentials.project;
            this.accessKeyId = credentials.accessKeyId;
            this.accessKeySecret = credentials.accessKeySecret;
            this.securityToken = credentials.securityToken;
        }
    }

    public static class NetworkDiagnosisCredentials extends LogstoreCredentials {
        public String secretKey;
        public String siteId = null;
        public final Map<String, String> extension = new LinkedHashMap<>();

        private NetworkDiagnosisCredentials(Credentials credentials) {
            super(credentials);
        }
    }

    public static class TracerCredentials extends LogstoreCredentials {
        public TracerLogCredentials logCredentials;

        public TracerCredentials(Credentials credentials) {
            super(credentials);
        }

        public TracerLogCredentials createLogCredentials() {
            if (null == logCredentials) {
                logCredentials = new TracerLogCredentials(this);
            }
            return logCredentials;
        }

        public static class TracerLogCredentials extends LogstoreCredentials {

            public TracerLogCredentials(Credentials credentials) {
                super(credentials);
            }
        }
    }

}
