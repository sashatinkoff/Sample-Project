package com.isidroid.b21.ext

import android.view.View

fun View.enable(enabled: Boolean, alpha: Float = .6f) = apply {
    this.alpha = if (enabled) 1f else alpha
    isEnabled = enabled
}

fun View.visible(isVisible: Boolean, isInvisible: Boolean = false) {
    visibility = when {
        isVisible -> View.VISIBLE
        isInvisible -> View.INVISIBLE
        else -> View.GONE
    }
}
