package com.sanron.jsbridge.client

import android.net.Uri
import android.webkit.JsPromptResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.sanron.jsbridge.BridgeUtils
import com.sanron.jsbridge.Logger
import com.sanron.jsbridge.SBridge
import com.sanron.jsbridge.web.WebCall
import com.sanron.jsbridge.web.WebCallImpl
import org.json.JSONObject

/**
 *
 * @author chenrong
 * @date 2023/4/5
 */
internal class SWebChromeClient(
    val bridge: SBridge,
    webChromeClient: WebChromeClient? = null
) : WebChromeClientWrapper(webChromeClient) {

    override fun onJsPrompt(
        view: WebView,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult
    ): Boolean {
        if (message?.startsWith(BridgeUtils.SCHEME) == true) {
            runCatching {
                Uri.parse(message)
            }.onSuccess { uri ->
                when (uri.host) {
                    BridgeUtils.METHOD_WEB_CALL -> {
                        parseCall(bridge, uri, result)?.let {
                            bridge.handleCall(it)
                            return true
                        }
                    }
                }
            }
        }
        return super.onJsPrompt(view, url, message, defaultValue, result)
    }


    private fun parseCall(
        sBridge: SBridge, uri: Uri,
        jsPromptResult: JsPromptResult
    ): WebCall? {
        runCatching {
            val callObj = uri.getQueryParameter(BridgeUtils.QUERY_CALL)
            val json = JSONObject(callObj)
            val objName = json.optString("obj")
            val objMethod = json.optString("method")
            val objArgs = json.optJSONObject("args")
            val callbackIds = json.optJSONArray("callbackIds")

            return WebCallImpl(
                objName,
                objMethod,
                objArgs,
                callbackIds,
                sBridge.webView,
                jsPromptResult,
            )
        }.onFailure {
            Logger.w("parseCall failure", it)
        }
        return null
    }
}