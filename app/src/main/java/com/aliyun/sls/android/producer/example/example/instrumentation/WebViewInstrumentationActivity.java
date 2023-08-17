package com.aliyun.sls.android.producer.example.example.instrumentation;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.webkit.WebView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.aliyun.sls.android.producer.example.R;

/**
 * @author yulong.gyl
 * @date 2023/7/5
 */
public class WebViewInstrumentationActivity extends AppCompatActivity {
    private WebView webView = null;
    private String url = "https://www.aliyun.com";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.webView);

        //WebViewInstrumentationConfiguration configuration = new WebViewInstrumentationConfiguration
        // (GlobalOpenTelemetry.get());
        //WebViewInstrumentation instrumentation = new WebViewInstrumentation(webView, configuration);
        //instrumentation.start();

        //TransformerHelper.init(webView);
        webView.loadUrl("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com");

        webViewFromMethodParameter(webView, url);
        webViewFromLocal();

        loadUrlWithHeaders();
        loadData();
        loadDataWithBaseURL(webView);
    }

    private void webViewFromMethodParameter(WebView webView, String url) {
        webView.loadUrl(url);
    }

    private void webViewFromLocal() {
        WebView webView = new WebView(this);
        String url = "https://www.baidu.com";
        webView.loadUrl(url);
    }

    private void loadUrlWithHeaders() {
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("key", "value");
            }
        };
        webView.loadUrl(url, headers);
    }

    private void loadData() {

        webView.loadData("", "", "");
    }

    private void loadDataWithBaseURL(WebView webView) {
        webView.loadDataWithBaseURL(url, "", "", "", "");
    }
}
