package com.aliyun.sls.android.webview.instrumentation;

import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.webkit.WebViewCompat;
import com.aliyun.sls.android.webview.instrumentation.httpclient.HttpClient;
import com.aliyun.sls.android.webview.instrumentation.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @author yulong.gyl
 * @date 2023/6/21
 */
public class TelemetryWebViewClient extends ProxyWebViewClient {
    private static final String TAG = "TelemetryWebViewClient";
    WebViewInstrumentation instrumentation;

    @SuppressLint("RequiresFeature")
    public TelemetryWebViewClient(WebViewInstrumentation instrumentation) {
        super(instrumentation.webViewClient);
        this.instrumentation = instrumentation;
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (instrumentation.configuration.debuggable()) {
            Log.d(TAG, "shouldInterceptRequest. path: " + request.getUrl().getPath()
                + ", url: " + request.getUrl().toString()
            );
        }

        if (!instrumentation.configuration.shouldInstrument(request)) {
            return super.shouldInterceptRequest(view, request);
        }

        final WebResourceResponse response;
        if (request.isForMainFrame()) {
            String newHtml = injectJSHook(view.getContext(), HttpClient.requestHtml(instrumentation, request));
            response = new InternalWebResourceResponse(Utils.string2Input(newHtml));
        } else {
            //if (request.getUrl().toString().contains("otel_flag")) {
            //    String url = Utils.fetchRequestIdPair(request.getUrl().toString());
            //    String requestId = request.getUrl().getQueryParameter("otel_flag");
            //    WebRequestInfo model = PayloadManager.get(requestId);
            //    Log.d(TAG, "shouldInterceptRequest. requestId: " + requestId + ", model: " + model);
            //
            //}
            response = super.shouldInterceptRequest(view, request);
        }

        //Log.d(TAG, "response: " + response);

        return response;
    }

    @VisibleForTesting
    public String injectJSHook(Context context, String originHtml) {
        if (null == originHtml) {
            return null;
        }

        String jsHook = Utils.readFromAssets(context, "telemetry_js_hook.js");
        if (TextUtils.isEmpty(jsHook)) {
            return originHtml;
        }

        return injectJSHook(jsHook, originHtml);
    }

    @VisibleForTesting
    public String injectJSHook(String jsHookString, String originHtml) {
        Document document = Jsoup.parse(originHtml);
        document.outputSettings().prettyPrint(true);
        Elements elements = document.getElementsByTag("head");
        if (elements.size() > 0) {
            elements.get(0).prepend(jsHookString);
        }
        return document.toString();
    }

    public static class InternalWebResourceResponse extends WebResourceResponse {

        public InternalWebResourceResponse(InputStream data) {
            super("text/html", "utf-8", data);
        }
    }
}
