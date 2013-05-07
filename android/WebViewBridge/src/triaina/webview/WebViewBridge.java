package triaina.webview;

import java.net.URLEncoder;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import triaina.commons.exception.SecurityRuntimeException;
import triaina.commons.json.JSONConverter;
import triaina.commons.utils.JSONObjectUtils;
import triaina.commons.utils.UriUtils;
import triaina.webview.DeviceBridgeProxy;
import triaina.webview.config.BridgeConfig;
import triaina.webview.config.DomainConfig;
import triaina.webview.entity.Error;
import triaina.webview.entity.Params;
import triaina.webview.entity.Result;
import triaina.webview.exception.SkipDomainCheckRuntimeException;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewBridge extends WebView {
    public static final float VERSION = 1.2F;
    public static final double COMPATIBLE_VERSION = Math.floor(VERSION);

    private static final String TAG = WebViewBridge.class.getSimpleName();
    private static final String JAVASCRIPT_INTERFACE_NAME = "DeviceBridge";

    private Handler mHandler;
    private DomainConfig mDomainConfig;

    private DeviceBridgeProxy mDeviceBridgeProxy;

    private WebViewBridgeHelper mHelper = new WebViewBridgeHelper();

    private AtomicInteger mSeq = new AtomicInteger();

    private Map<String, Callback<?>> callbacks = new ConcurrentHashMap<String, Callback<?>>();

    private boolean mIsDestroyed;

    private boolean mNoPause;

    public WebViewBridge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WebViewBridge(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebViewBridge(Context context) {
        super(context);
    }

    public void setWebViewClient(WebViewClient client) {
        super.setWebViewClient(new WebViewClientProxy(client, mDomainConfig));
    }

    public void setDomainConfig(DomainConfig domainConfig) {
        mDomainConfig = domainConfig;
    }

    public DomainConfig getDomainConfig() {
        return mDomainConfig;
    }

    public void addBridgeWithConfig(Object bridgeObject, BridgeConfig config) {
        addBridgeWithConfig(bridgeObject, config, new Handler(getContext().getMainLooper()));
    }

    public void addBridgeWithConfig(Object bridgeObject, BridgeConfig config, Handler handler) {
        mHandler = handler;

        if (mDeviceBridgeProxy == null) {
            mDeviceBridgeProxy = new DeviceBridgeProxy(this, mHandler);
            addJavascriptInterface(mDeviceBridgeProxy, JAVASCRIPT_INTERFACE_NAME);
        }

        mDeviceBridgeProxy.addBridgeObjectConfig(bridgeObject, config);
    }

    public void setNoPause(boolean noPause) {
        mNoPause = noPause;
    }

    public void onPause() {
        if (!mNoPause)
            super.onPause();
    }

    /**
     * for unit test
     * 
     * @return
     */
    public DeviceBridgeProxy getDeviceBridgeProxy() {
        return mDeviceBridgeProxy;
    }

    public BridgeConfig getBridgeConfigSet() {
        return mDeviceBridgeProxy.getBridgeConfigSet();
    }

    public String call(String dest, Params params) {
        if (mIsDestroyed)
            return null;
        return notifyToWebInternal(null, dest, "params", params);
    }

    public String call(String dest, Params params, Callback<?> callback) {
        if (mIsDestroyed)
            return null;

        String id = mSeq.incrementAndGet() + "";
        String js = notifyToWebInternal(id, dest, "params", params);
        if (js != null)
            callbacks.put(id, callback);

        return js;
    }

    public Callback<?> getCallback(String id) {
        return callbacks.get(id);
    }

    public void removeCallback(String id) {
        callbacks.remove(id);
    }

    public String returnToWeb(String id, String dest, Result result) {
        if (mIsDestroyed || TextUtils.isEmpty(id))
            return null;
        return notifyToWebInternal(id, dest, "result", result);
    }

    public String returnToWeb(String id, String dest, Error error) {
        if (mIsDestroyed || TextUtils.isEmpty(id)) {
            return null;
        }
        return notifyToWebInternal(id, dest, "error", error);
    }

    private String notifyToWebInternal(String id, String dest, String container, Object data) {
        try {
            JSONObject json = new JSONObject();
            JSONObjectUtils.put(json, "bridge", VERSION + "");
            JSONObjectUtils.put(json, "id", id);

            if (dest != null)
                JSONObjectUtils.put(json, "dest", dest);

            JSONObject jsonData = JSONConverter.toJSON(data);
            JSONObjectUtils.put(json, container, jsonData);

            String s = json.toString();
            Log.d(TAG, "Notify to Web with " + s);

            String js = mHelper.makeJavaScript("WebBridge.notifyToWeb",
                    URLEncoder.encode(s, "UTF-8").replace("+", "%20"));

            loadUrl(js);
            return js;// for test
        } catch (Exception exp) {
            Log.e(TAG, exp.getMessage() + "", exp);
        }
        return null;
    }

    public void resume() {
        mDeviceBridgeProxy.resume();
    }

    public void pause() {
        mDeviceBridgeProxy.pause();
    }

    @Override
    public void destroy() {
        if (mIsDestroyed)
            return;

        // workaround for some device
        try {
            mDeviceBridgeProxy.destroy();
            super.destroy();
        } catch (Exception exp) {
            Log.w(TAG, exp.getMessage() + "", exp);
        } finally {
            mIsDestroyed = true;
        }
    }

    public static class WebViewClientProxy extends WebViewClient {
        private WebViewClient mWebViewClient;
        private DomainConfig mDomainConfig;

        public WebViewClientProxy(WebViewClient webViewClient, DomainConfig domainConfig) {
            mWebViewClient = webViewClient;
            mDomainConfig = domainConfig;
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return mWebViewClient.shouldOverrideUrlLoading(view, url);
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            try {
                // XXX ugly
                mWebViewClient.onPageStarted(view, url, favicon);
            } catch (SkipDomainCheckRuntimeException exp) {
                return;
            }

            Uri uri = Uri.parse(url);
            String[] domains = mDomainConfig.getDomains();
            for (String domain : domains) {
                if (UriUtils.compareDomain(uri, domain))
                    return;
            }

            throw new SecurityRuntimeException("cannot load " + url);
        }

        public void onPageFinished(WebView view, String url) {
            mWebViewClient.onPageFinished(view, url);
        }

        public void onLoadResource(WebView view, String url) {
            mWebViewClient.onLoadResource(view, url);
        }

        @SuppressWarnings("deprecation")
        public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
            mWebViewClient.onTooManyRedirects(view, cancelMsg, continueMsg);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            mWebViewClient.onReceivedError(view, errorCode, description, failingUrl);
        }

        public boolean equals(Object o) {
            return mWebViewClient.equals(o);
        }

        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            mWebViewClient.onFormResubmission(view, dontResend, resend);
        }

        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            mWebViewClient.doUpdateVisitedHistory(view, url, isReload);
        }

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            mWebViewClient.onReceivedSslError(view, handler, error);
        }

        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            mWebViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
        }

        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return mWebViewClient.shouldOverrideKeyEvent(view, event);
        }

        public int hashCode() {
            return mWebViewClient.hashCode();
        }

        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            mWebViewClient.onUnhandledKeyEvent(view, event);
        }

        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            mWebViewClient.onScaleChanged(view, oldScale, newScale);
        }

        public String toString() {
            return mWebViewClient.toString();
        }
    }
}
