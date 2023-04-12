package com.sanron.jsbridge.obj

import com.sanron.jsbridge.DateConverter
import com.sanron.jsbridge.Logger
import com.sanron.jsbridge.UserInfo
import com.sanron.jsbridge.annotation.NativeMethod
import com.sanron.jsbridge.toJson
import com.sanron.jsbridge.web.WebCallback
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

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

    @NativeMethod
    fun minus(a: Int, b: Int): Int {
        return a - b
    }

    data class Man(
        val name: String?,
        val age: Int?
    ) {
        constructor() : this(null, null)
    }

    @NativeMethod
    fun testAny(
        @DateConverter.DateFormatter("yyyy-MM-dd")
        date1: Date,
        date2: Date,
        a: Int, b: Int?, c: String?, d: String, e: Double, f: Double?, g: WebCallback,
        h: WebCallback?, i: Boolean?, xx: Boolean, j: WebCallback, k: Array<Int>, l: IntArray,
        m: Man, n: Array<String>?
    ) {
        Logger.d("date = ${SimpleDateFormat("yyyy-MM-dd").format(date1)}")
        Logger.d("date2 = ${SimpleDateFormat("yyyy-MM-dd").format(date2)}")
        h?.onNext("hello,h")
        g.onNext("hello,g")
        j.onNext("hello,j")
        Logger.d("test any invoke success")
    }
}