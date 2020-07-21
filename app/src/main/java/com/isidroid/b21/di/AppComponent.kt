package com.isidroid.b21.di

import android.content.Context
import androidx.fragment.app.Fragment
import com.isidroid.b21.App
import com.isidroid.b21.presentation.edit.EditProductActivity
import com.isidroid.b21.presentation.main.MainActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton @Component(
    modules = [
        ViewModelModule::class,
        AppModule::class
    ]
)
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(activity: EditProductActivity)


    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}


fun Context.appComponent() = (applicationContext as App).appComponent
fun Fragment.appComponent() = requireActivity().appComponent()