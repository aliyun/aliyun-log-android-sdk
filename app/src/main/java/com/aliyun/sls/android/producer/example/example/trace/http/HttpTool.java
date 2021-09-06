package com.aliyun.sls.android.producer.example.example.trace.http;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aliyun.sls.android.producer.HttpConfigProxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class HttpTool {

    private static final String TAG = "HttpTool";

    private HttpTool() {
        //no instance
    }

    public static Response get(String url) {
        return get(url, null);
    }

    public static Response get(String url, Map<String, String> headers) {
        return http(url, "GET", mapToStrings(headers), null);
    }

    public static Response post(String url) {
        return post(url, null);
    }

    public static Response post(String url, Map<String, String> headers) {
        return post(url, headers, null);
    }

    public static Response post(String url, Map<String, String> headers, String body) {
        if (null == headers) {
            headers = new HashMap<>();
        }
        headers.put("Content-Length", TextUtils.isEmpty(body) ? "0" : String.valueOf(body.length()));
        return http(url, "POST", mapToStrings(headers), (null != body ? body.getBytes(Charset.forName("UTF-8")) : null));
    }

    private static Response http(String urlString, String method, String[] header, byte[] body) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (TextUtils.equals("POST", method)) {
                connection.setDoOutput(true);
            }
            connection.setDoInput(true);
            connection.setRequestMethod(method);
            connection.setRequestProperty("User-agent", HttpConfigProxy.getUserAgent());

            if (header != null) {

                int pairs = header.length / 2;
                for (int i = 0; i < pairs; i++) {
                    String key = header[2 * i];
                    String val = header[2 * i + 1];
                    connection.setRequestProperty(key, val);
                }
            }

            if (null != body && connection.getDoOutput()) {
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.write(body);
                out.flush();
                out.close();
            }

            Response response = new Response();
            Map<String, List<String>> responseHeaderMap = connection.getHeaderFields();
            if (null != responseHeaderMap) {
                String[] responseHeaders = new String[responseHeaderMap.size() * 2];
                int index = 0;
                for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                    responseHeaders[index * 2] = entry.getKey();
                    responseHeaders[index * 2 + 1] = listToString(entry.getValue());
                    index += 1;
                }
                response.headers = responseHeaders;
            }

            int responseCode = connection.getResponseCode();
            response.code = responseCode;

            if (responseCode / 100 == 2) {
                response.data = streamToString(connection.getInputStream());
            } else {
                response.error = streamToString(connection.getErrorStream());
            }

            Log.w(TAG, "code: " + responseCode + ", response: " + response.toString());
            return response;
        } catch (Exception ex) {
            Log.w(TAG, "exception: " + ex.getLocalizedMessage());
            Response response = new Response();
            response.error = ex.getLocalizedMessage();
            response.code = 400;
            return response;
        }
    }

    private static String listToString(List<String> list) {
        if (null == list || list.size() == 0) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String s : list) {
            stringBuilder.append(s).append(";");
        }

        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    private static String[] mapToStrings(Map<String, String> maps) {
        if (null == maps || maps.size() == 0) {
            return null;
        }

        maps.put("Content-Type", "application/json; charset=UTF-8");

        String[] headers = new String[maps.size() * 2];
        int index = 0;
        for (Map.Entry<String, String> entry : maps.entrySet()) {
            headers[index * 2] = entry.getKey();
            headers[index * 2 + 1] = entry.getValue();
            index++;
        }
        return headers;
    }

    private static String streamToString(InputStream ins) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(ins));
        String line;
        StringBuilder data = new StringBuilder();
        while ((line = in.readLine()) != null) {
            data.append(line);
        }
        in.close();
        return data.toString();

    }

    public static class Response {
        public int code;
        public String data;
        public String error;
        public String[] headers;

        public boolean success() {
            return code / 100 == 2;
        }

        @NonNull
        @Override
        public String toString() {
            return "{" + "code: " + code + ", headers: " + headerToString() + ", data: " + data + ", error: " + error + "}";
        }

        private String headerToString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            for (int i = 0; i < headers.length / 2; i++) {
                builder.append(headers[i * 2]);
                builder.append(": ");
                builder.append(headers[i * 2 + 1]);
                builder.append(", ");
            }
            builder.append("]");

            if (builder.length() > 2) {
                builder.replace(builder.length() - 2, builder.length() - 2, "");
            }

            return builder.toString();
        }
    }
}
