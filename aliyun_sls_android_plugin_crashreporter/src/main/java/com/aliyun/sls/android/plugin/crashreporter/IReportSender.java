package com.aliyun.sls.android.plugin.crashreporter;

import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.scheme.Scheme;

/**
 * @author gordon
 * @date 2021/04/15
 */
public interface IReportSender {
    String LOGSTORE = "sls-alysls-track-base";

    void init(SLSConfig config);

    /**
     * send report data to remote server.
     */
    boolean send(Scheme data);

    /**
     * reset security token
     * @param accessKeyId accessKeyId
     * @param accessKeySecret accessKeySecret
     * @param securityToken securityToken
     */
    void resetSecurityToken(String accessKeyId, String accessKeySecret, String securityToken);

    /**
     * reset project configuration
     * @param endpoint endpoint of this project. should start with 'https://' prefix
     * @param project project name
     * @param logstore logstore name
     */
    void resetProject(String endpoint, String project, String logstore);
}
