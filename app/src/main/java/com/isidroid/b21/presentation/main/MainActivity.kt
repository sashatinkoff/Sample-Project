package com.isidroid.b21.presentation.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.isidroid.b21.Const
import com.isidroid.b21.R
import com.isidroid.b21.data.Product
import com.isidroid.b21.data.product
import com.isidroid.b21.di.appComponent
import com.isidroid.b21.ext.alert
import com.isidroid.b21.ext.observe
import com.isidroid.b21.ext.permission
import com.isidroid.b21.presentation.edit.EditProductActivity
import com.isidroid.b21.utils.BindActivity
import com.isidroid.b21.utils.views.YRecyclerScrollListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*

class MainActivity : BindActivity(layoutRes = R.layout.activity_main) {
    private val viewModel by viewModels<MainViewModel> { viewModelFactory }
    private val adapter = ProductsAdapter(
        onEdit = { EditProductActivity.open(caller = this, product = it) },
        onDelete = { requestDelete(it) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent().inject(this)
        super.onCreate(savedInstanceState)

        createToolbar()
        createAdapters()
        buttonCreate.setOnClickListener { EditProductActivity.open(caller = this) }
        viewModel.loadList(isClear = true)
    }

    override fun onCreateViewModel() {
        observe(viewModel.state) { onState(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Const.Code.EDIT_PRODUCT.code -> onProductSaved(data?.product())
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Views init
    private fun createToolbar() = toolbar.apply {
        menuInflater.inflate(R.menu.main, menu)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_export -> exportData()
                R.id.action_import -> importData()
            }
            true
        }
    }


    private fun createAdapters() {
        recyclerView.addOnScrollListener(YRecyclerScrollListener(buttonCreate))
        recyclerView.adapter = adapter
        swipeRefreshLayout.setOnRefreshListener { viewModel.loadList(isClear = true) }
    }

    private fun onProductSaved(product: Product?) {
        product ?: return
        adapter.insertOrUpdate(product)
    }

    private fun requestDelete(product: Product) = alert(
        titleRes = R.string.confirm_delete_product,
        message = String.format(getString(R.string.delete_product_message), product.name),
        negativeRes = android.R.string.cancel,
        positiveRes = android.R.string.ok,
        onPositive = { viewModel.delete(product) }
    )

    // Live data
    private fun onState(state: MainViewModel.State?) {
        when (state) {
            is MainViewModel.State.ProductList -> onProductList(
                list = state.list,
                isClear = state.isClear
            )
            is MainViewModel.State.Error -> onError(state.t)
            is MainViewModel.State.Delete -> onDelete(state.product)
            is MainViewModel.State.Exported -> onExported()
            is MainViewModel.State.Restored -> onRestored(state.list)
        }
    }

    private fun onError(t: Throwable) {
        swipeRefreshLayout.isRefreshing = false
        Toast.makeText(this, t.message, Toast.LENGTH_LONG).show()
    }

    private fun onProductList(list: List<Product>, isClear: Boolean) {
        swipeRefreshLayout.isRefreshing = false
        if (isClear) adapter.clear()
        adapter.insert(list)
    }

    private fun onDelete(product: Product) {
        adapter.remove(product)
    }

    private fun onExported() {
        Toast.makeText(this, R.string.export_completed, Toast.LENGTH_LONG).show()
    }

    private fun onRestored(list: List<Product>) {
        Toast.makeText(this, R.string.import_completed, Toast.LENGTH_LONG).show()
        onProductList(list = list, isClear = true)
    }

    private fun exportData() {
        permission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            onGranted = {
                registerForActivityResult(ActivityResultContracts.CreateDocument()) {
                    it ?: return@registerForActivityResult
                    viewModel.exportData(it)
                }.launch("data.json")
            })
    }

    private fun importData() {
        permission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            onGranted = {
                registerForActivityResult(ActivityResultContracts.GetContent()) {
                    it ?: return@registerForActivityResult
                    viewModel.importData(it)
                }.launch("*/*")
            })
    }

}