package com.isidroid.b21.data

import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.isidroid.b21.Const

interface ISerialize {
    fun toJson(): String = gson.toJson(this)

    companion object {
        internal val gson: Gson = GsonBuilder().create()
    }
}

fun Intent?.product(): Product? {
    this ?: return null
    return with(getStringExtra(Const.Arg.PRODUCT.name)) {
        ISerialize.gson.fromJson(this, Product::class.java)
    }
}