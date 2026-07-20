package com.al4apps.chatshop.core.ui

import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.error_network
import chatshop.shared.generated.resources.error_unknown
import com.al4apps.chatshop.core.network.AppError

/** Ошибка → текст для пользователя. `detail` сервера показывается как есть (CLIENT_GUIDE §12). */
fun AppError.toUiText(): UiText = when (this) {
    is AppError.Api -> detail?.let { UiText(it) } ?: UiText(Res.string.error_unknown)
    AppError.Network -> UiText(Res.string.error_network)
    AppError.Unauthorized -> UiText(Res.string.error_unknown)
    AppError.Unknown -> UiText(Res.string.error_unknown)
}
