package com.al4apps.chatshop.core.media

import kotlinx.coroutines.CompletableDeferred

/**
 * Android-реализация пикера. App-scope singleton, мостящий к текущей Activity:
 * Activity в onCreate регистрирует launcher и вызывает [attach], в onDestroy — [detach].
 * Байты читаются в Activity и передаются в [deliverResult].
 */
class AndroidImagePicker : ImagePicker {

    private var launch: (() -> Unit)? = null
    private var pending: CompletableDeferred<PickedImage?>? = null

    fun attach(launch: () -> Unit) {
        this.launch = launch
    }

    fun detach() {
        launch = null
        pending?.complete(null)
        pending = null
    }

    /** Вызывается Activity с результатом пикера (или null при отмене). */
    fun deliverResult(image: PickedImage?) {
        pending?.complete(image)
        pending = null
    }

    override suspend fun pickImage(): PickedImage? {
        val launcher = launch ?: return null
        val deferred = CompletableDeferred<PickedImage?>()
        pending = deferred
        launcher()
        return deferred.await()
    }
}
