package com.aliyun.sls.android.sdk;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.sls.android.sdk.common.OSSHeaders;
import com.aliyun.sls.android.sdk.common.OSSLog;
import com.aliyun.sls.android.sdk.common.utils.DateUtil;
import com.aliyun.sls.android.sdk.common.utils.OSSUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class OSSRequestTask<T extends OSSResult> implements Callable<T> {

    private ResponseParser<T> responseParser;

    private RequestMessage message;

    private ExecutionContext context;

    private OkHttpClient client;

    private OSSRetryHandler retryHandler;

    private int currentRetryCount = 0;


    public OSSRequestTask(RequestMessage message, ResponseParser parser, ExecutionContext context, int maxRetry) {
        this.responseParser = parser;
        this.message = message;
        this.context = context;
        this.client = context.getClient();
        this.retryHandler = new OSSRetryHandler(maxRetry);
    }

    @Override
    public T call() throws Exception {

        Request request = null;
        Response response = null;
        Exception exception = null;
        Call call = null;

        try {
            OSSLog.logDebug("[call] - ");

//            // validate request
//            OSSUtils.ensureRequestValid(context.getRequest(), message);
//            // signing
//            OSSUtils.signRequest(message);

            if (context.getCancellationHandler().isCancelled()) {
                throw new InterruptedIOException("This task is cancelled!");
            }

            Request.Builder requestBuilder = new Request.Builder();

            // build request url
            String url = message.url;
            requestBuilder = requestBuilder.url(url);

            // set request headers
            for (String key : message.getHeaders().keySet()) {
                requestBuilder = requestBuilder.addHeader(key, message.getHeaders().get(key));
            }

            String contentType = message.getHeaders().get(OSSHeaders.CONTENT_TYPE);

            // set request body
            switch (message.getMethod()) {
                case POST:
                case PUT:
                    OSSUtils.assertTrue(contentType != null, "Content type can't be null when upload!");
                    if (message.getUploadData() != null) {
                        requestBuilder = requestBuilder.method(message.getMethod().toString(),
                                new DataRequestBody(message.getUploadData(), contentType));
                    } else if (message.getUploadFilePath() != null) {
                        requestBuilder = requestBuilder.method(message.getMethod().toString(),
                                new DataRequestBody(new File(message.getUploadFilePath()), contentType));
                    } else if (message.getUploadInputStream() != null) {
                        requestBuilder = requestBuilder.method(message.getMethod().toString(),
                                new DataRequestBody(message.getUploadInputStream(),
                                        message.getReadStreamLength(), contentType));
                    } else {
                        requestBuilder = requestBuilder.method(message.getMethod().toString(), RequestBody.create(null, new byte[0]));
                    }
                    break;
                case GET:
                    requestBuilder = requestBuilder.get();
                    break;
                case HEAD:
                    requestBuilder = requestBuilder.head();
                    break;
                case DELETE:
                    requestBuilder = requestBuilder.delete();
                    break;
                default:
                    break;
            }

            request = requestBuilder.build();

            call = client.newCall(request);
            context.getCancellationHandler().setCall(call);

            // send request
            response = call.execute();

            // 输出响应信息日志
            Map<String, List<String>> headerMap = response.headers().toMultimap();
            StringBuilder printRsp = new StringBuilder();
            printRsp.append("response:---------------------\n");
            printRsp.append("response code: " + response.code() + " for url: " + request.url()+"\n");
            printRsp.append("response msg: "+ response.message()+"\n");
            for(String key : headerMap.keySet()){
                printRsp.append("responseHeader ["+key+"]: ").append(headerMap.get(key).get(0)+"\n");
            }
            OSSLog.logDebug(printRsp.toString());

        } catch (Exception e) {
            OSSLog.logError("Encounter local execpiton: " + e.toString());
            if (OSSLog.isEnableLog()) {
                e.printStackTrace();
            }
            exception = new ClientException(e.getMessage(), e);
        }

        //先检查request 是否 cancel
        // reconstruct exception caused by manually cancelling
        if ((call != null && call.isCanceled())
                || context.getCancellationHandler().isCancelled()) {
            exception = new ClientException("Task is cancelled!", exception.getCause(), true);
        }

        //只要有返回，就进行服务器时间和本地时间校对。
        if (response != null) {
            String responseDateString = response.header(OSSHeaders.DATE);
            try {
                // update the server time after every response
                long serverTime = DateUtil.parseRfc822Date(responseDateString).getTime();
                DateUtil.setCurrentServerTime(serverTime);
            } catch (Exception ignore) {
                // Fail to parse the time, ignore it
            }
        }

        //如果联网过程中没有出现异常
        T result = null;
        if (exception == null){
            int responseCode = response.code();
            String request_id = response.header("x-log-requestid");

            if (request_id == null) {
                request_id = "";
            }

            if (responseCode == 200) {
                exception = null;
                result = responseParser.parse(response);
                if (context.getCompletedCallback() != null) {
                    try {
                        context.getCompletedCallback().onSuccess(context.getRequest(), result);
                    } catch (Exception ignore) {
                        // The callback throws the exception, ignore it
                    }
                }
            } else {
                exception =  new LogException("LogServerError", "Response code:"
                        + String.valueOf(responseCode) + "\nMessage: internal error", request_id);
                byte[] body = response.body().bytes();
                if (body != null && body.length > 0){
                    String bodyString = new String(body,"utf-8");
                    JSONObject obj = JSON.parseObject(bodyString);
                    if (obj != null && obj.containsKey("errorCode") && obj.containsKey("errorMessage")) {
                        exception =  new LogException(obj.getString("errorCode"), obj.getString("errorMessage"), request_id);
                    }else{
                        exception =  new LogException("LogServerError", "Response code:"
                                + String.valueOf(responseCode) + "\nMessage:"
                                + bodyString, request_id);
                    }
                }
            }
        }

        if (result == null){
            //如果访问正常是不会执行到这里的，出错才会要求重试。
            OSSRetryType retryType = retryHandler.shouldRetry(exception, currentRetryCount);
            OSSLog.logError("[run] - retry, retry type: " + retryType);
            if (retryType == OSSRetryType.OSSRetryTypeShouldRetry) {
                this.currentRetryCount++;
                return call();
            } else if (retryType == OSSRetryType.OSSRetryTypeShouldFixedTimeSkewedAndRetry) {
                // Updates the DATE header value and try again
                if (response != null) {
                    message.getHeaders().put(OSSHeaders.DATE, response.header(OSSHeaders.DATE));
                }
                this.currentRetryCount++;
                return call();
            } else {
                if (exception instanceof ClientException) {
                    if (context.getCompletedCallback() != null) {
                        context.getCompletedCallback().onFailure(context.getRequest(), (ClientException) exception, null);
                    }
                } else {
                    if (context.getCompletedCallback() != null) {
                        context.getCompletedCallback().onFailure(context.getRequest(), null, (ServiceException) exception);
                    }
                }
                throw exception;
            }
        }else{
            return result;
        }
    }

    class DataRequestBody extends RequestBody {

        private static final int SEGMENT_SIZE = 2048; // okio.Segment.SIZE

        private byte[] data;
        private File file;
        private InputStream inputStream;
        private String contentType;
        private long contentLength;

        public DataRequestBody(File file, String contentType) {
            this.file = file;
            this.contentType = contentType;
            this.contentLength = file.length();
        }

        public DataRequestBody(byte[] data, String contentType) {
            this.data = data;
            this.contentType = contentType;
            this.contentLength = data.length;
        }

        public DataRequestBody(InputStream input, long contentLength, String contentType) {
            this.inputStream = input;
            this.contentType = contentType;
            this.contentLength = contentLength;
        }

        @Override
        public MediaType contentType() {
            return MediaType.parse(this.contentType);
        }

        @Override
        public long contentLength() throws IOException {
            return this.contentLength;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            Source source = null;
            if (this.file != null) {
                source = Okio.source(this.file);
            } else if (this.data != null) {
                source = Okio.source(new ByteArrayInputStream(this.data));
            } else if (this.inputStream != null) {
                source = Okio.source(this.inputStream);
            }
            long total = 0;
            long read, toRead, remain;

            while (total < contentLength) {
                remain = contentLength - total;
                toRead = Math.min(remain, SEGMENT_SIZE);

                read = source.read(sink.buffer(), toRead);
                if (read == -1) {
                    break;
                }

                total += read;
                sink.flush();
            }
            if(source != null){
                source.close();
            }
        }
    }
}
