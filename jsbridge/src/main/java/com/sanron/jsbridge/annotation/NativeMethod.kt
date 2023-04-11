package com.sanron.jsbridge.annotation

/**
 *
 * @author chenrong
 * @date 2023/4/8
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NativeMethod(
    val async: Boolean = true
)
