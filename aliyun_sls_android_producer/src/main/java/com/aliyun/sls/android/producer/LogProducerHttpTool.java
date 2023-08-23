package com.aliyun.sls.android.producer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.VisibleForTesting;
import com.aliyun.sls.android.producer.utils.TimeUtils;

public class LogProducerHttpTool {
    private static final String TAG = "LogProducerHttpTool";

    @VisibleForTesting
    public static HttpURLConnection createConnection(String urlString, String method, String[] header)
        throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();

        if ("post".equalsIgnoreCase(method)) {
            httpConn.setDoOutput(true);
        }

        httpConn.setRequestMethod(method);

        if (header != null) {
            int pairs = header.length / 2;
            for (int i = 0; i < pairs; i++) {
                String key = header[2 * i];
                String val = header[2 * i + 1];
                httpConn.setRequestProperty(key, val);
            }
        }

        return httpConn;
    }

    @VisibleForTesting
    public static void writeToConnection(HttpURLConnection connection, String method, byte[] body) throws Exception {
        if ("post".equalsIgnoreCase(method)) {
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(body);
            out.flush();
            out.close();
        }
    }

    @VisibleForTesting
    public static void processResponseHeader(HttpURLConnection connection) {
        String timeVal = connection.getHeaderField("x-log-time");
        if (timeVal != null && !"".equals(timeVal)) {
            long serverTime = toLong(timeVal);
            if (serverTime > 1500000000 && serverTime < 4294967294L) {
                TimeUtils.getInstance().updateServerTime(serverTime);
            }
        }
    }

    @VisibleForTesting
    public static int processResponse(HttpURLConnection connection) throws Exception {
        int responseCode = connection.getResponseCode();
        if (responseCode / 100 == 2) {
            return responseCode;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // if 400 response code and x-log-requestid in response header, this request may be blocked.
        // we should return -1 that sdk will re-upload data to sls
        if (LogProducerHttpTool.shouldRetrySendData(connection)) {
            Log.w(TAG, "request may have been blocked. it will be retried. errorCode: " + response);
            return -1;
        }

        Log.w(TAG, "code: " + responseCode + ", response: " + response);
        return responseCode;
    }

    @VisibleForTesting
    public static boolean shouldRetrySendData(HttpURLConnection connection) throws Exception {
        final int responseCode = connection.getResponseCode();
        return 400 == responseCode && TextUtils.isEmpty(connection.getHeaderField("x-log-requestid"));
    }

    @VisibleForTesting
    public static long toLong(String time) {
        try {
            return Long.parseLong(time);
        } catch (Throwable t) {
            return 0L;
        }
    }

    public static int android_http_post(String urlString, String[] header, byte[] body) {
        return android_http_post(urlString, "POST", header, body);
    }

    public static int android_http_post(String urlString, String method, String[] header, byte[] body) {
        try {
            HttpURLConnection httpConn = createConnection(urlString, method, header);

            writeToConnection(httpConn, method, body);

            processResponseHeader(httpConn);

            return processResponse(httpConn);
        } catch (Exception ex) {
            Log.w(TAG, "exception: " + ex.getLocalizedMessage());
            return -1;
        }
    }
}
