package com.al4apps.chatshop

import android.app.Application
import com.al4apps.chatshop.di.AppConfig
import com.al4apps.chatshop.di.DevConfig
import com.al4apps.chatshop.di.initKoin
import org.koin.android.ext.koin.androidContext

class ChatShopApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin(
            appConfig = AppConfig(
                testMode = DevConfig.TEST_MODE,
                apiBaseUrl = BuildConfig.API_BASE_URL,
            ),
        ) {
            androidContext(this@ChatShopApp)
        }
    }
}
