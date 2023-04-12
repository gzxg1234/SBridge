package com.sanron.jsbridge
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sanron.jsbridge.BridgeUtils.evalJs
import com.sanron.jsbridge.annotation.NativeMethod
import com.sanron.jsbridge.call.NativeCallback
import com.sanron.jsbridge.client.SWebChromeClient
import com.sanron.jsbridge.client.SWebViewClient
import com.sanron.jsbridge.convert.ParameterConverter
import com.sanron.jsbridge.convert.ParametersHandler
import com.sanron.jsbridge.web.WebCall
import org.json.JSONObject
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

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

    private var invokeAsyncExecutor: Executor =
        Executors.newCachedThreadPool(object : ThreadFactory {
            var threadNum = AtomicInteger(1)
            override fun newThread(r: Runnable?): Thread {
                return Thread(r, "jsbridge-execute-thread-${threadNum.getAndIncrement()}")
            }
        })

    private val nativeObjMap = mutableMapOf<String, MutableMap<String, MethodInvoke>>()

    /**
     * the callback passed when calling the web method from the client.
     */
    private var callbackIdIndex = 1L
    private val callbackMap = mutableMapOf<String, NativeCallback?>()

    private val parametersHandler = ParametersHandler(webView)


    init {
        addNativeObjInner(BASE_OBJ, BaseObj())
    }

    fun setAsyncExecutor(executor: Executor) {
        invokeAsyncExecutor = executor
    }

    fun addNativeObj(name: String, obj: Any) {
        if (name == BASE_OBJ) {
            Logger.d("the object name conflicts with the base object.")
            return
        }
        addNativeObjInner(name, obj)
    }

    fun addConverter(converter: ParameterConverter<*>) {
        parametersHandler.addConverter(converter)
    }

    private fun addNativeObjInner(name: String, obj: Any) {
        val handlers = mutableMapOf<String, MethodInvoke>()
        obj.javaClass.declaredMethods.filter {
            it.isAnnotationPresent(NativeMethod::class.java)
        }.forEach { method ->
            val isAsync = method.getAnnotation(NativeMethod::class.java)!!.async
            handlers[method.name] = MethodInvoke(obj, method, isAsync)
        }
        nativeObjMap[name] = handlers
    }

    fun removeNativeObj(name: String) {
        nativeObjMap.remove(name)
    }

    fun callWebMethod(method: String, args: JSONObject?, nativeCallback: NativeCallback) {
        val id = callbackIdIndex++ % Long.MAX_VALUE
        callbackMap[id.toString()] = nativeCallback
        webView.evalJs("$CALL_FROM_NATIVE('$method',${args ?: "null"},'$id')")
    }

    internal fun clearNativeCallback() {
        callbackMap.clear()
    }

    fun setLoggerEnabled(enable: Boolean) {
        Logger.enable = enable
    }

    internal fun handleCall(webCall: WebCall) {
        val methodMap = nativeObjMap[webCall.obj]
        if (methodMap == null) {
            Logger.w("${webCall.obj} is not registered in native")
            webCall.sendResult(false, "${webCall.obj} is not registered in native")
            return
        }

        val methodInvoke = methodMap[webCall.method]
        if (methodInvoke == null) {
            Logger.w("method ${webCall.method} is not defined in ${webCall.obj}")
            webCall.sendResult(
                false,
                "method ${webCall.method} is not defined in ${webCall.obj}"
            )
            return
        }

        invokeAsyncExecutor.execute {
            methodInvoke(webCall)
        }
    }

    fun getWebChromeClientProxy(): WebChromeClient {
        return sWebChromeClient
    }

    fun getWebViewClientProxy(): WebViewClient {
        return sWebViewClient
    }

    fun setWebViewClient(client: WebViewClient?) {
        sWebViewClient.real = client
    }

    fun setWebChromeClient(client: WebChromeClient?) {
        sWebChromeClient.webChromeClient = client
    }

    private inner class MethodInvoke(
        val obj: Any,
        val method: java.lang.reflect.Method,
        val isAsync: Boolean
    ) {

        operator fun invoke(webCall: WebCall) {
            val argList = mutableListOf<Any?>()
            if (webCall.args.length() != method.parameterTypes.size) {
                Logger.w("incorrect parameter length invoke,arguments=${webCall.args},method=${method}")
                webCall.sendResult(
                    false,
                    "incorrect parameter length invoke,arguments=${webCall.args},method=${method}"
                )
            } else {
                for (i in 0 until webCall.args.length()) {
                    val paramClass = method.parameterTypes[i]
                    val paramAnnotations = method.parameterAnnotations[i]
                    val arg = webCall.args.opt(i)
                    val paramResult = parametersHandler.handle(
                        arg,
                        paramClass,
                        paramAnnotations
                    )
                    if (paramResult == null) {
                        Logger.w(
                            "convert argument(value=$arg,type=${arg.javaClass.name}) " +
                                    "to parameter(type=$paramClass) failure"
                        )
                        webCall.sendResult(
                            false, "convert argument(value=$arg,type=${arg.javaClass.name}) " +
                                    "to parameter(type=$paramClass) failure"
                        )
                        return
                    }
                    argList.add(paramResult.value)
                }
                val invoke = Runnable {
                    kotlin.runCatching {
                        val result = method.invoke(obj, *argList.toTypedArray())
                        webCall.sendResult(true, result?.toString())
                    }.onFailure {
                        Logger.w(
                            "A Java exception was thrown when invoke the method \"${webCall.method}\"",
                            it
                        )
                        webCall.sendResult(
                            false,
                            "A Java exception was thrown when invoke the method \"${webCall.method}\""
                        )
                    }
                }

                if (isAsync) {
                    invoke.run()
                } else {
                    BridgeUtils.runOnMain(invoke)
                }
            }
        }
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