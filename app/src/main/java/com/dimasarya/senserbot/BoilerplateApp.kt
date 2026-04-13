package com.dimasarya.senserbot

import android.app.Application
import com.dimasarya.senserbot.core.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BoilerplateApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@BoilerplateApp)
            modules(appModules)
        }
    }
}
