package com.aliyun.sls.android.sdk.core;

import com.aliyun.sls.android.sdk.ClientConfiguration;
import com.aliyun.sls.android.sdk.ResponseParsers;
import com.aliyun.sls.android.sdk.CommonHeaders;
import com.aliyun.sls.android.sdk.Constants;
import com.aliyun.sls.android.sdk.core.http.HttpMethod;
import com.aliyun.sls.android.sdk.model.LogGroup;
import com.aliyun.sls.android.sdk.core.parser.ResponseParser;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.core.auth.OSSCredentialProvider;
import com.aliyun.sls.android.sdk.core.auth.OSSFederationToken;
import com.aliyun.sls.android.sdk.core.auth.OSSPlainTextAKSKCredentialProvider;
import com.aliyun.sls.android.sdk.core.auth.OSSStsTokenCredentialProvider;
import com.aliyun.sls.android.sdk.utils.HttpHeaders;
import com.aliyun.sls.android.sdk.utils.Utils;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * Created by zhuoqin on 10/10/17.
 */
public class RequestOperation {

    private volatile URI endpoint;
    private OkHttpClient innerClient;
    private OSSCredentialProvider credentialProvider;
    private int maxRetryCount = Constants.DEFAULT_RETRY_COUNT;

    private static ExecutorService executorService = Executors.newFixedThreadPool(Constants.DEFAULT_BASE_THREAD_POOL_SIZE);

    public RequestOperation(final URI endpoint, OSSCredentialProvider credentialProvider, ClientConfiguration conf) {
        this.endpoint = endpoint;
        this.credentialProvider = credentialProvider;

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .cache(null)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return HttpsURLConnection.getDefaultHostnameVerifier().verify(endpoint.getHost(), session);
                    }
                });

        if (conf != null) {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(conf.getMaxConcurrentRequest());

            builder.connectTimeout(conf.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(conf.getSocketTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(conf.getSocketTimeout(), TimeUnit.MILLISECONDS)
                    .dispatcher(dispatcher);

            if (conf.getProxyHost() != null && conf.getProxyPort() != 0) {
                builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(conf.getProxyHost(), conf.getProxyPort())));
            }

            this.maxRetryCount = conf.getMaxErrorRetry();
        }

        this.innerClient = builder.build();
    }

    public OkHttpClient getInnerClient() {
        return innerClient;
    }

    public AsyncTask<PostLogResult> postLog(PostLogRequest postLogRequest, CompletedCallback<PostLogRequest, PostLogResult> completedCallback) {

        LogGroup logGroup = postLogRequest.mLogGroup;
        String logStoreName = postLogRequest.mLogStoreName;
        String project = postLogRequest.mProject;
        String contentType = postLogRequest.logContentType;

        RequestMessage requestMessage = new RequestMessage();

        String host = project + "." + endpoint.getHost();

        requestMessage.method = HttpMethod.POST;
        requestMessage.url = "http://" + host + "/logstores/" + logStoreName + "/shards/lb";


        //设置header信息
        Map<String, String> headers = requestMessage.headers;

        headers.put(CommonHeaders.COMMON_HEADER_APIVERSION, Constants.API_VERSION);
        headers.put(CommonHeaders.COMMON_HEADER_SIGNATURE_METHOD, Constants.SIGNATURE_METHOD);
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);
        headers.put(HttpHeaders.DATE, Utils.GetMGTTime());
        headers.put(HttpHeaders.HOST, host);

        try {
            byte[] httpPostBody = logGroup.LogGroupToJsonString().getBytes("UTF-8");
            byte[] httpPostBodyZipped = Utils.GzipFrom(httpPostBody);
            requestMessage.setUploadData(httpPostBodyZipped);
            headers.put(HttpHeaders.CONTENT_MD5, Utils.ParseToMd5U32(httpPostBodyZipped));
            headers.put(HttpHeaders.CONTENT_LENGTH, String.valueOf(httpPostBodyZipped.length));
            headers.put("x-log-bodyrawsize", String.valueOf(httpPostBody.length));
            headers.put("x-log-compresstype", "deflate");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


        StringBuilder signStringBuf = new StringBuilder("POST" + "\n").
                append(headers.get(HttpHeaders.CONTENT_MD5) + "\n").
                append(headers.get(HttpHeaders.CONTENT_TYPE) + "\n").
                append(headers.get(HttpHeaders.DATE) + "\n");

        OSSFederationToken federationToken = null;
        if (credentialProvider instanceof OSSStsTokenCredentialProvider) {
            federationToken = ((OSSStsTokenCredentialProvider) credentialProvider).getFederationToken();
        }

        String token = federationToken == null ? "" : federationToken.getSecurityToken();
        if (token != null && token != "") {
            headers.put(CommonHeaders.COMMON_HEADER_SECURITY_TOKEN, token);
            signStringBuf.append(CommonHeaders.COMMON_HEADER_SECURITY_TOKEN + ":" + token + "\n");
        }
        signStringBuf.append("x-log-apiversion:0.6.0\n").
                append("x-log-bodyrawsize:" + headers.get("x-log-bodyrawsize") + "\n").
                append("x-log-compresstype:deflate\n").
                append("x-log-signaturemethod:hmac-sha1\n").
                append("/logstores/" + logStoreName + "/shards/lb");
        String signString = signStringBuf.toString();


        String signature = "---initValue---";
        if (credentialProvider instanceof OSSStsTokenCredentialProvider) {
            signature = Utils.sign(federationToken.getTempAK(), federationToken.getTempSK(), signString);
        } else if (credentialProvider instanceof OSSPlainTextAKSKCredentialProvider) {
            signature = Utils.sign(((OSSPlainTextAKSKCredentialProvider) credentialProvider).getAccessKeyId(),
                    ((OSSPlainTextAKSKCredentialProvider) credentialProvider).getAccessKeySecret(), signString);
        }

        SLSLog.logDebug("signed content: " + signString + "   \n ---------   signature: " + signature, false);


        headers.put(CommonHeaders.AUTHORIZATION, signature);


        ResponseParser<PostLogResult> parser = new ResponseParsers.PostLogResponseParser();

        ExecutionContext<PostLogRequest> executionContext = new ExecutionContext<PostLogRequest>(getInnerClient(), postLogRequest);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }

        Callable<PostLogResult> callable = new RequestTask<PostLogResult>(requestMessage, parser, executionContext, maxRetryCount);

        return AsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

}
