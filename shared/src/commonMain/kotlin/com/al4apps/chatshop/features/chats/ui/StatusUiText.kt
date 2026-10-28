package com.al4apps.chatshop.features.chats.ui

import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.dialog_status_active
import chatshop.shared.generated.resources.dialog_status_assigned
import chatshop.shared.generated.resources.dialog_status_closed
import chatshop.shared.generated.resources.dialog_status_new
import chatshop.shared.generated.resources.order_status_cancelled
import chatshop.shared.generated.resources.order_status_completed
import chatshop.shared.generated.resources.order_status_created
import chatshop.shared.generated.resources.order_status_paid
import chatshop.shared.generated.resources.order_status_processing
import chatshop.shared.generated.resources.order_status_unknown
import com.al4apps.chatshop.core.design.model.StatusBadgeTone
import com.al4apps.chatshop.core.ui.UiText
import com.al4apps.chatshop.features.chats.domain.model.DialogStatus
import com.al4apps.chatshop.features.chats.domain.model.OrderStatus

fun OrderStatus.toUiText(): UiText = when (this) {
    OrderStatus.CREATED -> UiText(Res.string.order_status_created)
    OrderStatus.PAID -> UiText(Res.string.order_status_paid)
    OrderStatus.PROCESSING -> UiText(Res.string.order_status_processing)
    OrderStatus.COMPLETED -> UiText(Res.string.order_status_completed)
    OrderStatus.CANCELLED -> UiText(Res.string.order_status_cancelled)
    OrderStatus.UNKNOWN -> UiText(Res.string.order_status_unknown)
}

fun OrderStatus.toBadgeTone(): StatusBadgeTone = when (this) {
    OrderStatus.CREATED,
    OrderStatus.PAID,
    OrderStatus.PROCESSING,
    -> StatusBadgeTone.ACCENT

    OrderStatus.COMPLETED,
    OrderStatus.UNKNOWN,
    -> StatusBadgeTone.NEUTRAL

    OrderStatus.CANCELLED -> StatusBadgeTone.ERROR
}

fun DialogStatus.toUiText(): UiText = when (this) {
    DialogStatus.NEW -> UiText(Res.string.dialog_status_new)
    DialogStatus.ASSIGNED -> UiText(Res.string.dialog_status_assigned)
    DialogStatus.ACTIVE -> UiText(Res.string.dialog_status_active)
    DialogStatus.CLOSED -> UiText(Res.string.dialog_status_closed)
    DialogStatus.UNKNOWN -> UiText(Res.string.order_status_unknown)
}

fun DialogStatus.toBadgeTone(): StatusBadgeTone = when (this) {
    DialogStatus.NEW,
    DialogStatus.ASSIGNED,
    DialogStatus.ACTIVE,
    -> StatusBadgeTone.ACCENT

    DialogStatus.CLOSED,
    DialogStatus.UNKNOWN,
    -> StatusBadgeTone.NEUTRAL
}
