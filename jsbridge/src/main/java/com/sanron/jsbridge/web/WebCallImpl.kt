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
    args: JSONArray,
    val webView: WebView,
    val jsPromptResult: JsPromptResult,
) : WebCall(obj, method, args) {


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