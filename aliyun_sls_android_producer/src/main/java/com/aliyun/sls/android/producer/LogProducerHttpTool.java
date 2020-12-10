package com.aliyun.sls.android.producer;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogProducerHttpTool {

    private static final String VERSION = "sls-android-sdk_v2.5.4";

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
            int responseCode = httpConn.getResponseCode();
            InputStream inputStream;
            if(responseCode == 200){
                inputStream = httpConn.getInputStream();
            }else {
                inputStream = httpConn.getErrorStream();
            }
            InputStreamReader r = new InputStreamReader(inputStream);
            BufferedReader in = new BufferedReader(r);
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            if(responseCode != 200){
                Log.e(VERSION, response.toString());
            }
            return responseCode;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 400;
        }
    }
}
