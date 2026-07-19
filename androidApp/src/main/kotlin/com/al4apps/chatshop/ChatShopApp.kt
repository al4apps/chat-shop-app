package com.al4apps.chatshop

import android.app.Application
import com.al4apps.chatshop.di.initKoin

class ChatShopApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
