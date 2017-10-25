package com.aliyun.sls.android.sdk.core;

import com.aliyun.sls.android.sdk.ClientConfiguration;
import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.ResponseParsers;
import com.aliyun.sls.android.sdk.CommonHeaders;
import com.aliyun.sls.android.sdk.Constants;
import com.aliyun.sls.android.sdk.core.auth.FederationToken;
import com.aliyun.sls.android.sdk.core.http.HttpMethod;
import com.aliyun.sls.android.sdk.model.LogGroup;
import com.aliyun.sls.android.sdk.core.parser.ResponseParser;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.core.auth.CredentialProvider;
import com.aliyun.sls.android.sdk.core.auth.PlainTextAKSKCredentialProvider;
import com.aliyun.sls.android.sdk.core.auth.StsTokenCredentialProvider;
import com.aliyun.sls.android.sdk.utils.HttpHeaders;
import com.aliyun.sls.android.sdk.utils.Utils;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;
import com.aliyun.sls.android.sdk.utils.VersionInfoUtils;

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
    private CredentialProvider credentialProvider;
    private int maxRetryCount = Constants.DEFAULT_RETRY_COUNT;

    private static ExecutorService executorService = Executors.newFixedThreadPool(Constants.DEFAULT_BASE_THREAD_POOL_SIZE);

    public RequestOperation(final URI endpoint, CredentialProvider credentialProvider, ClientConfiguration conf) {
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

    private void buildUrl(PostLogRequest postLogRequest, RequestMessage requestMessage) throws
            LogException {
        if (postLogRequest == null || requestMessage == null) {
            LogException exception = new LogException("", "postLogRequest or requestMessage when buildUrl is not null", null, "");
            throw exception;
        }
        String logStoreName = postLogRequest.mLogStoreName;
        String project = postLogRequest.mProject;
        String scheme = endpoint.getScheme();
        String host = project + "." + endpoint.getHost();
        String url = scheme + "://" + host + "/logstores/" + logStoreName + "/shards/lb";
        requestMessage.url = url;
        requestMessage.method = HttpMethod.POST;
    }

    private void buildHeaders(PostLogRequest postLogRequest, RequestMessage requestMessage) throws
            LogException {
        if (postLogRequest == null || requestMessage == null) {
            LogException exception = new LogException("", "postLogRequest or requestMessage when buildheaders is not null", null, "");
            throw exception;
        }
        LogGroup logGroup = postLogRequest.mLogGroup;
        String logStoreName = postLogRequest.mLogStoreName;
        String project = postLogRequest.mProject;
        String contentType = postLogRequest.logContentType;
        String host = project + "." + endpoint.getHost();

        Map<String, String> headers = requestMessage.headers;
        headers.put(CommonHeaders.COMMON_HEADER_APIVERSION, Constants.API_VERSION);
        headers.put(CommonHeaders.COMMON_HEADER_SIGNATURE_METHOD, Constants.SIGNATURE_METHOD);
        headers.put(CommonHeaders.COMMON_HEADER_COMPRESSTYPE, Constants.COMPRESSTYPE_DEFLATE);
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);
        headers.put(HttpHeaders.DATE, Utils.GetMGTTime());
        headers.put(HttpHeaders.HOST, host);


        try {
            byte[] httpPostBody = logGroup.LogGroupToJsonString().getBytes("UTF-8");
            byte[] httpPostBodyZipped = Utils.GzipFrom(httpPostBody);
            requestMessage.setUploadData(httpPostBodyZipped);
            headers.put(HttpHeaders.CONTENT_MD5, Utils.ParseToMd5U32(httpPostBodyZipped));
            headers.put(HttpHeaders.CONTENT_LENGTH, String.valueOf(httpPostBodyZipped.length));
            headers.put(CommonHeaders.COMMON_HEADER_BODYRAWSIZE, String.valueOf(httpPostBody.length));
        } catch (Exception e) {
            LogException exception = new LogException("", "postLogRequest or requestMessage is not null", null, "");
            throw exception;
        }

        StringBuilder signStringBuf = new StringBuilder("POST" + "\n").
                append(headers.get(HttpHeaders.CONTENT_MD5) + "\n").
                append(headers.get(HttpHeaders.CONTENT_TYPE) + "\n").
                append(headers.get(HttpHeaders.DATE) + "\n");

        FederationToken federationToken = null;
        if (credentialProvider instanceof StsTokenCredentialProvider) {
            federationToken = ((StsTokenCredentialProvider) credentialProvider).getFederationToken();
        }

        String token = federationToken == null ? "" : federationToken.getSecurityToken();
        if (token != null && token != "") {
            headers.put(CommonHeaders.COMMON_HEADER_SECURITY_TOKEN, token);
            signStringBuf.append(CommonHeaders.COMMON_HEADER_SECURITY_TOKEN + ":" + token + "\n");
        }
        signStringBuf.append(CommonHeaders.COMMON_HEADER_APIVERSION + ":" + Constants.API_VERSION + "\n").
                append(CommonHeaders.COMMON_HEADER_BODYRAWSIZE + ":" + headers.get(CommonHeaders.COMMON_HEADER_BODYRAWSIZE) + "\n").
                append(CommonHeaders.COMMON_HEADER_COMPRESSTYPE + ":" + Constants.COMPRESSTYPE_DEFLATE + "\n").
                append(CommonHeaders.COMMON_HEADER_SIGNATURE_METHOD + ":" + Constants.SIGNATURE_METHOD + "\n").
                append("/logstores/" + logStoreName + "/shards/lb");
        String signString = signStringBuf.toString();


        String signature = "---initValue---";
        if (credentialProvider instanceof StsTokenCredentialProvider) {
            signature = Utils.sign(federationToken.getTempAK(), federationToken.getTempSK(), signString);
        } else if (credentialProvider instanceof PlainTextAKSKCredentialProvider) {
            signature = Utils.sign(((PlainTextAKSKCredentialProvider) credentialProvider).getAccessKeyId(),
                    ((PlainTextAKSKCredentialProvider) credentialProvider).getAccessKeySecret(), signString);
        }

        SLSLog.logDebug("signed content: " + signString + "   \n ---------   signature: " + signature, false);


        headers.put(CommonHeaders.AUTHORIZATION, signature);

        headers.put(HttpHeaders.USER_AGENT, VersionInfoUtils.getUserAgent());

    }

    public AsyncTask<PostLogResult> postLog(PostLogRequest postLogRequest, CompletedCallback<PostLogRequest, PostLogResult> completedCallback) throws
            LogException {

        RequestMessage requestMessage = new RequestMessage();

        try {
            buildUrl(postLogRequest, requestMessage);
            buildHeaders(postLogRequest, requestMessage);
        } catch (LogException e) {
            throw e;
        }

        ResponseParser<PostLogResult> parser = new ResponseParsers.PostLogResponseParser();

        ExecutionContext<PostLogRequest> executionContext = new ExecutionContext<PostLogRequest>(getInnerClient(), postLogRequest);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }

        Callable<PostLogResult> callable = new RequestTask<PostLogResult>(requestMessage, parser, executionContext, maxRetryCount);

        return AsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

}
