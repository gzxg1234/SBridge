package com.sanron.jsbridge

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.sanron.jsbridge.app.databinding.ActivityMainBinding
import com.sanron.jsbridge.call.NativeCallback
import com.sanron.jsbridge.obj.CommonObj
import com.sanron.jsbridge.obj.DownloadObj
import com.sanron.jsbridge.obj.UIObj

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebView.setWebContentsDebuggingEnabled(true)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.webview.apply {
            settings.javaScriptEnabled = true
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            settings.useWideViewPort = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            settings.allowFileAccess
        }
        binding.webview.loadUrl("file:///android_asset/home.html")
        binding.btnRefresh.setOnClickListener {
            binding.webview.reload()
        }
        binding.webview.addNativeObj(
            "download",
            DownloadObj(binding.webview, applicationContext)
        )
        binding.webview.addNativeObj("common", CommonObj())
        binding.webview.addNativeObj("ui", UIObj(this))
        binding.webview.addConverter(GsonConverter())
        binding.webview.addConverter(DateConverter())
        binding.webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Logger.d("onPageStarted:" + url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Logger.d("onPageFinished:" + url)
                binding.webview.callWebMethod("addEventListener", json(
                    "event" to "click"
                ), object : NativeCallback {
                    override fun onNext(value: String?) {
                        Logger.d("addEvent callback,${value}")
                    }

                    override fun onCallError(errorMsg: String?) {
                    }
                })
            }
        }
    }
}