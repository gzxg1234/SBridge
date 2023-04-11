package com.sanron.jsbridge

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import java.io.InputStream

/**
 *
 * @author chenrong
 * @date 2023/4/6
 */
object BridgeUtils {

    val SCHEME = "jsbridge"

    /**
     * 调用native方法格式jsbridge://call_native?call={call}
     */
    val METHOD_WEB_CALL = "call_native"
    val QUERY_CALL = "call"

    /**
     * web端使用APP本地jsbridge.js的url
     */
    const val LOCAL_JS_FILE_REQUEST_URL = "native://jsbridge.js"

    private const val JS_FILE = "jsbridge.js"

    private val MAIN_HANDLER = Handler(Looper.getMainLooper())


    fun jsFileStream(context: Context): InputStream {
        return context.assets.open(JS_FILE)
    }

    fun runOnMain(runnable: Runnable) {
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            runnable.run()
        } else {
            MAIN_HANDLER.post(runnable)
        }
    }

    fun WebView.evalJs(script: String) {
        if (Build.VERSION.SDK_INT >= 19) {
            evaluateJavascript(
                script, null
            )
        } else {
            loadUrl("javascript:$script")
        }
    }

}