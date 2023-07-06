package com.aliyun.sls.android.webview.instrumentation;

import android.webkit.WebSettings;
import android.webkit.WebView;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentation.WebViewInstrumentationConfiguration;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * @author yulong.gyl
 * @date 2023/7/6
 */
@RunWith(MockitoJUnitRunner.class)
public class WebViewInstrumentationTests {

    @Mock
    private WebSettings webSettings;
    @Mock
    private WebView webView;

    @Mock
    OpenTelemetry telemetry;

    @Test
    public void testConstructor() {
        //try (MockedStatic<WebView> mocked = mockStatic(WebView.class)){
        //    //doNothing().when(mocked)
        //    //doNothing().when(mocked.when(WebView::setWebContentsDebuggingEnabled)
        //    doAnswer(invocation -> {
        //        return null;
        //    }).when(WebView::setWebContentsDebuggingEnabled);
        //}

        when(webView.getSettings()).thenReturn(webSettings);
        when(webSettings.getUserAgentString()).thenReturn("test useragent");
        //doAnswer(invocation -> {
        //    return null;
        //}).when(webView).addJavascriptInterface();
        //verify(webView).addJavascriptInterface(new OTelJSI(), );

        GlobalOpenTelemetry.set(telemetry);

        WebViewInstrumentationConfiguration configuration = new WebViewInstrumentationConfiguration(telemetry);
        WebViewInstrumentation instrumentation = new WebViewInstrumentation(webView, configuration);

        assertEquals(instrumentation.configuration, configuration);
        assertEquals(instrumentation.webView, webView);
        assertNotNull(instrumentation.requestInstrumentation);
        assertEquals("test useragent", instrumentation.userAgent);
    }

    @Test
    public void testStart() {
        //doAnswer(invocation -> {
        //
        //}).when(webView::webc)
    }
}
