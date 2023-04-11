package com.sanron.jsbridge.client

import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sanron.jsbridge.BridgeUtils
import com.sanron.jsbridge.SBridge

/**
 *
 * @author chenrong
 * @date 2023/4/5
 */
internal class SWebViewClient(
    val bridge: SBridge,
    webViewClient: WebViewClient? = null
) : WebViewClientWrapper(webViewClient) {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        bridge.clearNativeCallback()
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        if (BridgeUtils.LOCAL_JS_FILE_REQUEST_URL == request?.url.toString()) {
            return WebResourceResponse(
                "text/javascript",
                "utf-8",
                BridgeUtils.jsFileStream(view!!.context)
            )
        }
        return super.shouldInterceptRequest(view, request)
    }

    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        if (BridgeUtils.LOCAL_JS_FILE_REQUEST_URL == url) {
            return WebResourceResponse(
                "text/javascript",
                "utf-8",
                BridgeUtils.jsFileStream(view!!.context)
            )
        }
        return super.shouldInterceptRequest(view, url)
    }

}