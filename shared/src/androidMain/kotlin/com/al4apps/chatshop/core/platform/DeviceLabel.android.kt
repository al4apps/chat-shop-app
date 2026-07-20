package com.al4apps.chatshop.core.platform

import android.os.Build

actual fun deviceLabel(): String = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
