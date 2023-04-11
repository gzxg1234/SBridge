package com.sanron.jsbridge.web

import android.webkit.WebView
import com.sanron.jsbridge.BridgeUtils.evalJs
import com.sanron.jsbridge.Logger
import com.sanron.jsbridge.json
import com.sanron.jsbridge.web.WebCallbackImpl.Companion.JS_CALLBACK_METHOD
import com.sanron.jsbridge.web.WebCallbackImpl.Companion.KEY_RELEASE
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

/**
 * 回收Web端调用Native生成的callback
 */
object CallbackRecycler {
    private val webCallbackRefQueue = ReferenceQueue<WebCallbackImpl>()
    private val webCallbackRefList = hashSetOf<WebCallbackRef>()
    private val webCallbackMap = mutableMapOf<WebView, MutableList<String>>()

    private fun releaseUnusedCallback() {
        webCallbackMap.clear()
        while (true) {
            (webCallbackRefQueue.poll() as? WebCallbackRef)?.also { ref ->
                webCallbackRefList.remove(ref)

                ref.webViewRef.get()?.let { webView ->
                    var idList = webCallbackMap[webView]
                    if (idList == null) {
                        idList = mutableListOf()
                        webCallbackMap[webView] = idList
                    }
                    idList.add(ref.callId)
                }
            } ?: break
        }

        webCallbackMap.forEach {
            val (webView, idList) = it
            val resultString = json(
                KEY_RELEASE to true
            ).toString()
            val jsBuilder = StringBuilder()
            for (id in idList) {
                jsBuilder.append("$JS_CALLBACK_METHOD(\"$id\",$resultString);")
            }
            Logger.d("WebCallbackImpl(${idList.joinToString(",")}) auto release")
            webView.evalJs(jsBuilder.toString())
        }
    }

    fun add(webCallbackImpl: WebCallbackImpl) {
        webCallbackRefList.add(
            WebCallbackRef(
                webCallbackImpl.webViewRef,
                webCallbackImpl,
                webCallbackRefQueue
            )
        )
        releaseUnusedCallback()
    }


    private class WebCallbackRef(
        val webViewRef: WeakReference<WebView>,
        webCallback: WebCallbackImpl,
        q: ReferenceQueue<WebCallbackImpl>
    ) :
        WeakReference<WebCallbackImpl>(webCallback, q) {

        val callId = webCallback.id
    }
}