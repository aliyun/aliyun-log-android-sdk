package com.aliyun.sls.android.producer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogProducerHttpTool {

    static public int android_http_post(String urlString, String[] header, byte[] body) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("POST");

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

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpConn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //打印结果
            System.out.println(response.toString());

            return httpConn.getResponseCode();
        } catch (Exception ex) {
            ex.printStackTrace();
            return 400;
        }
    }
}
