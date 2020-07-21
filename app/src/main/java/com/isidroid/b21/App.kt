package com.isidroid.b21

import android.app.Application
import com.isidroid.b21.di.DaggerAppComponent
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber

class App : Application() {
    internal val appComponent by lazy { DaggerAppComponent.factory().create(this) }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        createRealm()
    }

    private fun createRealm() {
        Realm.init(this)
        val builder = RealmConfiguration.Builder()
            .name("data.realm")
            .schemaVersion(1L)
            .deleteRealmIfMigrationNeeded()

        Realm.setDefaultConfiguration(builder.build())
    }
}

