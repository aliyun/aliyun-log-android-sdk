package com.aliyun.sls.android.sdk.utils;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by wangzheng on 2017/7/27.
 * 得到一些域名的ip地址
 * 背景：传媒公司有一些设备基于SDK，需要通过Android SDK进行数据采集，其中有一个需求是获得该设备外网IP，并且能在source字段中发送
 * 提供同步异步两个接口
 */

public class IPService {

    public final static String DEFAULT_URL = "https://api.ipify.org?format=text";

    public final static int HANDLER_MESSAGE_GETIP_CODE = 1530101;

    private static IPService instance = null;

    private String TAG = "IPService";

    private FutureTask<String> futureTask;

    public static IPService getInstance(){
        if (instance == null){
            instance = new IPService();
        }
        return instance;
    }

    private IPService(){

    }


    /*
        异步获取ip地址。handler回传
     */
    public void asyncGetIp(String url, final Handler handler){
        ExecutorService es = Executors.newSingleThreadExecutor();
        IP ip = new IP(url);
        futureTask = new FutureTask<String>(ip){
            @Override
            protected void done() {
                try {
                    String ip = futureTask.get();
                    if (handler != null){
                        Message message = Message.obtain(handler);
                        message.what = HANDLER_MESSAGE_GETIP_CODE;
                        message.obj = ip;
                        message.sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        es.execute(futureTask);
    }

    /*
        同步获取ip地址。注，最好不要在主线程操作
     */
    public String syncGetIp(String url) throws Exception{
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<String> future = es.submit(new IP(url));
        String ip = "";
        try{
            ip = future.get();
        }catch (Exception e){
            throw e;
        }
        return ip;
    }


    public class IP implements Callable<String> {
        private String uri = null;

        public IP(String uri){
            this.uri = uri;
        }

        @Override
        public String call() throws Exception {
            InputStream inputStream = null;

            HttpURLConnection urlConnection = null;

            String ipAddress = "";
            try {
                /* forming th java.net.URL object */
                URL url = new URL(uri);

                urlConnection = (HttpURLConnection) url.openConnection();
                /* for Get request */
                urlConnection.setRequestMethod("GET");
                //设置超时时间
                urlConnection.setConnectTimeout(15 * 1000);

                int statusCode = urlConnection.getResponseCode();

                /* 200 represents HTTP OK */
                if (statusCode ==  200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    ipAddress = convertInputStreamToString(inputStream);
                }else{
                    ipAddress = "";
                    Exception e = new Exception("statusCode : " + statusCode);
                    throw e;
                }
            } catch (Exception e) {
                ipAddress = "";
                throw e;
            }
            return ipAddress;
        }

    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));

        String line = "";
        String result = "";

        while((line = bufferedReader.readLine()) != null){
            result += line;
        }

            /* Close Stream */
        if(null!=inputStream){
            inputStream.close();
        }

        return result;
    }






}

