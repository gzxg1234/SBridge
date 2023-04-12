package com.sanron.jsbridge.obj

import android.content.Context
import com.sanron.jsbridge.BridgeWebView
import com.sanron.jsbridge.annotation.NativeMethod
import com.sanron.jsbridge.web.WebCallback
import org.json.JSONObject
import kotlin.concurrent.thread

/**
 *
 * @author chenrong
 * @date 2023/4/7
 */
class DownloadObj(val webView: BridgeWebView, val context: Context) {

    @NativeMethod
    fun download(
        arg: JSONObject,
        onSuccess: WebCallback?,
        onFailure: WebCallback?,
        onProgress: WebCallback?
    ) {
        val url = arg.optString("url")
        thread {
            for (i in 0 .. 100) {
                Thread.sleep(100)
                if (Math.random() < 0.01) {
                    onFailure?.onNext("下载失败")
                    return@thread
                }
                onProgress?.onNext(i.toString())
            }
            onSuccess?.onNext("file://sss.xml")
        }
    }


}