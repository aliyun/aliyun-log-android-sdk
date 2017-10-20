package com.aliyun.sls.android.sdk;

import android.content.Context;

import com.aliyun.sls.android.sdk.callback.OSSCompletedCallback;
import com.aliyun.sls.android.sdk.common.Constants;
import com.aliyun.sls.android.sdk.common.OSSHeaders;
import com.aliyun.sls.android.sdk.common.OSSLog;
import com.aliyun.sls.android.sdk.common.auth.OSSCredentialProvider;
import com.aliyun.sls.android.sdk.common.auth.OSSFederationToken;
import com.aliyun.sls.android.sdk.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.aliyun.sls.android.sdk.common.auth.OSSStsTokenCredentialProvider;
import com.aliyun.sls.android.sdk.common.utils.DateUtil;
import com.aliyun.sls.android.sdk.common.utils.HttpHeaders;
import com.aliyun.sls.android.sdk.common.utils.OSSUtils;
import com.aliyun.sls.android.sdk.common.utils.VersionInfoUtils;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import static android.R.id.message;

/**
 * Created by zhuoqin on 10/10/17.
 */
public class RequestOperation {

    private volatile URI endpoint;
    private OkHttpClient innerClient;
    //    private Context applicationContext;
    private OSSCredentialProvider credentialProvider;
    private int maxRetryCount = Constants.DEFAULT_RETRY_COUNT;
    private ClientConfiguration conf;
    private static final int LIST_PART_MAX_RETURNS = 1000;
    private static final int MAX_PART_NUMBER = 10000;

    private static ExecutorService executorService = Executors.newFixedThreadPool(Constants.DEFAULT_BASE_THREAD_POOL_SIZE);

    public RequestOperation(final URI endpoint, OSSCredentialProvider credentialProvider, ClientConfiguration conf) {
        this.endpoint = endpoint;
        this.credentialProvider = credentialProvider;
        this.conf = conf;

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

    public OSSAsyncTask<PostLogResult> postLog(PostLogRequest postLogRequest, OSSCompletedCallback<PostLogRequest, PostLogResult> completedCallback) {

        LogGroup logGroup = postLogRequest.mLogGroup;
        String logStoreName = postLogRequest.mLogStoreName;
        String project = postLogRequest.mProject;

        RequestMessage requestMessage = new RequestMessage();

        String host = project + "." + endpoint.getHost();

        requestMessage.method = HttpMethod.POST;
        requestMessage.url = "http://" + host + "/logstores/" + logStoreName + "/shards/lb";


        //设置header信息
        Map<String, String> headers = requestMessage.headers;

        headers.put("x-log-apiversion", "0.6.0");
        headers.put("x-log-signaturemethod", "hmac-sha1");
        headers.put("Content-Type", "application/json");
        headers.put("Date", OSSUtils.GetMGTTime());
        headers.put("Host", host);

        try {
            byte[] httpPostBody = logGroup.LogGroupToJsonString().getBytes("UTF-8");
            byte[] httpPostBodyZipped = OSSUtils.GzipFrom(httpPostBody);
            requestMessage.setUploadData(httpPostBodyZipped);
            headers.put("Content-MD5", OSSUtils.ParseToMd5U32(httpPostBodyZipped));
            headers.put("Content-Length", String.valueOf(httpPostBodyZipped.length));
            headers.put("x-log-bodyrawsize", String.valueOf(httpPostBody.length));
            headers.put("x-log-compresstype", "deflate");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


        StringBuilder signStringBuf = new StringBuilder("POST" + "\n").
                append(headers.get("Content-MD5") + "\n").
                append(headers.get("Content-Type") + "\n").
                append(headers.get("Date") + "\n");

        OSSFederationToken federationToken = null;
        if (credentialProvider instanceof OSSStsTokenCredentialProvider) {
            federationToken = ((OSSStsTokenCredentialProvider) credentialProvider).getFederationToken();
            headers.put(OSSHeaders.OSS_SECURITY_TOKEN, federationToken.getSecurityToken());
        }

        String token = federationToken == null ? "" : federationToken.getSecurityToken();
        if (token != null && token != "") {
            headers.put("x-acs-security-token", token);
            signStringBuf.append("x-acs-security-token:" + token + "\n");
        }
        signStringBuf.append("x-log-apiversion:0.6.0\n").
                append("x-log-bodyrawsize:" + headers.get("x-log-bodyrawsize") + "\n").
                append("x-log-compresstype:deflate\n").
                append("x-log-signaturemethod:hmac-sha1\n").
                append("/logstores/" + logStoreName + "/shards/lb");
        String signString = signStringBuf.toString();


        String signature = "---initValue---";
        if (credentialProvider instanceof OSSStsTokenCredentialProvider) {
            signature = OSSUtils.sign(federationToken.getTempAK(), federationToken.getTempSK(), signString);
        } else if (credentialProvider instanceof OSSPlainTextAKSKCredentialProvider) {
            signature = OSSUtils.sign(((OSSPlainTextAKSKCredentialProvider) credentialProvider).getAccessKeyId(),
                    ((OSSPlainTextAKSKCredentialProvider) credentialProvider).getAccessKeySecret(), signString);
        }

        OSSLog.logDebug("signed content: " + signString + "   \n ---------   signature: " + signature, false);


        headers.put(OSSHeaders.AUTHORIZATION, signature);


        ResponseParser<PostLogResult> parser = new ResponseParsers.PostLogResponseParser();

        ExecutionContext<PostLogRequest> executionContext = new ExecutionContext<PostLogRequest>(getInnerClient(), postLogRequest);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }

        Callable<PostLogResult> callable = new OSSRequestTask<PostLogResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

}
