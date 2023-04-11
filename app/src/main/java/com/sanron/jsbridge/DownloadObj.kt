package com.sanron.jsbridge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Base64
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.sanron.jsbridge.annotation.NativeMethod
import com.sanron.jsbridge.web.WebCallback
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

/**
 *
 * @author chenrong
 * @date 2023/4/7
 */
class DownloadObj(val webView: BridgeWebView, val context: Context) {

    @NativeMethod
    fun downloadImage(
        arg: JSONObject,
        onSuccess: WebCallback?,
        onFailure: WebCallback?
    ) {
        val width = arg.optInt("width")
        val height = arg.optInt("height")
        val url = arg.optString("url")
        thread {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .into(object : CustomTarget<Bitmap>(width, height) {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        thread {
                            val baos = ByteArrayOutputStream()
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            baos.flush()
                            val result = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                            onSuccess?.onNext(result)
                        }
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        onFailure?.onNext("图片加载失败")
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })
        }
    }


    @NativeMethod
    fun download(
        arg: JSONObject,
        onSuccess: WebCallback?,
        onFailure: WebCallback?,
        onProgress: WebCallback?
    ) {
        val url = arg.optString("url")
        thread {
            for (i in 0 until 100) {
                Thread.sleep(100)
                if (Math.random() < 0.05) {
                    onFailure?.onNext("下载失败")
                    return@thread
                }
                onProgress?.onNext(i.toString())
            }
            onSuccess?.onNext("file://sss.xml")
        }
    }

    @NativeMethod
    fun download2(
        arg: JSONObject,
        callback: WebCallback,
    ) {
        val url = arg.optString("url")
        thread {
            for (i in 0 until 100) {
                Thread.sleep(100)
                if (Math.random() < 0.05) {
                    callback.onNext(JSONObject().apply {
                        put("type", "onFailure")
                        put("error", "下载失败")
                    }.toString())
                    return@thread
                }
                callback.onNext(JSONObject().apply {
                    put("type", "onProgress")
                    put("progress", i)
                }.toString())
            }
            callback.onNext(JSONObject().apply {
                put("type", "onSuccess")
                put("src", "file://sdcard/download/1.img")
            }.toString())
        }
    }

}