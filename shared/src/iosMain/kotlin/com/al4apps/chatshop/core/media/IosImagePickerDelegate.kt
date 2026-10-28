package com.al4apps.chatshop.core.media

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.darwin.NSObject

private const val IMAGE_TYPE = "public.image"
private const val JPEG_QUALITY = 0.95

/**
 * Делегат PHPicker: получает выбранный результат, загружает изображение
 * и отдаёт JPEG-байты в [onResult]. Всегда закрывает пикер.
 */
internal class IosImagePickerDelegate(
    private val scope: CoroutineScope,
    private val onResult: (PickedImage?) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        if (result == null) {
            finish(picker, null)
            return
        }

        result.itemProvider.loadDataRepresentationForTypeIdentifier(IMAGE_TYPE) { data, _ ->
            val image = data?.toJpegBytes()
            scope.launch(Dispatchers.Main) {
                finish(picker, image)
            }
        }
    }

    private fun finish(picker: PHPickerViewController, image: PickedImage?) {
        picker.dismissViewControllerAnimated(flag = true, completion = null)
        onResult(image)
    }

    private fun NSData.toJpegBytes(): PickedImage? {
        val image = UIImage.imageWithData(this) ?: return null
        val jpeg = UIImageJPEGRepresentation(image, JPEG_QUALITY) ?: return null
        return PickedImage(bytes = jpeg.toByteArray(), mime = "image/jpeg")
    }
}
