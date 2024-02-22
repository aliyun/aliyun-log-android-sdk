package com.aliyun.sls.android.webview.instrumentation.jsbridge;

import com.aliyun.sls.android.webview.instrumentation.PayloadManager;
import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentationConfiguration;
import com.aliyun.sls.android.webview.instrumentation.instrumentation.IWebRequestInstrumentation;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author yulong.gyl
 * @date 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class OTelJSITests {
    private static final String requestId = "11212121";
    private static final String url = "http://www.aliyun.com/test";
    private static final String method = "GET";
    private static final String origin = "http://www.aliyun.com";
    private static final String headers = new JSONObject() {
        {
            try {
                put("h_1", "va_1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }.toString();
    private static final String body = "test";

    @After
    public void after() {
        PayloadManager.getInstance().remove(requestId);
    }

    @Test
    public void testFetch() {
        OTelJSI oTelJSI = createOTelJSIInstance();

        Assert.assertNull(PayloadManager.getInstance().get(requestId));

        oTelJSI.fetch(requestId, url, method, origin, headers, body);
        final WebRequestInfo info = PayloadManager.getInstance().get(requestId);
        Assert.assertNotNull(info);

        Assert.assertEquals(requestId, info.requestId);
        Assert.assertEquals(url, info.url);
        Assert.assertEquals(method, info.method);
        Assert.assertEquals(origin, info.origin);
        Assert.assertEquals(headers, info.headers.toString());
        Assert.assertEquals(body, info.body);
    }

    @Test
    public void testHeader2JSON() {
        OTelJSI oTelJSI = createOTelJSIInstance();
        JSONObject object = oTelJSI.header2JSON(null);
        Assert.assertEquals(0, object.length());

        object = oTelJSI.header2JSON(new JSONObject() {
            {
                try {
                    put("name", "value");
                } catch (JSONException e) {
                }
            }
        }.toString());
        Assert.assertEquals("value", object.optString("name"));
    }

    @Test
    public void testOpen() {
        OTelJSI oTelJSI = createOTelJSIInstance();
        Assert.assertNull(PayloadManager.getInstance().get(requestId));

        oTelJSI.open(requestId, url, method, origin);
        final WebRequestInfo info = PayloadManager.getInstance().get(requestId);
        Assert.assertNotNull(info);

        Assert.assertEquals(requestId, info.requestId);
        Assert.assertEquals(url, info.url);
        Assert.assertEquals(method, info.method);
        Assert.assertEquals(origin, info.origin);
    }

    @Test
    public void testSetRequestHeader() {
        OTelJSI oTelJSI = createOTelJSIInstance();
        Assert.assertNull(PayloadManager.getInstance().get(requestId));

        oTelJSI.setRequestHeader(requestId, "key", "value");
        final WebRequestInfo info = PayloadManager.getInstance().get(requestId);
        Assert.assertNotNull(info);
        Assert.assertNotNull(info.headers);
        Assert.assertEquals("value", info.headers.optString("key"));
    }

    @Test
    public void testPutHeader() {
        OTelJSI oTelJSI = createOTelJSIInstance();
        JSONObject headers = new JSONObject();
        oTelJSI.putHeader(headers, "key", "value");
        Assert.assertNotEquals(0, headers.length());
        Assert.assertEquals("value", headers.optString("key"));
    }

    @Test
    public void testOverrideMimeType() {
        OTelJSI oTelJSI = createOTelJSIInstance();

        oTelJSI.fetch(requestId, url, method, origin, headers, body);
        Assert.assertNotNull(PayloadManager.getInstance().get(requestId));

        oTelJSI.overrideMimeType(requestId, "test/test");

        final WebRequestInfo info = PayloadManager.getInstance().get(requestId);
        Assert.assertNotNull(info);
        Assert.assertEquals("test/test", info.mimeType);
    }

    @Test
    public void testSend() {
        OTelJSI oTelJSI = createOTelJSIInstance();
        oTelJSI.open(requestId, url, method, origin);
        Assert.assertNotNull(PayloadManager.getInstance().get(requestId));

        oTelJSI.send(requestId, "test");

        final WebRequestInfo info = PayloadManager.getInstance().get(requestId);
        Assert.assertNotNull(info);
        Assert.assertEquals("test", info.body);
    }

    @Test
    public void testInternalHandleResponse() {
        OTelJSI oTelJSI = createOTelJSIInstance();
        oTelJSI.fetch(requestId, url, method, origin, headers, body);
        Assert.assertNotNull(PayloadManager.getInstance().get(requestId));

        oTelJSI.internalHandleResponse(PayloadManager.getInstance().get(requestId), 356, "error", "body",
            new JSONObject() {
                {
                    try {
                        put("key", "value");
                    } catch (JSONException e) {
                    }
                }
            }.toString());

        final WebRequestInfo info = PayloadManager.getInstance().get(requestId);
        Assert.assertNotNull(info);
        Assert.assertEquals(356, info.responseStatus);
        Assert.assertEquals("error", info.responseStatusText);
        Assert.assertEquals("body", info.responseBody);
        Assert.assertEquals("value", info.responseHeaders.optString("key"));
    }

    @Test
    public void testHandleResponse() {
        OTelJSI oTelJSI = createOTelJSIInstance();
        oTelJSI.fetch(requestId, url, method, origin, headers, body);
        Assert.assertNotNull(PayloadManager.getInstance().get(requestId));

        oTelJSI.handleResponse(requestId, 344, "error", "body", new JSONObject() {
            {
                try {
                    put("name", "value");
                } catch (JSONException e) {
                }
            }
        }.toString());
        Assert.assertNull(PayloadManager.getInstance().get(requestId));
    }

    private OTelJSI createOTelJSIInstance() {
        return new OTelJSI(new WebViewInstrumentationConfiguration(null), new IWebRequestInstrumentation() {
            @Override
            public void createdRequest(WebRequestInfo info) {

            }

            @Override
            public void receivedResponse(WebRequestInfo info) {

            }
        });
    }
}
