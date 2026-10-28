package com.al4apps.chatshop.core.media

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerViewController
import platform.UIKit.UIViewController

/**
 * iOS-пикер через PHPicker: одно изображение, без доступа к photo library.
 * Презентуется от контроллера, переданного через [attach].
 */
class IosImagePicker : ImagePicker {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var host: UIViewController? = null
    private var pending: CompletableDeferred<PickedImage?>? = null
    private var delegate: IosImagePickerDelegate? = null

    /** Привязывает корневой контроллер приложения для презентации пикера. */
    fun attach(host: UIViewController) {
        this.host = host
    }

    override suspend fun pickImage(): PickedImage? = withContext(Dispatchers.Main) {
        val host = host ?: return@withContext null
        if (pending != null || host.presentedViewController != null) return@withContext null

        val deferred = CompletableDeferred<PickedImage?>()
        pending = deferred

        val configuration = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter
            selectionLimit = 1L
        }
        val picker = PHPickerViewController(configuration = configuration)
        val pickerDelegate = IosImagePickerDelegate(scope = scope) { image ->
            deferred.complete(image)
        }
        delegate = pickerDelegate
        picker.delegate = pickerDelegate
        host.presentViewController(picker, animated = true, completion = null)

        try {
            deferred.await()
        } finally {
            pending = null
            delegate = null
        }
    }
}
