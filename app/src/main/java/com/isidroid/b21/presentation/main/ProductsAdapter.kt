package com.isidroid.b21.presentation.main

import android.net.Uri
import androidx.databinding.ViewDataBinding
import com.isidroid.b21.GlideApp
import com.isidroid.b21.R
import com.isidroid.b21.data.Product
import com.isidroid.b21.databinding.ItemProductBinding
import com.isidroid.b21.ext.visible
import com.isidroid.b21.utils.views.adapters.CoreBindAdapter
import kotlinx.android.synthetic.main.activity_edit_product.*
import timber.log.Timber
import java.text.NumberFormat

class ProductsAdapter(
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : CoreBindAdapter<Product>() {
    override var hasEmpty = true
    override val emptyResource = R.layout.item_empty_products

    override fun resource(viewType: Int) = R.layout.item_product
    override fun onBindHolder(binding: ViewDataBinding, position: Int) {
        (binding as? ItemProductBinding)?.apply {
            val product = items[position]

            imageView.visible(!product.picture.isNullOrEmpty())
            product.picture?.let { GlideApp.with(imageView).load(it).into(imageView) }
            titleView.text = product.name
            priceView.text = NumberFormat.getCurrencyInstance().format(product.price)

            buttonEdit.setOnClickListener { onEdit(product) }
            buttonDelete.setOnClickListener { onDelete(product) }
        }
    }
}