package com.aliyun.sls.android.producer.example;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.aliyun.sls.android.producer.R;
import com.aliyun.sls.android.producer.example.utils.PreferenceUtils;

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
    protected String pluginAppId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.endpoint = PreferenceUtils.getEndpoint(this);
        this.logProject = PreferenceUtils.getLogProject(this);
        this.logStore = PreferenceUtils.getLogStore(this);
        this.accessKeyId = PreferenceUtils.getAccessKeyId(this);
        this.accessKeySecret = PreferenceUtils.getAccessKeySecret(this);
        this.accessKeyToken = PreferenceUtils.getAccessKeyToken(this);
        this.pluginAppId = PreferenceUtils.getPluginAppId(this);
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
     *
     * @param msg
     */
    protected void printStatus(String msg) {
        final TextView textView = findViewById(R.id.example_console_text);
        if (null == textView) {
            return;
        }

        final String message = "> " + msg + " thread: " + Thread.currentThread();
        textView.post(() -> {
            textView.setMovementMethod(ScrollingMovementMethod.getInstance());
            textView.append(message);
            textView.append("\n");
            int scrollAmount = textView.getLayout().getLineTop(textView.getLineCount()) - textView.getHeight();
            textView.scrollTo(0, Math.max(scrollAmount, 0));
        });
    }

}
