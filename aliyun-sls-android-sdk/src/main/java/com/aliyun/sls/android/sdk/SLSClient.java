package com.aliyun.sls.android.sdk;

import android.content.Context;

import com.aliyun.sls.android.sdk.callback.OSSCompletedCallback;
import com.aliyun.sls.android.sdk.common.auth.OSSCredentialProvider;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by wangzheng on 2017/10/12.
 */

public class SLSClient {

    private URI endpointURI;
    private OSSCredentialProvider credentialProvider;
    private RequestOperation requestOperation;
    private ClientConfiguration conf;


//    public SLSClient(Context context, String endpoint, OSSCredentialProvider credentialProvider, ClientConfiguration conf) {
//        try {
//            endpoint = endpoint.trim();
//            if (!endpoint.startsWith("http")) {
//                endpoint = "http://" + endpoint;
//            }
//            this.endpointURI = new URI(endpoint);
//        } catch (URISyntaxException e) {
//            throw new IllegalArgumentException("Endpoint must be a string like 'http://oss-cn-****.aliyuncs.com'," +
//                    "or your cname like 'http://image.cnamedomain.com'!");
//        }
//        if (credentialProvider == null) {
//            throw new IllegalArgumentException("CredentialProvider can't be null.");
//        }
//        this.credentialProvider = credentialProvider;
//        this.conf = (conf == null ? ClientConfiguration.getDefaultConf() : conf);
//
//        requestOperation = new RequestOperation(context.getApplicationContext(), endpointURI, credentialProvider, this.conf);
//    }
//
//
//    public OSSAsyncTask postLog(
//            LogGroup logGroup, String logStoreName, OSSCompletedCallback<PostLogRequest, PostLogResult> completedCallback) {
//        return requestOperation.postLog(logGroup, logStoreName, completedCallback);
//    }


}
