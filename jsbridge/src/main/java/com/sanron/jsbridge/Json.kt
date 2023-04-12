package com.sanron.jsbridge

import org.json.JSONObject

/**
 *
 * @author chenrong
 * @date 2023/4/9
 */


fun json(vararg pairs: Pair<String, Any?>): JSONObject {
    return JSONObject().apply {
        pairs.forEach {
            put(it.first, it.second)
        }
    }
}


operator fun JSONObject.set(key: String, value: Any?): Any? {
    return this.putOpt(key, value)
}

