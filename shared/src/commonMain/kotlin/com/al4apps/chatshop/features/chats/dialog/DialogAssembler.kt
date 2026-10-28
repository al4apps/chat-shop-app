package com.al4apps.chatshop.features.chats.dialog

import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.dialog_title
import chatshop.shared.generated.resources.order_number
import com.al4apps.chatshop.core.media.AudioPlaybackState
import com.al4apps.chatshop.core.media.VoiceRecordingState
import com.al4apps.chatshop.core.ui.UiText
import com.al4apps.chatshop.core.util.Money
import com.al4apps.chatshop.core.util.toClockLabel
import com.al4apps.chatshop.features.chats.dialog.model.DialogUiState
import com.al4apps.chatshop.features.chats.dialog.model.DialogVmState
import com.al4apps.chatshop.features.chats.dialog.model.MessageItemUiState
import com.al4apps.chatshop.features.chats.dialog.model.OrderEventUiState
import com.al4apps.chatshop.features.chats.dialog.model.OrderSummaryUiState
import com.al4apps.chatshop.features.chats.domain.model.Dialog
import com.al4apps.chatshop.features.chats.domain.model.DialogStatus
import com.al4apps.chatshop.features.chats.domain.model.FileStatus
import com.al4apps.chatshop.features.chats.domain.model.Message
import com.al4apps.chatshop.features.chats.domain.model.MessageType
import com.al4apps.chatshop.features.chats.domain.model.OrderStatus
import com.al4apps.chatshop.features.chats.domain.model.SendState
import com.al4apps.chatshop.features.chats.domain.model.SenderRole
import com.al4apps.chatshop.features.chats.ui.toBadgeTone
import com.al4apps.chatshop.features.chats.ui.toUiText
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Собирает uiState диалога из vmState, проекции диалога и ленты БД. */
class DialogAssembler(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {

    fun assemble(
        vmState: DialogVmState,
        dialog: Dialog?,
        messages: List<Message>,
        recording: VoiceRecordingState,
        canRecord: Boolean,
        playback: AudioPlaybackState,
    ): DialogUiState {
        val isClosed = dialog?.status == DialogStatus.CLOSED
        val items = messages.asReversed().map { it.toItem(playback) }
        val canConfirmReceipt = dialog != null &&
            !isClosed &&
            dialog.orderStatus == OrderStatus.PROCESSING &&
            !vmState.isConfirmActionForbidden
        val actionableId = if (canConfirmReceipt) {
            items.firstOrNull { item ->
                item.eventType == "ORDER_PROCESSING" &&
                    (item.orderEvent?.orderNo == null || item.orderEvent.orderNo == dialog.orderNo)
            }?.localId
        } else {
            null
        }

        return DialogUiState(
            title = dialog.toTitle(),
            orderSummary = dialog.toOrderSummary(),
            items = items
                .map { item ->
                    if (item.localId == actionableId) {
                        item.copy(showConfirmAction = true)
                    } else {
                        item
                    }
                }
                .toImmutableList(),
            inputText = vmState.inputText,
            isSendEnabled = vmState.inputText.isNotBlank() && !isClosed,
            isInputBlocked = isClosed,
            isConfirmingReceipt = vmState.isConfirmingReceipt,
            commandError = vmState.commandError,
            fullscreenImagePath = vmState.fullscreenImagePath,
            canRecord = canRecord && !isClosed,
            isRecording = recording.isRecording,
            recordingSeconds = recording.seconds,
        )
    }

    private fun Dialog?.toTitle(): UiText {
        val dialog = this
        val displayOrderNo = dialog?.orderNo?.takeIf { it > 0 }
        return when {
            displayOrderNo != null -> UiText(Res.string.order_number, displayOrderNo.toString())
            else -> dialog?.orderDescription
                ?.takeIf { it.isNotBlank() }
                ?.let { UiText(it) }
                ?: UiText(Res.string.dialog_title)
        }
    }

    private fun Dialog?.toOrderSummary(): OrderSummaryUiState? {
        val dialog = this ?: return null
        val status = dialog.orderStatus ?: return null
        if (dialog.orderNo <= 0) return null
        val isClosed = dialog.status == DialogStatus.CLOSED
        return OrderSummaryUiState(
            description = dialog.orderDescription?.takeIf { it.isNotBlank() },
            amount = dialog.orderAmount?.asDisplayString(),
            status = if (isClosed) dialog.status.toUiText() else status.toUiText(),
            tone = if (isClosed) dialog.status.toBadgeTone() else status.toBadgeTone(),
        )
    }

    private fun Message.toItem(playback: AudioPlaybackState): MessageItemUiState {
        val fileId = file?.id
        val isThisPlaying = fileId != null && playback.playingId == fileId
        return MessageItemUiState(
            localId = localId,
            text = text.orEmpty(),
            isMine = senderRole == SenderRole.CLIENT,
            isSystem = type == MessageType.SYSTEM,
            eventType = eventType,
            orderEvent = parseOrderEvent(),
            showConfirmAction = false,
            isImage = type == MessageType.IMAGE,
            imagePath = file?.localPath,
            isVoice = type == MessageType.VOICE,
            voiceFileId = fileId,
            voicePath = file?.localPath,
            voiceDurationSec = file?.durationSec ?: 0,
            isVoicePlaying = isThisPlaying && playback.isPlaying,
            voicePositionSec = if (isThisPlaying) playback.positionSec else 0,
            isFileDeleted = file?.status == FileStatus.DELETED,
            time = createdAt.toClockLabel(),
            isSending = sendState == SendState.PENDING || sendState == SendState.SENDING,
            isFailed = sendState == SendState.FAILED,
        )
    }

    private fun Message.parseOrderEvent(): OrderEventUiState? {
        val type = eventType ?: return null
        if (!type.startsWith("ORDER_")) return null
        val payload = payload
            ?.let { runCatching { json.parseToJsonElement(it).jsonObject }.getOrNull() }
        return OrderEventUiState(
            eventType = type,
            orderNo = payload?.get("orderNo")?.jsonPrimitive?.intOrNull,
            amount = payload?.get("amount")
                ?.jsonPrimitive
                ?.contentOrNull
                ?.let { raw -> Money.parseOrNull(raw)?.asDisplayString() ?: raw },
            description = payload?.get("description")?.jsonPrimitive?.contentOrNull,
        )
    }
}
