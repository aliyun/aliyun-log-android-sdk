package com.aliyun.sls.android.producer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;
import com.aliyun.sls.android.producer.utils.TimeUtils;

public class LogProducerHttpTool {
    private static final String TAG = "LogProducerHttpTool";

    public static int android_http_post(String urlString, String[] header, byte[] body) {
        return android_http_post(urlString, "POST", header, body);
    }

    public static int android_http_post(String urlString, String method, String[] header, byte[] body) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

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

            if ("post".equalsIgnoreCase(method)) {
                DataOutputStream out = new DataOutputStream(httpConn.getOutputStream());
                out.write(body);
                out.flush();
                out.close();
            }

            String timeVal = httpConn.getHeaderField("x-log-time");
            if (timeVal != null && !"".equals(timeVal)) {
                long serverTime = toLong(timeVal);
                if (serverTime > 1500000000 && serverTime < 4294967294L) {
                    TimeUtils.updateServerTime(serverTime);
                }
            }
            int responseCode = httpConn.getResponseCode();
            if (responseCode / 100 == 2) {
                return responseCode;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            Log.w(TAG, "code: " + responseCode + ", response: " + response.toString());
            return responseCode;
        } catch (Exception ex) {
            Log.w(TAG, "exception: " + ex.getLocalizedMessage());
            return -1;
        }
    }

    private static long toLong(String time) {
        try {
            return Long.parseLong(time);
        } catch (Throwable t) {
            return 0L;
        }

    }
}
