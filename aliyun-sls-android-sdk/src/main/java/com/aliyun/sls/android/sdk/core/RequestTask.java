package com.aliyun.sls.android.sdk.core;


import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.CommonHeaders;
import com.aliyun.sls.android.sdk.core.parser.ResponseParser;
import com.aliyun.sls.android.sdk.core.retry.RetryHandler;
import com.aliyun.sls.android.sdk.core.retry.RetryType;
import com.aliyun.sls.android.sdk.utils.DateUtil;
import com.aliyun.sls.android.sdk.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
public class RequestTask<T extends Result> implements Callable<T> {

    private ResponseParser<T> responseParser;

    private RequestMessage message;

    private ExecutionContext context;

    private OkHttpClient client;

    private RetryHandler retryHandler;

    private int currentRetryCount = 0;


    public RequestTask(RequestMessage message, ResponseParser parser, ExecutionContext context, int maxRetry) {
        this.responseParser = parser;
        this.message = message;
        this.context = context;
        this.client = context.getClient();
        this.retryHandler = new RetryHandler(maxRetry);
    }

    @Override
    public T call() throws Exception {

        Request request = null;
        Response response = null;
        LogException exception = null;
        Call call = null;

        try {
            SLSLog.logDebug("[call] - ");

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

            String contentType = message.getHeaders().get(CommonHeaders.CONTENT_TYPE);

            // set request body
            switch (message.getMethod()) {
                case POST:
                case PUT:
                    Utils.assertTrue(contentType != null, "Content type can't be null when upload!");
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
            printRsp.append("response code : " + response.code() + " for url : " + request.url() + "\n");

            byte[] body = response.body().bytes();
            if (body != null && body.length > 0) {
                String bodyString = new String(body, "utf-8");
                printRsp.append("response body : " + bodyString + "\n");
            }else{
                printRsp.append("response body is null \n");
            }
            for (String key : headerMap.keySet()) {
                printRsp.append("responseHeader [" + key + "]: ").append(headerMap.get(key).get(0) + "\n");
            }
            SLSLog.logDebug(printRsp.toString());
        } catch (Exception e) {
            SLSLog.logError("Encounter local execpiton: " + e.toString());
            if (SLSLog.isEnableLog()) {
                e.printStackTrace();
            }
            exception = new LogException("", e.getMessage(), e.getCause(), "");
        }

        //先检查request 是否 cancel
        // reconstruct exception caused by manually cancelling
        if ((call != null && call.isCanceled())
                || context.getCancellationHandler().isCancelled()) {
            exception = new LogException("", "Task is cancelled!", "");
            exception.canceled = true;
        }

        //只要有返回，就进行服务器时间和本地时间校对。
        if (response != null) {
            String responseDateString = response.header(CommonHeaders.DATE);
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
        if (exception == null) {
            int responseCode = response.code();
            String request_id = response.header(CommonHeaders.COMMON_HEADER_REQUEST_ID);

            if (TextUtils.isEmpty(request_id)) {
                request_id = "no request id";
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
                exception = new LogException("LogServerError", "Response code:"
                        + String.valueOf(responseCode) + "\nMessage: internal error", request_id);
                exception.responseCode = responseCode;
                byte[] body = response.body().bytes();
                if (body != null && body.length > 0) {
                    String bodyString = new String(body, "utf-8");
                    JSONObject obj = JSON.parseObject(bodyString);
                    if (obj != null && obj.containsKey("errorCode") && obj.containsKey("errorMessage")) {
                        exception = new LogException(obj.getString("errorCode"), obj.getString("errorMessage"), request_id);
                        exception.responseCode = responseCode;
                    } else {
                        exception = new LogException("LogServerError", "Response code:"
                                + String.valueOf(responseCode) + "\nMessage:"
                                + bodyString, request_id);
                        exception.responseCode = responseCode;
                    }
                }
            }
        }

        if (result == null) {
            //如果访问正常是不会执行到这里的，出错才会要求重试。
            RetryType retryType = retryHandler.shouldRetry(exception, currentRetryCount);
            SLSLog.logError("[run] - retry, retry type: " + retryType);
            if (retryType == RetryType.RetryTypeShouldRetry) {
                this.currentRetryCount++;
                return call();
            } else if (retryType == RetryType.RetryTypeShouldFixedTimeSkewedAndRetry) {
                // Updates the DATE header value and try again
                if (response != null) {
                    message.getHeaders().put(CommonHeaders.DATE, response.header(CommonHeaders.DATE));
                }
                this.currentRetryCount++;
                return call();
            } else {
                if (context.getCompletedCallback() != null) {
                    context.getCompletedCallback().onFailure(context.getRequest(), exception);
                }
                throw exception;
            }
        } else {
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
            if (source != null) {
                source.close();
            }
        }
    }
}
