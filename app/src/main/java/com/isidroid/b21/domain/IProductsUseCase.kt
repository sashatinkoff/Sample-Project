package com.isidroid.b21.domain

import com.isidroid.b21.data.Product

interface IProductsUseCase {
    fun getList(page: Int, limit: Int): List<Product>
    fun find(productId: String): Product?
    fun update(product: Product, name: String?, price: Float?): Product
    fun update(product: Product, name: String?, price: Float?, picture: String?): Product
    fun delete(product: Product)
    fun exportData(): String
    fun import(json: String?): List<Product>
}