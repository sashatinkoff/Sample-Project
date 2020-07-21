package com.isidroid.b21.presentation.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.isidroid.b21.data.Product
import com.isidroid.b21.domain.IProductsUseCase
import com.isidroid.b21.utils.CoroutineViewModel
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val context: Context,
    private val productsUseCase: IProductsUseCase
) : CoroutineViewModel() {
    sealed class State {
        data class ProductList(val list: List<Product>, val isClear: Boolean) : State()
        data class Error(val t: Throwable) : State()
        data class Delete(val product: Product) : State()
        object Exported : State()
        data class Restored(val list: List<Product>) : State()
    }

    private var page: Int = 0
    val state = MutableLiveData<State>()

    fun loadList(isClear: Boolean) = io(
        doWork = { productsUseCase.getList(page = page, limit = 10) },
        onComplete = { state.value = State.ProductList(list = it!!, isClear = isClear) },
        onError = { state.value = State.Error(it) }
    )

    fun delete(product: Product) = io(
        doWork = { productsUseCase.delete(product) },
        onComplete = { state.value = State.Delete(product) }
    )

    fun exportData(uri: Uri) = io(
        doWork = {
            context.contentResolver.openOutputStream(uri)
                ?.bufferedWriter()
                .use { it?.write(productsUseCase.exportData()) }
        },
        onComplete = { state.value = State.Exported },
        onError = { state.value = State.Error(it) }
    )

    fun importData(uri: Uri) = io(
        doWork = {
            val json = context.contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                .use { it?.readText() }

            productsUseCase.import(json)
        },
        onComplete = { state.value = State.Restored(it!!) },
        onError = { state.value = State.Error(it) }
    )
}