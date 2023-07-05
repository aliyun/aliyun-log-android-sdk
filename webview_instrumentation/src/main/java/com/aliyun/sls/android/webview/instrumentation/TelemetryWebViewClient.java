package com.aliyun.sls.android.webview.instrumentation;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;
import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;
import com.aliyun.sls.android.webview.instrumentation.httpclient.HttpClient;
import com.aliyun.sls.android.webview.instrumentation.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @author yulong.gyl
 * @date 2023/6/21
 */
public class TelemetryWebViewClient extends WebViewClient {
    private static final String TAG = "TelemetryWebViewClient";
    WebViewInstrumentation instrumentation;

    public TelemetryWebViewClient(WebViewInstrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Log.d(TAG, "shouldInterceptRequest. path: " + request.getUrl().getPath() + "url: " + request.getUrl().toString());

        if (!instrumentation.configuration.shouldInstrument(request)) {
            return null;
        }

        final WebResourceResponse response;
        if (request.isForMainFrame()) {
            String newHtml = injectJSHook(view.getContext(), HttpClient.requestHtml(instrumentation, view, request));
            response = new WebResourceResponse("text/html", "utf-8", Utils.string2Input(newHtml));
        } else {
            if (request.getUrl().toString().contains("otel_flag")) {
                String requestId = request.getUrl().getQueryParameter("otel_flag");
                WebRequestInfo model = PayloadManager.get(requestId);
                Log.d(TAG, "shouldInterceptRequest. requestId: " + requestId + ", model: " + model);

            }
            response = super.shouldInterceptRequest(view, request);
        }

        Log.d(TAG, "response: " + response);

        return response;
    }

    private String injectJSHook(Context context, String originHtml) {
        String jsHook = Utils.readFromAssets(context, "telemetry_js_hook.js");
        if (TextUtils.isEmpty(jsHook)) {
            return originHtml;
        }

        Document document = Jsoup.parse(originHtml);
        document.outputSettings().prettyPrint(true);
        Elements elements = document.getElementsByTag("head");
        if (elements.size() > 0) {
            elements.get(0).prepend(jsHook);
        }
        return document.toString();
    }
}
