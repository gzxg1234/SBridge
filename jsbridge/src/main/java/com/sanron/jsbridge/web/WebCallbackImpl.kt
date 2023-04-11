package com.sanron.jsbridge.web

import android.webkit.WebView
import com.sanron.jsbridge.BridgeUtils
import com.sanron.jsbridge.BridgeUtils.evalJs
import com.sanron.jsbridge.json
import java.lang.ref.WeakReference

/**
 *
 * @author chenrong
 * @date 2023/4/6
 */
class WebCallbackImpl(webView: WebView, val id: String) : WebCallback {

    internal val webViewRef = WeakReference(webView)
    private var isAlive = true

    companion object {
        const val JS_CALLBACK_METHOD = "window.___callbackFromNative"
        const val KEY_VALUE = "value"
        const val KEY_RELEASE = "release"
        const val KEY_NEXT = "next"
    }

    init {
        CallbackRecycler.add(this)
    }

    override fun onNext(value: String?) {
        if (isAlive) {
            postResult(value, true, false)
        }
    }

    override fun onRelease() {
        if (isAlive) {
            isAlive = false
            postResult(null, false, true)
        }
    }

    private fun postResult(
        value: String?, next: Boolean, release: Boolean
    ) {
        webViewRef.get()?.let { webView ->
            val resultString = json(
                KEY_VALUE to value,
                KEY_NEXT to next,
                KEY_RELEASE to release
            ).toString()
            BridgeUtils.runOnMain {
                webView.evalJs("$JS_CALLBACK_METHOD(\"$id\",$resultString)")
            }
        }
    }
}