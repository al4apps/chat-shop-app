package com.al4apps.chatshop

import android.app.Application
import com.al4apps.chatshop.di.initKoin
import org.koin.android.ext.koin.androidContext

class ChatShopApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@ChatShopApp)
        }
    }
}
