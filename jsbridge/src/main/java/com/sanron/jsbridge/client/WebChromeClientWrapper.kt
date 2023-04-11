package com.sanron.jsbridge.client

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Message
import android.view.View
import android.webkit.*

/**
 *
 * @author chenrong
 * @date 2023/4/5
 */
open class WebChromeClientWrapper(
    var webChromeClient: WebChromeClient? = null
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        webChromeClient?.let {
            it.onProgressChanged(view, newProgress)
            return
        }
        super.onProgressChanged(view, newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        webChromeClient?.let {
            it.onReceivedTitle(view, title)
            return
        }
        super.onReceivedTitle(view, title)
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        webChromeClient?.let {
            it.onReceivedIcon(view, icon)
            return
        }
        super.onReceivedIcon(view, icon)
    }

    override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {
        webChromeClient?.let {
            it.onReceivedTouchIconUrl(view, url, precomposed)
            return
        }
        super.onReceivedTouchIconUrl(view, url, precomposed)
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        webChromeClient?.let {
            it.onShowCustomView(view, callback)
            return
        }
        super.onShowCustomView(view, callback)
    }

    override fun onShowCustomView(
        view: View?,
        requestedOrientation: Int,
        callback: CustomViewCallback?
    ) {
        webChromeClient?.let {
            it.onShowCustomView(view, requestedOrientation, callback)
            return
        }
        super.onShowCustomView(view, requestedOrientation, callback)
    }

    override fun onHideCustomView() {
        webChromeClient?.let {
            it.onHideCustomView()
            return
        }
        super.onHideCustomView()
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        webChromeClient?.let {
            return it.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
        }
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
    }

    override fun onRequestFocus(view: WebView?) {
        webChromeClient?.let {
            it.onRequestFocus(view)
            return
        }
        super.onRequestFocus(view)
    }

    override fun onCloseWindow(window: WebView?) {
        webChromeClient?.let {
            it.onCloseWindow(window)
            return
        }
        super.onCloseWindow(window)
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        webChromeClient?.let {
            return it.onJsAlert(view, url, message, result)
        }
        return super.onJsAlert(view, url, message, result)
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        webChromeClient?.let {
            return it.onJsConfirm(view, url, message, result)
        }
        return super.onJsConfirm(view, url, message, result)
    }

    override fun onJsPrompt(
        view: WebView,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult
    ): Boolean {
        webChromeClient?.let {
            return it.onJsPrompt(view, url, message, defaultValue, result)
        }
        return super.onJsPrompt(view, url, message, defaultValue, result)
    }

    override fun onJsBeforeUnload(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        webChromeClient?.let {
            return it.onJsBeforeUnload(view, url, message, result)
        }
        return super.onJsBeforeUnload(view, url, message, result)
    }

    override fun onExceededDatabaseQuota(
        url: String?,
        databaseIdentifier: String?,
        quota: Long,
        estimatedDatabaseSize: Long,
        totalQuota: Long,
        quotaUpdater: WebStorage.QuotaUpdater?
    ) {
        webChromeClient?.let {
            it.onExceededDatabaseQuota(
                url,
                databaseIdentifier,
                quota,
                estimatedDatabaseSize,
                totalQuota,
                quotaUpdater
            )

            return
        }
        super.onExceededDatabaseQuota(
            url,
            databaseIdentifier,
            quota,
            estimatedDatabaseSize,
            totalQuota,
            quotaUpdater
        )
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        webChromeClient?.let {
            it.onGeolocationPermissionsShowPrompt(origin, callback)
            return
        }
        super.onGeolocationPermissionsShowPrompt(origin, callback)
    }

    override fun onGeolocationPermissionsHidePrompt() {
        webChromeClient?.let {
            it.onGeolocationPermissionsHidePrompt()
            return
        }
        super.onGeolocationPermissionsHidePrompt()
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        webChromeClient?.let {
            it.onPermissionRequest(request)
            return
        }
        super.onPermissionRequest(request)
    }

    override fun onPermissionRequestCanceled(request: PermissionRequest?) {
        webChromeClient?.let {
            it.onPermissionRequestCanceled(request)
            return
        }
        super.onPermissionRequestCanceled(request)
    }

    override fun onJsTimeout(): Boolean {
        webChromeClient?.let {
            return it.onJsTimeout()
        }
        return super.onJsTimeout()
    }

    override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
        webChromeClient?.let {
            it.onConsoleMessage(message, lineNumber, sourceID)
            return
        }
        super.onConsoleMessage(message, lineNumber, sourceID)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        webChromeClient?.let {
            return it.onConsoleMessage(consoleMessage)
        }
        return super.onConsoleMessage(consoleMessage)
    }

    override fun getDefaultVideoPoster(): Bitmap? {
        webChromeClient?.let {
            return it.getDefaultVideoPoster()
        }
        return super.getDefaultVideoPoster()
    }

    override fun getVideoLoadingProgressView(): View? {
        webChromeClient?.let {
            return it.getVideoLoadingProgressView()
        }
        return super.getVideoLoadingProgressView()
    }

    override fun getVisitedHistory(callback: ValueCallback<Array<String>>?) {
        webChromeClient?.let {
            it.getVisitedHistory(callback)
            return
        }
        super.getVisitedHistory(callback)
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        webChromeClient?.let {
            return it.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }
        return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
    }
}