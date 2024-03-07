package com.aliyun.sls.android.webview.instrumentation;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build.VERSION_CODES;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * @author yulong.gyl
 * @date 2023/8/25
 */
public class ProxyWebViewClient extends WebViewClient {
    private final WebViewClient webViewClient;

    public ProxyWebViewClient(WebViewClient webViewClient) {
        this.webViewClient = webViewClient;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return null != webViewClient ? webViewClient.shouldOverrideUrlLoading(view, url)
            : super.shouldOverrideUrlLoading(view, url);
    }

    @RequiresApi(api = VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return null != webViewClient ? webViewClient.shouldOverrideUrlLoading(view, request)
            : super.shouldOverrideUrlLoading(view, request);
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return null != webViewClient ? webViewClient.shouldInterceptRequest(view, url)
            : super.shouldInterceptRequest(view, url);
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return null != webViewClient ? webViewClient.shouldInterceptRequest(view, request)
            : super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (null != webViewClient) {
            webViewClient.onPageStarted(view, url, favicon);
        } else {
            super.onPageStarted(view, url, favicon);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (null != webViewClient) {
            webViewClient.onPageFinished(view, url);
        } else {
            super.onPageFinished(view, url);
        }
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        if (null != webViewClient) {
            webViewClient.onLoadResource(view, url);
        } else {
            super.onLoadResource(view, url);
        }
    }

    @RequiresApi(api = VERSION_CODES.M)
    @Override
    public void onPageCommitVisible(WebView view, String url) {
        if (null != webViewClient) {
            webViewClient.onPageCommitVisible(view, url);
        } else {
            super.onPageCommitVisible(view, url);
        }
    }

    @Override
    public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
        if (null != webViewClient) {
            webViewClient.onTooManyRedirects(view, cancelMsg, continueMsg);
        } else {
            super.onTooManyRedirects(view, cancelMsg, continueMsg);
        }
    }

    @RequiresApi(api = VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (null != webViewClient) {
            webViewClient.onReceivedError(view, request, error);
        } else {
            super.onReceivedError(view, request, error);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (null != webViewClient) {
            webViewClient.onReceivedError(view, errorCode, description, failingUrl);
        } else {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    @RequiresApi(api = VERSION_CODES.M)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        if (null != webViewClient) {
            webViewClient.onReceivedHttpError(view, request, errorResponse);
        } else {
            super.onReceivedHttpError(view, request, errorResponse);
        }
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        if (null != webViewClient) {
            webViewClient.onFormResubmission(view, dontResend, resend);
        } else {
            super.onFormResubmission(view, dontResend, resend);
        }
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        if (null != webViewClient) {
            webViewClient.doUpdateVisitedHistory(view, url, isReload);
        } else {
            super.doUpdateVisitedHistory(view, url, isReload);
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        if (null != webViewClient) {
            webViewClient.onReceivedSslError(view, handler, error);
        } else {
            super.onReceivedSslError(view, handler, error);
        }
    }

    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        if (null != webViewClient) {
            webViewClient.onReceivedClientCertRequest(view, request);
        } else {
            super.onReceivedClientCertRequest(view, request);
        }
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        if (null != webViewClient) {
            webViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
        } else {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        }
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return null != webViewClient ? webViewClient.shouldOverrideKeyEvent(view, event)
            : super.shouldOverrideKeyEvent(view, event);
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        if (null != webViewClient) {
            webViewClient.onUnhandledKeyEvent(view, event);
        } else {
            super.onUnhandledKeyEvent(view, event);
        }
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        if (null != webViewClient) {
            webViewClient.onScaleChanged(view, oldScale, newScale);
        } else {
            super.onScaleChanged(view, oldScale, newScale);
        }
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, @Nullable String account, String args) {
        if (null != webViewClient) {
            webViewClient.onReceivedLoginRequest(view, realm, account, args);
        } else {
            super.onReceivedLoginRequest(view, realm, account, args);
        }
    }

    @RequiresApi(api = VERSION_CODES.O)
    @Override
    public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
        return null != webViewClient ? webViewClient.onRenderProcessGone(view, detail)
            : super.onRenderProcessGone(view, detail);
    }

    @RequiresApi(api = VERSION_CODES.O_MR1)
    @Override
    public void onSafeBrowsingHit(WebView view, WebResourceRequest request, int threatType,
        SafeBrowsingResponse callback) {
        if (null != webViewClient) {
            webViewClient.onSafeBrowsingHit(view, request, threatType, callback);
        } else {
            super.onSafeBrowsingHit(view, request, threatType, callback);
        }
    }
}
