package com.al4apps.chatshop.core.platform

import platform.UIKit.UIDevice

actual fun deviceLabel(): String = with(UIDevice.currentDevice) {
    "$model ($systemName $systemVersion)"
}
