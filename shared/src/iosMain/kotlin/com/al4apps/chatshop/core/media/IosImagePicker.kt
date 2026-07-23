package com.al4apps.chatshop.core.media

/**
 * iOS-пикер. Заглушка до реализации PHPicker (см. DEBT) — приложение тестируется
 * на Android-эмуляторе; iOS-выбор фото будет добавлен отдельно.
 */
class IosImagePicker : ImagePicker {

    override suspend fun pickImage(): PickedImage? = null
}
