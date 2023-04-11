package com.sanron.jsbridge

import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sanron.jsbridge.BridgeUtils.evalJs
import com.sanron.jsbridge.annotation.NativeMethod
import com.sanron.jsbridge.call.NativeCallback
import com.sanron.jsbridge.client.SWebChromeClient
import com.sanron.jsbridge.client.SWebViewClient
import com.sanron.jsbridge.web.WebCall
import org.json.JSONObject
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

typealias MethodHandler = ((WebCall) -> Unit)

/**
 *
 * @author chenrong
 * @date 2023/4/9
 */
class SBridge(val webView: WebView) {
    companion object {
        const val CALL_FROM_NATIVE = "window.___callFromNative"

        const val BASE_OBJ = "__Native"

        const val CALL_WEB_RESULT_ID = "id"
        const val CALL_WEB_RESULT_ERROR = "error"
        const val CALL_WEB_RESULT_VALUE = "value"
        const val CALL_WEB_RESULT_ALIVE = "keepAlive"
    }

    private val sWebViewClient: SWebViewClient = SWebViewClient(this)
    private val sWebChromeClient: SWebChromeClient = SWebChromeClient(this)

    private var callNativeExecutor: Executor =
        ThreadPoolExecutor(
            2, 2, 10, TimeUnit.SECONDS, LinkedBlockingQueue(), object : ThreadFactory {
                var threadNum = AtomicInteger(1)
                override fun newThread(r: Runnable?): Thread {
                    return Thread(r, "jsbridge-execute-thread-${threadNum.getAndIncrement()}")
                }
            })

    private val nativeObjMap = mutableMapOf<String, MutableMap<String, MethodHandler>>()

    /**
     * the callback passed when calling the web method from the client.
     */
    private var callbackIdIndex = 1L
    private val callbackMap = mutableMapOf<String, NativeCallback?>()

    init {
        webView.webChromeClient = sWebChromeClient
        webView.webViewClient = sWebViewClient
        addNativeObj(BASE_OBJ, BaseObj())
    }

    fun setWebCallExecutor(executor: Executor) {
        callNativeExecutor = executor
    }

    fun addNativeObj(name: String, obj: Any) {
        if (name == BASE_OBJ) {
            Log.w("SBridge", "the object name conflicts with the base object.")
            return
        }

        val handlers = mutableMapOf<String, MethodHandler>()
        obj.javaClass.declaredMethods.filter {
            it.isAnnotationPresent(NativeMethod::class.java)
        }.forEach { method ->
            val isAsync = method.getAnnotation(NativeMethod::class.java)!!.async
            handlers[method.name] = { call ->
                runCatching {
                    val argList = mutableListOf<Any?>()
                    call.args?.let {
                        argList.add(it)
                    }
                    argList.addAll(call.getCallbacks())
                    if (argList.size != method.parameterTypes.size) {
                        Logger.w("incorrect parameter length passed to the ${call.method} method")
                        call.sendResult(false, "incorrect parameter length passed to the ${call.method} method")
                    } else {
                        val invoke = Runnable {
                            val result = method.invoke(obj, *argList.toTypedArray())
                            call.sendResult(true, result?.toString())
                        }
                        if (isAsync) {
                            callNativeExecutor.execute(invoke)
                        } else {
                            invoke.run()
                        }
                    }
                }.onFailure {
                    Logger.w(
                        "A Java exception was thrown when invoke the method \"${call.method}\"",
                        it
                    )
                    call.sendResult(false, "A Java exception was thrown when invoke the method \"${call.method}\"")
                }
            }
        }

        nativeObjMap[name] = handlers
    }

    fun removeNativeObj(name: String) {
        nativeObjMap.remove(name)
    }

    fun callWeb(method: String, args: JSONObject?, nativeCallback: NativeCallback) {
        val id = callbackIdIndex++ % Long.MAX_VALUE
        callbackMap[id.toString()] = nativeCallback
        webView.evalJs("$CALL_FROM_NATIVE('$method',${args ?: "null"},'$id')")
    }

    fun setWebViewClient(client: WebViewClient) {
        sWebViewClient.webViewClient = client
    }

    fun setWebChromeClient(client: WebChromeClient?) {
        sWebChromeClient.webChromeClient = client
    }

    internal fun clearNativeCallback() {
        callbackMap.clear()
    }

    internal fun handleCall(webCall: WebCall) {

        val handlers = nativeObjMap[webCall.obj]
        if (handlers == null) {
            Logger.w("${webCall.obj} is not registered in native")
            webCall.sendResult(false, "${webCall.obj} is not registered in native")
            return
        }

        val methodHandler = handlers[webCall.method]
        if (methodHandler == null) {
            Logger.w("method ${webCall.method} is not defined in ${webCall.obj}")
            webCall.sendResult(
                false,
                "method ${webCall.method} is not defined in ${webCall.obj}"
            )
            return
        }

        methodHandler.invoke(webCall)
    }

    private inner class BaseObj : Any() {

        /**
         * callback from the web client to the native side.
         */
        @NativeMethod
        fun callbackFromWeb(args: JSONObject) {
            Logger.d("callbackFromWeb -> $args")
            BridgeUtils.runOnMain {
                val id = args.getString(CALL_WEB_RESULT_ID)
                callbackMap[id]?.let { callback ->
                    if (args.has(CALL_WEB_RESULT_ERROR)) {
                        callback.onCallError(args.optString(CALL_WEB_RESULT_ERROR))
                        callbackMap.remove(id)
                        return@runOnMain
                    }

                    val value = args.getString(CALL_WEB_RESULT_VALUE)
                    val keepAlive = args.optBoolean(CALL_WEB_RESULT_ALIVE, false)
                    if (!keepAlive) {
                        callbackMap.remove(id)
                    }
                    callback.onNext(value)
                }
            }
        }

    }
}