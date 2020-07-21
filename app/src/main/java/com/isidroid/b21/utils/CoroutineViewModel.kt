package com.isidroid.b21.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

abstract class CoroutineViewModel(
    private val jobDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(), CoroutineScope, Cancellation {

    private var job = Job()
    private var dispatcher = Dispatchers.Main + job

    override val coroutineContext: CoroutineContext
        get() {
            if (job.isCancelled) {
                job = Job()
                dispatcher = Dispatchers.Main + job
            }
            return dispatcher
        }

    override fun cancel() {
        job.cancel()
    }

    protected fun <T> io(
        doWork: () -> T,
        doBefore: (() -> Unit)? = null,
        onComplete: ((T?) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    ) = viewModelScope.launch(dispatcher) {
        try {
            doBefore?.invoke()
            val result = withContext(jobDispatcher) { doWork() }
            onComplete?.invoke(result)

        } catch (e: Throwable) {
            Timber.e(e)
            if (!isActive) return@launch
            onError?.invoke(e)
        }
    }
}

interface Cancellation {
    fun cancel()
}