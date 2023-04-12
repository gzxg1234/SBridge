package com.sanron.jsbridge.convert

import android.webkit.WebView
import com.sanron.jsbridge.web.WebCallback
import com.sanron.jsbridge.web.WebCallbackImpl
import org.json.JSONArray
import org.json.JSONObject

/**
 * 参数转换
 */
class ParametersHandler(val webView: WebView) {

    companion object {
        private val COMMON_PARAMETER_CONVERTERS = listOf<ParameterConverter<*>>(
            DEFAULT, BOOLEAN, BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, STRING
        )
    }

    private val parameterConverters = mutableListOf<ParameterConverter<*>>()

    private val handleCache = mutableMapOf<Class<*>, MutableList<ParameterConverter<*>>>()

    init {
        parameterConverters.addAll(COMMON_PARAMETER_CONVERTERS)
        parameterConverters.add(CallbackParameterConverter(webView))
        parameterConverters.add(ArrayConverter())
    }

    fun addConverter(parameterConverter: ParameterConverter<*>) {
        parameterConverters.add(parameterConverter)
        handleCache.clear()
    }

    fun handle(
        input: Any?,
        paramClazz: Class<*>,
        annotations: Array<Annotation>
    ): ParameterConverter.Result<*>? {
        var result: ParameterConverter.Result<*>?

        var cache = handleCache[paramClazz]
        if (cache == null) {
            cache = mutableListOf()
            handleCache[paramClazz] = cache
            for (converter in parameterConverters) {
                if (converter.handles(paramClazz, annotations)) {
                    cache.add(converter)
                }
            }
        }
        for (converter in cache) {
            result =
                converter.convert(input, paramClazz, annotations)
            if (result != null) {
                return result
            }
        }
        return null
    }

    object DEFAULT : ParameterConverter<Any> {
        override fun convert(
            input: Any?,
            paramClazz: Class<*>,
            annotations: Array<Annotation>
        ): ParameterConverter.Result<Any>? {
            if (input == null || input == JSONObject.NULL) {
                return ParameterConverter.Result(null)
            }
            if (paramClazz.isAssignableFrom(input::class.java)) {
                return ParameterConverter.Result(input)
            }
            return null
        }

        override fun handles(paramClazz: Class<*>, annotations: Array<Annotation>): Boolean {
            return true
        }

    }


    private abstract class NumberParameterConverter<O>(
        val outClass: Class<*>,
        val convertNumber: (number: Number) -> O
    ) : ParameterConverter<O> {
        override fun convert(
            input: Any?,
            paramClazz: Class<*>,
            annotations: Array<Annotation>
        ): ParameterConverter.Result<O>? {
            when (input) {
                is String -> {
                    runCatching {
                        return ParameterConverter.Result(
                            convertNumber(java.lang.Long.parseLong(input))
                        )
                    }
                    runCatching {
                        return ParameterConverter.Result(
                            convertNumber(java.lang.Double.parseDouble(input))
                        )
                    }
                    return null
                }
                is Number -> {
                    return ParameterConverter.Result(convertNumber(input))
                }
                null, JSONObject.NULL -> {
                    return ParameterConverter.Result(null)
                }
                else -> return null
            }
        }

        override fun handles(paramClazz: Class<*>, annotations: Array<Annotation>): Boolean {
            return outClass == paramClazz
                    || outClass == paramClazz.kotlin.javaPrimitiveType
        }

    }

    object STRING : ParameterConverter<String> {

        override fun convert(
            input: Any?,
            paramClazz: Class<*>,
            annotations: Array<Annotation>
        ): ParameterConverter.Result<String> {
            return ParameterConverter.Result(input?.toString())
        }

        override fun handles(paramClazz: Class<*>, annotations: Array<Annotation>): Boolean {
            return paramClazz.isAssignableFrom(String::class.java)
        }
    }

    object BOOLEAN : ParameterConverter<Boolean> {

        override fun convert(
            input: Any?,
            paramClazz: Class<*>,
            annotations: Array<Annotation>
        ): ParameterConverter.Result<Boolean>? {
            when (input) {
                is Boolean -> {
                    return ParameterConverter.Result(input)
                }
                is String -> {
                    kotlin.runCatching {
                        return ParameterConverter.Result(java.lang.Boolean.parseBoolean(input))
                    }
                }
                null, JSONObject.NULL -> {
                    return ParameterConverter.Result(null)
                }
            }
            return null
        }

        override fun handles(paramClazz: Class<*>, annotations: Array<Annotation>): Boolean {
            return paramClazz == Boolean::class.java
                    || paramClazz == Boolean::class.javaObjectType
        }
    }

    private object BYTE : NumberParameterConverter<Byte>(Byte::class.java, {
        it.toByte()
    })

    private object CHAR : NumberParameterConverter<Char>(Char::class.java, {
        it.toInt().toChar()
    })

    private object SHORT : NumberParameterConverter<Char>(Char::class.java, {
        it.toInt().toChar()
    })

    private object INT : NumberParameterConverter<Int>(Int::class.java, {
        it.toInt()
    })

    private object LONG : NumberParameterConverter<Long>(Long::class.java, {
        it.toLong()
    })

    private object FLOAT : NumberParameterConverter<Float>(Float::class.java, {
        it.toFloat()
    })

    private object DOUBLE : NumberParameterConverter<Double>(Double::class.java, {
        it.toDouble()
    })

    private inner class ArrayConverter : ParameterConverter<Any> {
        override fun convert(
            input: Any?,
            paramClazz: Class<*>,
            annotations: Array<Annotation>
        ): ParameterConverter.Result<Any>? {
            if (input is JSONArray) {
                val arr = paramClazz.cast(
                    java.lang.reflect.Array.newInstance(
                        paramClazz.componentType!!,
                        input.length()
                    )
                )!!
                for (i in 0 until input.length()) {
                    val r = this@ParametersHandler.handle(
                        input.get(i),
                        paramClazz.componentType!!,
                        emptyArray()
                    )
                    if (r != null) {
                        if (r.value == null && paramClazz.componentType.isPrimitive) {
                            //primitive array can't take null values
                            return null
                        }
                        java.lang.reflect.Array.set(arr, i, r.value)
                    } else {
                        return null
                    }
                }
                return ParameterConverter.Result(arr)
            }
            return null
        }

        override fun handles(paramClazz: Class<*>, annotations: Array<Annotation>): Boolean {
            return paramClazz.isArray
        }
    }

    private class CallbackParameterConverter(val webView: WebView) :
        ParameterConverter<WebCallback> {

        override fun convert(
            input: Any?,
            paramClazz: Class<*>,
            annotations: Array<Annotation>
        ): ParameterConverter.Result<WebCallback> {
            if (input == JSONObject.NULL
                || input !is String
                || !input.startsWith(WebCallback.ID_PREFIX)
            ) {
                return ParameterConverter.Result(null)
            }
            return ParameterConverter.Result(WebCallbackImpl(webView, input.toString()))
        }

        override fun handles(paramClazz: Class<*>, annotations: Array<Annotation>): Boolean {
            return paramClazz.isAssignableFrom(WebCallback::class.java)
        }
    }
}