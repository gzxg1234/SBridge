package com.sanron.jsbridge.web

/**
 *
 * @author chenrong
 * @date 2023/4/6
 */
interface WebCallback {

    companion object{
        val ID_PREFIX = "CALLBACK_ID_"
    }

    /**
     *  回传值，并且下次可继续回传，适用于持续性监听，如定位，下载进度
     */
    fun onNext(value: String?)

    /**
     * 释放callback，无法再回传
     */
    fun onRelease()
}