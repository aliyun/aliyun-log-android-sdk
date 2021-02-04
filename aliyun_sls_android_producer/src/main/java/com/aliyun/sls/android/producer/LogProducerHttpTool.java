package com.aliyun.sls.android.producer;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

public class LogProducerHttpTool {

    private static final String VERSION = "sls-android-sdk_v2.5.11";

    protected static AtomicLong localServerDeltaTime = new AtomicLong(0);

    static public int android_http_post(String urlString, String[] header, byte[] body) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("User-agent", VERSION);

            if (header != null) {
                int pairs = header.length / 2;
                for (int i = 0; i < pairs; i++) {
                    String key = header[2 * i];
                    String val = header[2 * i + 1];
                    httpConn.setRequestProperty(key, val);
                }
            }
            DataOutputStream out = new DataOutputStream(httpConn.getOutputStream());
            out.write(body);
            out.flush();
            out.close();
            String timeVal = httpConn.getHeaderField("x-log-time");
            if (timeVal != null && !"".equals(timeVal)) {
                long serverTime = Long.parseLong(timeVal);
                if (serverTime > 1500000000 && serverTime < 4294967294L) {
                    long sysTime = System.currentTimeMillis() / 1000;
                    long deltaTime = serverTime - sysTime;
                    if (deltaTime > 600 || deltaTime < -600) {
                        localServerDeltaTime.set(deltaTime);
                    }
                }
            }
            int responseCode = httpConn.getResponseCode();
            if (responseCode / 100 == 2) return responseCode;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            Log.w(VERSION, response.toString());
            return responseCode;
        } catch (Exception ex) {
            Log.w(VERSION, "exception", ex);
            return -1;
        }
    }
}
