package com.sanron.jsbridge

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sanron.jsbridge.call.NativeCallback
import org.json.JSONObject
import java.util.concurrent.*

/**
 *
 * @author chenrong
 * @date 2023/4/5
 */
class BridgeWebView : WebView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        privateBrowsing: Boolean
    ) : super(context, attrs, defStyleAttr, privateBrowsing)

    private val mSBridge = SBridge(this)

    fun callWeb(method: String, args: JSONObject?, nativeCallback: NativeCallback) {
        mSBridge.callWeb(method, args, nativeCallback)
    }

    fun setWebCallExecutor(executor: Executor) {
        mSBridge.setWebCallExecutor(executor)
    }

    override fun setWebViewClient(client: WebViewClient) {
        mSBridge.setWebViewClient(client)
    }

    override fun setWebChromeClient(client: WebChromeClient?) {
        mSBridge.setWebChromeClient(client)
    }

    fun addNativeObj(name: String, obj: Any) {
        mSBridge.addNativeObj(name, obj)
    }

    fun removeNativeObj(name: String) {
        mSBridge.removeNativeObj(name)
    }

    fun setLoggerEnabled(enable: Boolean) {
        Logger.enable = enable
    }
}