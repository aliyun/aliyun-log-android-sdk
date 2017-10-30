package com.aliyun.sls;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.aliyun.sls.android.sdk.ClientConfiguration;
import com.aliyun.sls.android.sdk.LOGClient;
import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.core.auth.StsTokenCredentialProvider;
import com.aliyun.sls.android.sdk.model.Log;
import com.aliyun.sls.android.sdk.model.LogGroup;
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.utils.IPService;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;


public class MainActivity extends AppCompatActivity {

    public final static int HANDLER_MESSAGE_UPLOAD_FAILED = 00011;
    public final static int HANDLER_MESSAGE_UPLOAD_SUCCESS = 00012;

    /**
     * 填入必要的参数
     */
    public String endpoint = "******";
    public String accesskeyID = "******";
    public String accessKeySecret = "******";
    public String project = "******";
    public String logStore = "******";
    public String source_ip = "";


    TextView logText;
    Button upload;

    private Handler handler = new Handler() {
        // 处理子线程给我们发送的消息。
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case IPService.HANDLER_MESSAGE_GETIP_CODE:
                    source_ip = (String) msg.obj;
                    logText.setText(source_ip);
                    return;
                case HANDLER_MESSAGE_UPLOAD_FAILED:
                    logText.setText((String) msg.obj);
                    return;
                case HANDLER_MESSAGE_UPLOAD_SUCCESS:
                    Toast.makeText(MainActivity.this,"upload success",Toast.LENGTH_SHORT).show();
                    return;
            }
            super.handleMessage(msg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logText = (TextView) findViewById(R.id.ip);
        upload = (Button) findViewById(R.id.upload);
        try {
            IPService.getInstance().asyncGetIp(IPService.DEFAULT_URL,handler);
        } catch (Exception e) {
            e.printStackTrace();
            logText.setText(e.getMessage());
        }
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asyncUploadLog(source_ip);
            }
        });

    }

    /*
     *  推荐使用的方式，直接调用异步接口，通过callback 获取回调信息
     */
    private void asyncUploadLog(@Nullable String ip) {
        if (TextUtils.isEmpty(ip)){
            Toast.makeText(MainActivity.this,"请先获取ip地址",Toast.LENGTH_SHORT).show();
            return;
        }

        //移动端是不安全环境，不建议把ak，sk保存在本地。建议使用STS方式。具体参见
        //https://help.aliyun.com/document_detail/60899.html
        String STS_AK = "******";
        String STS_SK = "******";
        String STS_TOKEN = "******";
        StsTokenCredentialProvider credentialProvider =
                new StsTokenCredentialProvider(STS_AK,STS_SK,STS_TOKEN);

        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        SLSLog.enableLog(); // log打印在控制台
        LOGClient logClient = new LOGClient(endpoint, credentialProvider, conf);
        /* 创建logGroup */
        LogGroup logGroup = new LogGroup("sls test", ip);

        /* 存入一条log */
        Log log = new Log();
        log.PutContent("current time ", "" + System.currentTimeMillis() / 1000);
        log.PutContent("content", "this is a log");

        logGroup.PutLog(log);

        try{
            PostLogRequest request = new PostLogRequest(project,logStore,logGroup);
            logClient.asyncPostLog(request, new CompletedCallback<PostLogRequest, PostLogResult>() {
                @Override
                public void onSuccess(PostLogRequest request, PostLogResult result) {
                    Message message = Message.obtain(handler);
                    message.what = HANDLER_MESSAGE_UPLOAD_SUCCESS;
                    message.sendToTarget();
                }

                @Override
                public void onFailure(PostLogRequest request, LogException exception) {
                    Message message = Message.obtain(handler);
                    message.what = HANDLER_MESSAGE_UPLOAD_FAILED;
                    message.obj = exception.getMessage();
                    message.sendToTarget();
                }
            });
        }catch (LogException e){
            e.printStackTrace();
        }
    }


    /*
     *  0.3.1 以下版本的使用方式(不推荐，但兼容)
     */
    private void sampleUploadLog(@Nullable String ip) {
        if (TextUtils.isEmpty(ip)){
            Toast.makeText(MainActivity.this,"请先获取ip地址",Toast.LENGTH_SHORT).show();
            return;
        }
        final LOGClient logClient = new LOGClient(endpoint, accesskeyID,
                accessKeySecret, project);
        /* 创建logGroup */
        final LogGroup logGroup = new LogGroup("sls test", ip);

        /* 存入一条log */
        Log log = new Log();
        log.PutContent("current time ", "" + System.currentTimeMillis() / 1000);
        log.PutContent("content", "this is a log");

        logGroup.PutLog(log);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    /* 发送log 会调用网络操作，需要在一个异步线程中完成*/
                    logClient.PostLog(logGroup, logStore);
                }catch (Exception e){
                    e.printStackTrace();
                    Message message = Message.obtain(handler);
                    message.what = HANDLER_MESSAGE_UPLOAD_FAILED;
                    message.obj = e.getMessage();
                    message.sendToTarget();
                    return;
                }
                Message message = Message.obtain(handler);
                message.what = HANDLER_MESSAGE_UPLOAD_SUCCESS;
                message.sendToTarget();
            }
        }).start();
    }


}