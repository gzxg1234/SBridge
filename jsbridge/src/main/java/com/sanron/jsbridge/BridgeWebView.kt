package com.sanron.jsbridge

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sanron.jsbridge.call.NativeCallback
import com.sanron.jsbridge.convert.ParameterConverter
import org.json.JSONObject
import java.util.concurrent.Executor

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

    private val bridge = SBridge(this)

    init {
        super.setWebViewClient(bridge.getWebViewClientProxy())
        super.setWebChromeClient(bridge.getWebChromeClientProxy())
    }

    override fun setWebChromeClient(client: WebChromeClient?) {
        bridge.setWebChromeClient(client)
    }

    override fun setWebViewClient(client: WebViewClient) {
        bridge.setWebViewClient(client)
    }

    fun addNativeObj(name: String, obj: Any) {
        bridge.addNativeObj(name, obj)
    }

    fun addConverter(converter: ParameterConverter<*>) {
        bridge.addConverter(converter)
    }

    fun callWebMethod(method: String, args: JSONObject, nativeCallback: NativeCallback) {
        bridge.callWebMethod(method, args, nativeCallback)
    }

    fun setAsyncExecutor(executor: Executor) {
        bridge.setAsyncExecutor(executor)
    }
}