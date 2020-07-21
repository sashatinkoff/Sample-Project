package com.isidroid.b21.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.isidroid.b21.domain.IPictureUseCase
import com.isidroid.b21.domain.IProductsUseCase
import com.isidroid.b21.domain.PictureInteractor
import com.isidroid.b21.domain.ProductsInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {
    @JvmStatic @Provides @Singleton
    fun provideProductUseCase(gson: Gson): IProductsUseCase = ProductsInteractor(gson)

    @JvmStatic @Provides @Singleton
    fun providePictureUseCase(context: Context): IPictureUseCase = PictureInteractor(context)

    @JvmStatic @Singleton @Provides
    fun provideGson(): Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .setLenient()
        .create()
}