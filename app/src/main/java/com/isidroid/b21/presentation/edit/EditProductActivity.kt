package com.isidroid.b21.presentation.edit

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.isidroid.b21.Const
import com.isidroid.b21.GlideApp
import com.isidroid.b21.R
import com.isidroid.b21.data.Product
import com.isidroid.b21.data.product
import com.isidroid.b21.di.appComponent
import com.isidroid.b21.ext.alert
import com.isidroid.b21.ext.enable
import com.isidroid.b21.ext.observe
import com.isidroid.b21.ext.permission
import com.isidroid.b21.presentation.Router
import com.isidroid.b21.utils.BindActivity
import kotlinx.android.synthetic.main.activity_edit_product.*
import kotlinx.android.synthetic.main.toolbar.*

class EditProductActivity : BindActivity(layoutRes = R.layout.activity_edit_product) {
    private val viewModel by viewModels<EditProductViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent().inject(this)
        super.onCreate(savedInstanceState)

        createToolbar()
        createForm()

        viewModel.create(
            productId = intent.getStringExtra(Const.Arg.PRODUCT_ID.name),
            product = intent.product()
        )
    }

    override fun onCreateViewModel() {
        observe(viewModel.state) { onState(it) }
    }

    override fun onBackPressed() {
        closeForm()
    }

    // View initialization
    private fun createToolbar() = toolbar.apply {
        setNavigationOnClickListener { closeForm() }
    }

    private fun createForm() {
        buttonPickImage.setOnClickListener {
            permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                onGranted = {
                    registerForActivityResult(ActivityResultContracts.GetContent()) {
                        it ?: return@registerForActivityResult
                        viewModel.image(it)
                    }.launch("image/*")
                })
        }

        buttonRemoveImage.setOnClickListener { viewModel.image(null) }
        buttonSubmit.setOnClickListener {
            buttonSubmit.enable(false)
            viewModel.save(
                name = nameInput.text.toString(),
                price = priceInput.text.toString().toFloatOrNull() ?: 0f
            )
        }
    }

    private fun closeForm() {
        if (viewModel.isChanged(
                name = nameInput.text.toString(),
                price = priceInput.text.toString().toFloatOrNull() ?: 0f
            )
        ) alert(
            titleRes = R.string.confirm_close_edit_form,
            messageRes = R.string.close_edit_form_message,
            negativeRes = android.R.string.cancel,
            positiveRes = R.string.close_form_and_save,
            neutralRes = R.string.close_without_saving,
            onNeutral = { finishAfterTransition() },
            onPositive = {
                viewModel.save(
                    name = nameInput.text.toString(),
                    price = priceInput.text.toString().toFloatOrNull() ?: 0f
                )
            }
        )
        else
            finishAfterTransition()
    }

    private fun onState(state: EditProductViewModel.State?) {
        when (state) {
            is EditProductViewModel.State.Ready -> onReady(state.product)
            is EditProductViewModel.State.Error -> onError(state.t)
            is EditProductViewModel.State.Saved -> onSave(state.product)
            is EditProductViewModel.State.FormError -> onFormError(state)
            is EditProductViewModel.State.Picture -> onPicture(state.path)
        }
    }

    private fun onPicture(path: String?) {
        viewFlipper.displayedChild = if (path.isNullOrEmpty()) 0 else 1
        GlideApp.with(this).load(path).into(imageView)
    }

    private fun onReady(product: Product) {
        nameInput.setText(product.name)
        priceInput.setText(product.price.toString())
        product.picture?.let {
            GlideApp.with(this).load(it).into(imageView)
            viewFlipper.displayedChild = 1
        }
        toolbar.title = if (product.name.isEmpty()) getString(R.string.label_new_product)
        else {
            toolbar.subtitle = product.name
            getString(R.string.label_edit_product)
        }
    }

    private fun onError(t: Throwable) {
        Toast.makeText(this, t.message, Toast.LENGTH_LONG).show()
        buttonSubmit.enable(true)
    }

    private fun onSave(product: Product) {
        setResult(RESULT_OK, Intent().putExtra(Const.Arg.PRODUCT.name, product.toJson()))
        finishAfterTransition()
    }

    private fun onFormError(errors: EditProductViewModel.State.FormError) {
        if (errors.name) nameInputLayout.error = getString(R.string.error_invalid_product_name)
        if (errors.price) priceInputLayout.error = getString(R.string.error_invalid_product_price)

    }

    companion object {
        fun open(
            caller: Any,
            productId: String? = null,
            product: Product? = null
        ) = Router(
            caller = caller,
            activity = EditProductActivity::class.java,
            requestCode = Const.Code.EDIT_PRODUCT
        )
            .putExtra(Const.Arg.PRODUCT_ID, productId)
            .putExtra(Const.Arg.PRODUCT, product?.toJson())
            .open()
    }
}