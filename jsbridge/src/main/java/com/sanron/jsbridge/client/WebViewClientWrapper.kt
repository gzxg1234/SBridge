package com.sanron.jsbridge.client

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.view.KeyEvent
import android.webkit.*

/**
 *
 * @author chenrong
 * @date 2023/4/5
 */
open class WebViewClientWrapper(var webViewClient: WebViewClient? = null) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        webViewClient?.let {
            return it.shouldOverrideUrlLoading(view, url)
        }
        return super.shouldOverrideUrlLoading(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        webViewClient?.let {
            return it.shouldOverrideUrlLoading(view, request)
        }
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        webViewClient?.let {
            it.onPageStarted(view, url, favicon)
            return
        }
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        webViewClient?.let {
            it.onPageFinished(view, url)
            return
        }
        super.onPageFinished(view, url)
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        webViewClient?.let {
            it.onLoadResource(view, url)
            return
        }
        super.onLoadResource(view, url)
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        webViewClient?.let {
            it.onPageCommitVisible(view, url)
            return
        }
        super.onPageCommitVisible(view, url)
    }

    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        webViewClient?.let {
            return it.shouldInterceptRequest(view, url)
        }
        return super.shouldInterceptRequest(view, url)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        webViewClient?.let {
            return it.shouldInterceptRequest(view, request)
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onTooManyRedirects(view: WebView?, cancelMsg: Message?, continueMsg: Message?) {
        webViewClient?.let {
            it.onTooManyRedirects(view, cancelMsg, continueMsg)
            return
        }
        super.onTooManyRedirects(view, cancelMsg, continueMsg)
    }

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        webViewClient?.let {
            it.onReceivedError(view, errorCode, description, failingUrl)
            return
        }
        super.onReceivedError(view, errorCode, description, failingUrl)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        webViewClient?.let {
            it.onReceivedError(view, request, error)
            return
        }
        super.onReceivedError(view, request, error)
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        webViewClient?.let {
            it.onReceivedHttpError(view, request, errorResponse)
            return
        }
        super.onReceivedHttpError(view, request, errorResponse)
    }

    override fun onFormResubmission(view: WebView?, dontResend: Message?, resend: Message?) {
        webViewClient?.let {
            it.onFormResubmission(view, dontResend, resend)
            return
        }
        super.onFormResubmission(view, dontResend, resend)
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        webViewClient?.let {
            it.doUpdateVisitedHistory(view, url, isReload)
            return
        }
        super.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        webViewClient?.let {
            it.onReceivedSslError(view, handler, error)
            return
        }
        super.onReceivedSslError(view, handler, error)
    }

    override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
        webViewClient?.let {
            it.onReceivedClientCertRequest(view, request)
            return
        }
        super.onReceivedClientCertRequest(view, request)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        webViewClient?.let {
            it.onReceivedHttpAuthRequest(view, handler, host, realm)
            return
        }
        super.onReceivedHttpAuthRequest(view, handler, host, realm)
    }

    override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
        webViewClient?.let {
            return it.shouldOverrideKeyEvent(view, event)
        }
        return super.shouldOverrideKeyEvent(view, event)
    }

    override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
        webViewClient?.let {
            it.onUnhandledKeyEvent(view, event)
            return
        }
        super.onUnhandledKeyEvent(view, event)
    }

    override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
        webViewClient?.let {
            it.onScaleChanged(view, oldScale, newScale)
            return
        }
        super.onScaleChanged(view, oldScale, newScale)
    }

    override fun onReceivedLoginRequest(
        view: WebView?,
        realm: String?,
        account: String?,
        args: String?
    ) {
        webViewClient?.let {
            it.onReceivedLoginRequest(view, realm, account, args)
            return
        }
        super.onReceivedLoginRequest(view, realm, account, args)
    }

    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        webViewClient?.let {
            return it.onRenderProcessGone(view, detail)
        }
        return super.onRenderProcessGone(view, detail)
    }

    override fun onSafeBrowsingHit(
        view: WebView?,
        request: WebResourceRequest?,
        threatType: Int,
        callback: SafeBrowsingResponse?
    ) {
        webViewClient?.let {
            it.onSafeBrowsingHit(view, request, threatType, callback)
            return
        }
        super.onSafeBrowsingHit(view, request, threatType, callback)
    }
}