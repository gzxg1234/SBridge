package com.sanron.jsbridge.web

import android.webkit.JsPromptResult
import android.webkit.WebView
import com.sanron.jsbridge.BridgeUtils
import org.json.JSONArray
import org.json.JSONObject

/**
 *
 * @author chenrong
 * @date 2023/4/6
 */
class WebCallImpl(
    obj: String,
    method: String,
    args: JSONObject?,
    callbackIds: JSONArray?,
    val webView: WebView,
    val jsPromptResult: JsPromptResult,
) : WebCall(obj, method, args, callbackIds) {

    val webCallbackList: MutableList<WebCallback?> = mutableListOf()

    init {
        if (callbackIds != null && callbackIds.length() > 0) {
            for (i in 0 until callbackIds.length()) {
                val callId = callbackIds.optString(i, null)
                if (!callId.isNullOrEmpty()) {
                    webCallbackList.add(WebCallbackImpl(webView, callId))
                } else {
                    webCallbackList.add(null)
                }
            }
        }
    }

    override fun getCallbacks(): List<WebCallback?> {
        return webCallbackList
    }

    override fun sendResult(isSuccess: Boolean, value: String?) {
        val result = JSONObject().run {
            put(IS_SUCCESS, isSuccess)
            put(VALUE, value)
            toString()
        }
        BridgeUtils.runOnMain {
            jsPromptResult.confirm(result)
        }
    }
}