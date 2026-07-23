package com.al4apps.chatshop.core.media

/**
 * Системный пикер изображений. Реализуется на краю приложения (Activity / UIViewController),
 * в shared приходит через интерфейс.
 */
interface ImagePicker {

    /**
     * Открывает системный выбор фото.
     *
     * @return выбранное изображение или null, если пользователь отменил / пикер недоступен.
     */
    suspend fun pickImage(): PickedImage?
}
