package com.aliyun.sls.android.webview.instrumentation;

import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author yulong.gyl
 * @date 2023/7/6
 */
public class PayloadManagerTests {
    private static final String requestId = "1234567";

    @Test
    public void testSet() {
        WebRequestInfo info = new WebRequestInfo();
        info.url = "https://www.aliyun.com";
        PayloadManager.getInstance().set(requestId, info);

        assertNotNull(PayloadManager.getInstance().cachedRequests.get(requestId));
        assertEquals(info.url, PayloadManager.getInstance().cachedRequests.get(requestId).url);
        assertEquals(info, PayloadManager.getInstance().cachedRequests.get(requestId));

        PayloadManager.getInstance().cachedRequests.remove(requestId);
    }

    @Test
    public void testGet() {
        WebRequestInfo info = new WebRequestInfo();
        info.url = "https://www.aliyun.com";
        PayloadManager.getInstance().set(requestId, info);

        assertNotNull(PayloadManager.getInstance().get(requestId));
        PayloadManager.getInstance().cachedRequests.remove(requestId);
    }

    @Test
    public void testRemove() {
        WebRequestInfo info = new WebRequestInfo();
        info.url = "https://www.aliyun.com";
        PayloadManager.getInstance().set(requestId, info);

        assertNotNull(PayloadManager.getInstance().get(requestId));

        PayloadManager.getInstance().remove(requestId);
        assertNull(PayloadManager.getInstance().get(requestId));

        PayloadManager.getInstance().remove(requestId);
    }

}
