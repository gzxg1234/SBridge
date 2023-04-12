package com.sanron.jsbridge

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sanron.jsbridge.convert.ParameterConverter
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * @author chenrong
 * @date 2023/4/11
 */
class DateConverter(val gson: Gson = GsonBuilder().create()) : ParameterConverter<Any> {

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class DateFormatter(
        val pattern: String = ""
    )

    override fun convert(
        input: Any?,
        paramClazz: Class<*>,
        annotations: Array<Annotation>
    ): ParameterConverter.Result<Any>? {
        if (input == null || input == JSONObject.NULL) {
            return ParameterConverter.Result(null)
        }
        kotlin.runCatching {
            if (input is Number) {
                val time = input.toLong()
                return ParameterConverter.Result(Date(time))
            }
            if (input is String) {
                annotations.find {
                    it is DateFormatter
                }?.let {
                    val format = (it as DateFormatter).pattern
                    if (format.isNotEmpty()) {
                        runCatching {
                            return ParameterConverter.Result(
                                SimpleDateFormat(
                                    format,
                                    Locale.getDefault()
                                ).parse(input)
                            )
                        }
                    }
                }
                return ParameterConverter.Result(gson.fromJson(input.toString(), paramClazz))
            }
        }
        return null
    }

    override fun handles(paramClazz: Class<*>, annotations: Array<Annotation>): Boolean {
        return true
    }
}
