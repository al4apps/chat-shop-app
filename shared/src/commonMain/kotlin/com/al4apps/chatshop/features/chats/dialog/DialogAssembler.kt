package com.al4apps.chatshop.features.chats.dialog

import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.dialog_title
import com.al4apps.chatshop.core.ui.UiText
import com.al4apps.chatshop.core.util.toClockLabel
import com.al4apps.chatshop.features.chats.dialog.model.DialogUiState
import com.al4apps.chatshop.features.chats.dialog.model.DialogVmState
import com.al4apps.chatshop.features.chats.dialog.model.MessageItemUiState
import com.al4apps.chatshop.features.chats.domain.model.Dialog
import com.al4apps.chatshop.features.chats.domain.model.DialogStatus
import com.al4apps.chatshop.features.chats.domain.model.FileStatus
import com.al4apps.chatshop.features.chats.domain.model.Message
import com.al4apps.chatshop.features.chats.domain.model.MessageType
import com.al4apps.chatshop.features.chats.domain.model.SendState
import com.al4apps.chatshop.features.chats.domain.model.SenderRole
import kotlinx.collections.immutable.toImmutableList

/** Собирает uiState диалога из vmState, проекции диалога и ленты БД. */
class DialogAssembler {

    fun assemble(
        vmState: DialogVmState,
        dialog: Dialog?,
        messages: List<Message>,
    ): DialogUiState {
        val isClosed = dialog?.status == DialogStatus.CLOSED
        return DialogUiState(
            title = dialog?.orderDescription
                ?.takeIf { it.isNotBlank() }
                ?.let { UiText(it) }
                ?: UiText(Res.string.dialog_title),
            items = messages.asReversed().map { it.toItem() }.toImmutableList(),
            inputText = vmState.inputText,
            isSendEnabled = vmState.inputText.isNotBlank() && !isClosed,
            isInputBlocked = isClosed,
        )
    }

    private fun Message.toItem(): MessageItemUiState = MessageItemUiState(
        localId = localId,
        text = text.orEmpty(),
        isMine = senderRole == SenderRole.CLIENT,
        isSystem = type == MessageType.SYSTEM,
        isImage = type == MessageType.IMAGE,
        imagePath = file?.localPath,
        isFileDeleted = file?.status == FileStatus.DELETED,
        time = createdAt.toClockLabel(),
        isSending = sendState == SendState.PENDING || sendState == SendState.SENDING,
        isFailed = sendState == SendState.FAILED,
    )
}
