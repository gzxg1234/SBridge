package com.sanron.jsbridge

import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.google.gson.GsonBuilder
import com.google.gson.internal.GsonBuildConfig

/**
 *
 * @author chenrong
 * @date 2023/4/7
 */
val gson by lazy {
    GsonBuilder().create()
}

fun Any.toJson(): String? {
    return gson.toJson(this)
}