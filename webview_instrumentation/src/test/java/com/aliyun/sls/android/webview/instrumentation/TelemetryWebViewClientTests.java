package com.aliyun.sls.android.webview.instrumentation;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import io.opentelemetry.api.GlobalOpenTelemetry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author yulong.gyl
 * @date 2023/7/6
 */
@RunWith(MockitoJUnitRunner.class)
public class TelemetryWebViewClientTests {

    @Mock
    private WebSettings webSettings;
    @Mock
    private WebView webView;

    @Mock
    private Context context;
    @Mock
    private WebResourceRequest webResourceRequest;
    @Mock
    private Uri uri;

    @Before
    public void before() {

        TelemetryTestHelper.initTelemetry();
        when(webView.getSettings()).thenReturn(webSettings);
    }

    @Test
    public void testConstructor() {
        // parameter is null
        TelemetryWebViewClient client = new TelemetryWebViewClient(null);
        assertNull(client.instrumentation);

        // parameter is not null
        WebViewInstrumentationConfiguration configuration = new WebViewInstrumentationConfiguration(
            GlobalOpenTelemetry.get());
        WebViewInstrumentation instrumentation = new WebViewInstrumentation(webView, configuration);
        client = new TelemetryWebViewClient(instrumentation);
        assertEquals(instrumentation, client.instrumentation);
    }

    //@Test
    //public void testShouldInterceptRequest() {
    //    when(webView.getContext()).thenReturn(context);
    //    Uri uri = mock(Uri.class);
    //    when(uri.getPath()).thenReturn("/");
    //
    //    when(webResourceRequest.getUrl()).thenReturn(uri);
    //
    //    WebViewInstrumentationConfiguration configuration = new WebViewInstrumentationConfiguration(telemetry);
    //    WebViewInstrumentation instrumentation = new WebViewInstrumentation(webView, configuration);
    //    TelemetryWebViewClient client = new TelemetryWebViewClient(instrumentation);
    //    // main frame
    //    //when(webResourceRequest.isForMainFrame()).thenReturn(true);
    //    //WebResourceResponse response = client.shouldInterceptRequest(webView, webResourceRequest);
    //    //assertTrue(response instanceof InternalWebResourceResponse);
    //
    //    // otherwise
    //    when(webResourceRequest.isForMainFrame()).thenReturn(false);
    //    WebResourceResponse response = client.shouldInterceptRequest(webView, webResourceRequest);
    //    assertFalse(response instanceof InternalWebResourceResponse);
    //}

    @Test
    public void testInjectJSHook() {
        final String jsHookString = "<script type=\"text/javascript\">";
        final String originHtml = "<!DOCTYPE html>\n"
            + "<html>\n"
            + "<head>\n"
            + "<meta charset=\"utf-8\">\n"
            + "<title>文档标题</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "\t<h1>我的第一个HTML页面</h1>\n"
            + "\t<p>我的第一个段落。</p>\n"
            + "</body>\n"
            + "</html>\n";
        TelemetryWebViewClient client = createTelemetryWebViewClient();
        String target = client.injectJSHook(jsHookString, originHtml);

        assertTrue(target.contains(jsHookString));

        Document document = Jsoup.parse(target);
        Elements elements = document.getElementsByTag("head");

        assertTrue(elements.size() > 0);
        assertTrue(elements.get(0).toString().contains(jsHookString));
    }

    private TelemetryWebViewClient createTelemetryWebViewClient() {
        //when(webResourceRequest.isForMainFrame()).thenReturn(true);
        WebViewInstrumentationConfiguration configuration = new WebViewInstrumentationConfiguration(
            GlobalOpenTelemetry.get());
        WebViewInstrumentation instrumentation = new WebViewInstrumentation(webView, configuration);
        return new TelemetryWebViewClient(instrumentation);
    }
}
