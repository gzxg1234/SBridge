package com.sanron.jsbridge.convert

/**
 *
 * @author chenrong
 * @date 2023/4/11
 */
interface ParameterConverter<O> {


    fun convert(
        input: Any?,
        paramClazz: Class<*>,
        annotations: Array<Annotation>
    ): Result<O>?

    fun handles(paramClazz: Class<*>, annotations: Array<Annotation>): Boolean

    class Result<O>(
        val value: O? = null
    )
}