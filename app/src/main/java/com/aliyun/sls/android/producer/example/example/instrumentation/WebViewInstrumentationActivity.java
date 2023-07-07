package com.aliyun.sls.android.producer.example.example.instrumentation;

import android.os.Bundle;
import android.webkit.WebView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.aliyun.sls.android.producer.example.R;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentation.WebViewInstrumentationConfiguration;
import io.opentelemetry.api.GlobalOpenTelemetry;

/**
 * @author yulong.gyl
 * @date 2023/7/5
 */
public class WebViewInstrumentationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        final WebView webView = findViewById(R.id.webView);

        WebViewInstrumentation instrumentation = new WebViewInstrumentation(
            webView,
            new WebViewInstrumentationConfiguration(GlobalOpenTelemetry.get()));
        instrumentation.start();

        webView.loadUrl("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com");
    }
}
