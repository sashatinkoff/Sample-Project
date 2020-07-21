package com.isidroid.b21.presentation.edit

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.isidroid.b21.data.Product
import com.isidroid.b21.domain.IPictureUseCase
import com.isidroid.b21.domain.IProductsUseCase
import com.isidroid.b21.utils.CoroutineViewModel
import timber.log.Timber
import javax.inject.Inject

class EditProductViewModel @Inject constructor(
    private val productUseCase: IProductsUseCase,
    private val pictureUseCase: IPictureUseCase
) : CoroutineViewModel() {
    sealed class State {
        data class Ready(val product: Product) : State()
        data class Error(val t: Throwable) : State()
        data class Saved(val product: Product) : State()
        data class Picture(val path: String?) : State()
        data class FormError(var name: Boolean = false, var price: Boolean = false) : State() {
            val passed
                get() = !name && !price
        }
    }


    private lateinit var product: Product

    @Volatile
    private var imagePath: String? = null
    val state = MutableLiveData<State>()

    fun create(productId: String?, product: Product?) = io(
        doWork = { productId?.let { productUseCase.find(it) } ?: product ?: Product() },
        onComplete = {
            this.product = it!!
            this.imagePath = it.picture
            state.value = State.Ready(it)
        }
    )

    fun image(imageUri: Uri?) = io(
        doWork = { imageUri?.let { pictureUseCase.parse(imageUri) } },
        onComplete = {
            imagePath = it
            state.value = State.Picture(it)
        }
    )

    fun save(name: String, price: Float) = io(
        doWork = {
            val formError = State.FormError(
                name = name.isEmpty(),
                price = price <= 0f
            )

            if (formError.passed)
                productUseCase.update(
                    product = product,
                    name = name,
                    price = price,
                    picture = imagePath
                )
            else {
                state.postValue(formError)
                null
            }
        },
        onComplete = { product -> product?.let { state.value = State.Saved(it) } },
        onError = { state.value = State.Error(it) }
    )

    fun isChanged(name: String, price: Float) =
        product.name != name || product.price != price || product.picture != imagePath

}