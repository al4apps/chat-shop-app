package com.al4apps.chatshop.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Текст для UI: сырое значение или строковый ресурс.
 * Резолвится в строку только в Compose-слое через [resolve].
 */
@Immutable
sealed interface UiText {

    data class Plain(val value: String) : UiText

    data class Res(
        val resource: StringResource,
        val args: List<Any> = emptyList(),
    ) : UiText
}

@Composable
fun UiText.resolve(): String = when (this) {
    is UiText.Plain -> value
    is UiText.Res -> if (args.isEmpty()) {
        stringResource(resource)
    } else {
        stringResource(resource, *args.toTypedArray())
    }
}
