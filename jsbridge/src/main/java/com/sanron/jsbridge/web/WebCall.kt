package com.sanron.jsbridge.web

import org.json.JSONArray
import org.json.JSONObject

/**
 *
 * @author chenrong
 * @date 2023/4/6
 */
abstract class WebCall(
    val obj: String,
    val method: String,
    val args: JSONArray
) {
    companion object {
        const val IS_SUCCESS = "isSuccess"
        const val VALUE = "value"
    }

    abstract fun sendResult(isSuccess: Boolean, value: String?)
}