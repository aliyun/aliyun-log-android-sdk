package com.aliyun.sls.android.producer.example;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author gordon
 * @date 2021/08/18
 */
public class BaseActivity extends AppCompatActivity {

    protected String endpoint;
    protected String logProject;
    protected String logStore;
    protected String accessKeyId;
    protected String accessKeySecret;
    protected String accessKeyToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle parameters = null != getIntent() ? getIntent().getExtras() : null;
        if (null == parameters) {
            return;
        }

        this.endpoint = parameters.getString("endpoint");
        this.logProject = parameters.getString("logProject");
        this.logStore = parameters.getString("logStore");
        this.accessKeyId = parameters.getString("accessKeyId");
        this.accessKeySecret = parameters.getString("accessKeySecret");
        this.accessKeyToken = parameters.getString("accessKeyToken");
    }

    @Override
    protected void onResume() {
        super.onResume();
        printParameters();
    }

    /**
     * 打印配置参数信息
     */
    protected void printParameters() {
        final TextView textView = findViewById(R.id.example_parameters_text);
        if (null == textView) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("endpoint: ").append(endpoint).append("\n");
        builder.append("logProject: ").append(logProject).append("\n");
        builder.append("logStore: ").append(logStore).append("\n");
        builder.append("accessKeyId: ").append(accessKeyId).append("\n");
        builder.append("accessKeySecret: ").append(accessKeySecret).append("\n");
        builder.append("accessKeyToken: ").append(accessKeyToken).append("\n");

        textView.setText(builder);
    }

    /**
     * 打印状态信息
     * @param msg
     */
    protected void printStatus(String msg) {
        final String message = msg + " thread: " + Thread.currentThread();
        final TextView textView = findViewById(R.id.example_console_text);
        textView.post(new Runnable() {
            @Override
            public void run() {
                textView.append(message);
                textView.append("\n");
                int scrollAmount = textView.getLayout().getLineTop(textView.getLineCount()) - textView.getHeight();
                textView.scrollTo(0, Math.max(scrollAmount, 0));
            }
        });
    }

}
