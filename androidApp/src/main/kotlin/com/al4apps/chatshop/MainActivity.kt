package com.al4apps.chatshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.lifecycle.lifecycleScope
import com.al4apps.chatshop.core.coroutines.AppDispatchers
import com.al4apps.chatshop.core.media.AndroidImagePicker
import com.al4apps.chatshop.core.media.ImagePicker
import com.al4apps.chatshop.core.media.PickedImage
import com.al4apps.chatshop.root.DefaultRootComponent
import com.arkivanov.decompose.retainedComponent
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {

    private val imagePicker: ImagePicker = get()

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        val picker = imagePicker as? AndroidImagePicker ?: return@registerForActivityResult
        if (uri == null) {
            picker.deliverResult(null)
            return@registerForActivityResult
        }
        lifecycleScope.launch {
            val picked = withContext(AppDispatchers.io) { readImage(uri) }
            picker.deliverResult(picked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        (imagePicker as? AndroidImagePicker)?.attach {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        val root = retainedComponent { componentContext ->
            DefaultRootComponent(
                componentContext = componentContext,
                sessionManager = get(),
                authInteractor = get(),
                appPreferences = get(),
                syncService = get(),
                chatsInteractor = get(),
                messagesInteractor = get(),
                imagePicker = imagePicker,
            )
        }

        setContent {
            App(root)
        }
    }

    override fun onDestroy() {
        (imagePicker as? AndroidImagePicker)?.detach()
        super.onDestroy()
    }

    private fun readImage(uri: android.net.Uri): PickedImage? {
        val resolver = contentResolver
        val mime = resolver.getType(uri) ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        return PickedImage(bytes = bytes, mime = mime)
    }
}
