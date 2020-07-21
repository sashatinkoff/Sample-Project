package com.isidroid.b21.domain

import android.content.Context
import android.net.Uri

interface IPictureUseCase {
    fun parse(uri: Uri): String
}