package com.sanron.jsbridge

import android.util.Log

/**
 *
 * @author chenrong
 * @date 2023/4/8
 */

object Logger {
    val TAG = "SBridge"

    internal var enable = true


    fun d(msg: String, throwable: Throwable? = null) {
        if (enable) {
            Log.d(TAG, msg, throwable)
        }
    }

    fun w(msg: String, throwable: Throwable? = null) {
        if (enable) {
            Log.w(TAG, msg, throwable)
        }
    }

}