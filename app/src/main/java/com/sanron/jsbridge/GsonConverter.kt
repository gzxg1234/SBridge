package com.sanron.jsbridge

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sanron.jsbridge.convert.ParameterConverter
import org.json.JSONObject

/**
 *
 * @author chenrong
 * @date 2023/4/11
 */
class GsonConverter(val gson: Gson = GsonBuilder().create()) : ParameterConverter<Any> {

    override fun convert(
        input: Any?,
        paramClazz: Class<*>,
        annotations: Array<Annotation>
    ): ParameterConverter.Result<Any>? {
        if (input == null || input == JSONObject.NULL) {
            return ParameterConverter.Result(null)
        }
        kotlin.runCatching {
            return ParameterConverter.Result(gson.fromJson(input.toString(), paramClazz))
        }
        return null
    }

    override fun handles(paramClazz: Class<*>, annotations: Array<Annotation>): Boolean {
        return true
    }
}