package com.sanron.jsbridge.call


/**
 *
 * @author chenrong
 * @date 2023/4/9
 */
interface NativeCallback {

    fun onNext(value: String?)

    /**
     * error occurred while calling web method.
     * The web method does not exist or there is an error within the web method.
     */
    fun onCallError(errorMsg: String?)
}