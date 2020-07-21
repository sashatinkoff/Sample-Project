package com.isidroid.b21.data

import io.realm.RealmModel
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class Product(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var price: Float = 0f,
    var picture: String? = null,
    var createdAt: Date = Date()
) : RealmModel, ISerialize {
    fun isValid(): Boolean = name.isNotEmpty() && price > 0f

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product
        return id == other.id
    }

    override fun hashCode() = id.hashCode()

}