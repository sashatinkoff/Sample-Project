package com.isidroid.b21.domain

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.isidroid.b21.data.Product
import io.realm.Realm
import io.realm.kotlin.deleteFromRealm
import java.lang.IllegalStateException
import javax.inject.Inject

class ProductsInteractor @Inject constructor(private val gson: Gson) : IProductsUseCase {
    override fun getList(page: Int, limit: Int): List<Product> {
        if (limit == 0) throw IllegalStateException("Limit should be more then a zero")

        return with(Realm.getDefaultInstance()) {
            refresh()
            val result = copyFromRealm(
                where(Product::class.java)
                    .sort("name")
                    .findAll()
            )
            close()

            val start = page * limit
            val end = with((page + 1) * limit) {
                when {
                    result.isEmpty() -> start
                    this < result.size -> result.size
                    else -> this
                }
            }

            result
            //.subList(start, end)
        }
    }

    override fun find(productId: String): Product? = with(Realm.getDefaultInstance()) {
        refresh()
        val result = where(Product::class.java)
            .equalTo("id", productId)
            .findFirst()
            ?.let { copyFromRealm(it) }
        close()
        result
    }

    override fun update(product: Product, name: String?, price: Float?): Product = update(
        product = product,
        name = name, price = price, picture = null
    )

    override fun update(product: Product, name: String?, price: Float?, picture: String?): Product =
        with(Realm.getDefaultInstance()) {
            refresh()
            name?.let { product.name = name }
            price?.let { product.price = price }
            product.picture = picture

            if (!product.isValid()) throw IllegalStateException("Invalid product")
            executeTransaction { insertOrUpdate(product) }
            close()

            product
        }

    override fun delete(product: Product) = with(Realm.getDefaultInstance()) {
        refresh()
        executeTransaction {
            where(Product::class.java)
                .equalTo("id", product.id)
                .findFirst()
                ?.deleteFromRealm()
        }
        close()
    }

    override fun exportData(): String = with(Realm.getDefaultInstance()) {
        refresh()
        val products = copyFromRealm(where(Product::class.java).findAll())
        val result = gson.toJson(products)
        close()
        result
    }

    override fun import(json: String?): List<Product> = with(Realm.getDefaultInstance())
    {
        refresh()
        var result: List<Product> = listOf()
        executeTransaction {
            deleteAll()
            result = gson.fromJson<List<Product>>(json, object : TypeToken<List<Product>>() {}.type)
            insertOrUpdate(result)
        }
        result
    }
}