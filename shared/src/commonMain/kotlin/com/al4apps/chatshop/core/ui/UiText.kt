package com.al4apps.chatshop.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.allStringResources
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Текст для UI: сырое значение или строковый ресурс (по стабильному ключу,
 * поэтому сериализуем и пригоден для uiState/vmState-моделей).
 * Резолвится в строку только в Compose-слое через [resolve].
 */
@Serializable
@Immutable
sealed interface UiText {

    @Serializable
    data class Plain(val value: String) : UiText

    @Serializable
    data class Res(
        val key: String,
        val args: List<String> = emptyList(),
    ) : UiText
}

/** Фабрика из типобезопасного ресурса: `UiText(Res.string.common_back)`. */
fun UiText(resource: StringResource, vararg args: String): UiText =
    UiText.Res(key = resource.key, args = args.toList())

fun UiText(value: String): UiText = UiText.Plain(value)

@Composable
fun UiText.resolve(): String = when (this) {
    is UiText.Plain -> value
    is UiText.Res -> {
        val resource = Res.allStringResources.getValue(key)
        if (args.isEmpty()) {
            stringResource(resource)
        } else {
            stringResource(resource, *args.toTypedArray())
        }
    }
}
