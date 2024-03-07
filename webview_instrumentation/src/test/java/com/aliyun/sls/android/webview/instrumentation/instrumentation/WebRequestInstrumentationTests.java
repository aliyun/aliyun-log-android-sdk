package com.aliyun.sls.android.webview.instrumentation.instrumentation;

import android.webkit.WebSettings;
import android.webkit.WebView;
import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;
import com.aliyun.sls.android.webview.instrumentation.TelemetryTestHelper;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentationConfiguration;
import io.opentelemetry.api.GlobalOpenTelemetry;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * @author yulong.gyl
 * @date 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class WebRequestInstrumentationTests {

    @Mock
    private WebSettings webSettings;
    @Mock
    private WebView webView;

    @BeforeClass
    public static void beforeClass() {
        TelemetryTestHelper.initTelemetry();
    }

    @Test
    public void testConstructor() {
        prepareWebRequestInstrumentationDeps();

        WebViewInstrumentationConfiguration configuration = new WebViewInstrumentationConfiguration(
            GlobalOpenTelemetry.get());
        WebViewInstrumentation instrumentation = new WebViewInstrumentation(webView, configuration);
        WebRequestInstrumentation requestInstrumentation = new WebRequestInstrumentation(instrumentation,
            configuration);

        Assert.assertNotNull(requestInstrumentation.instrumentation);
        Assert.assertNotNull(requestInstrumentation.configuration);
        Assert.assertNotNull(requestInstrumentation.tracer);
    }

    @Test
    public void testCreatedRequest() {
        prepareWebRequestInstrumentationDeps();
        WebViewInstrumentationConfiguration configuration = new WebViewInstrumentationConfiguration(
            GlobalOpenTelemetry.get());
        WebViewInstrumentation webViewInstrumentation = new WebViewInstrumentation(webView, configuration);
        WebRequestInstrumentation requestInstrumentation = new WebRequestInstrumentation(webViewInstrumentation,
            configuration);

        // spy requestInstrumentation while need call the original method
        WebRequestInstrumentation instrumentation = Mockito.spy(requestInstrumentation);

        final String url = "https://www.aliyun.com/test";
        // mock getPathFromUrl method than return the '/test'
        doReturn("/test").when(instrumentation).getPathFromUrl(url);
        //when(instrumentation.getPathFromUrl(url)).thenReturn("/test");

        WebRequestInfo requestInfo = new WebRequestInfo();

        // fallback test case
        requestInfo.requestId = "123456321";
        instrumentation.createdRequest(requestInfo);
        Assert.assertNull(instrumentation.cachedSpan.get(requestInfo.requestId));

        // normal test case
        requestInfo.url = url;
        requestInfo.headers = new JSONObject();
        instrumentation.createdRequest(requestInfo);
        Assert.assertNotNull(instrumentation.cachedSpan.get(requestInfo.requestId));
    }

    @Test
    public void testReceivedResponse() {
        prepareWebRequestInstrumentationDeps();
        WebViewInstrumentationConfiguration configuration = new WebViewInstrumentationConfiguration(
            GlobalOpenTelemetry.get());
        WebViewInstrumentation webViewInstrumentation = new WebViewInstrumentation(webView, configuration);
        WebRequestInstrumentation requestInstrumentation = new WebRequestInstrumentation(webViewInstrumentation,
            configuration);

        // spy requestInstrumentation while need call the original method
        WebRequestInstrumentation instrumentation = Mockito.spy(requestInstrumentation);
        final String url = "https://www.aliyun.com/test";
        doReturn("/test").when(instrumentation).getPathFromUrl(url);

        WebRequestInfo requestInfo = new WebRequestInfo();
        requestInfo.requestId = "123456321";
        requestInfo.url = url;

        Assert.assertNull(instrumentation.cachedSpan.get(requestInfo.requestId));

        instrumentation.createdRequest(requestInfo);
        instrumentation.receivedResponse(requestInfo);
        Assert.assertNull(instrumentation.cachedSpan.get(requestInfo.requestId));
    }

    public void prepareWebRequestInstrumentationDeps() {
        when(webView.getSettings()).thenReturn(webSettings);
        when(webSettings.getUserAgentString()).thenReturn("test useragent");
        //when(GlobalOpenTelemetry.get().getTracer("WebView-Instrumentation", "1.0.0")).thenReturn(tracer);
    }
}
