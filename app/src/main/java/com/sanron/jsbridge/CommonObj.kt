package com.sanron.jsbridge

import android.os.SystemClock
import com.sanron.jsbridge.annotation.NativeMethod
import com.sanron.jsbridge.web.WebCallback
import org.json.JSONObject
import kotlin.concurrent.thread

/**
 *
 * @author chenrong
 * @date 2023/4/7
 */
class CommonObj {

    @NativeMethod
    fun getUserInfo(): String {
        return UserInfo(1, "jack", 18, "man").toJson() ?: ""
    }

    @NativeMethod
    fun add(args: JSONObject): Int {
        return args.optInt("a") + args.optInt("b")
    }
}