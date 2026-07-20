package com.al4apps.chatshop.features.chats.data

import com.al4apps.chatshop.core.network.AppError
import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.features.chats.domain.ChatsInteractor
import com.al4apps.chatshop.features.chats.domain.DialogsRepository
import com.al4apps.chatshop.features.chats.domain.model.Dialog
import kotlinx.coroutines.flow.Flow

class DefaultChatsInteractor(
    private val chatsApi: ChatsApi,
    private val dialogsRepository: DialogsRepository,
) : ChatsInteractor {

    override fun observeDialogs(): Flow<List<Dialog>> = dialogsRepository.observeDialogs()

    override suspend fun hasDialogs(): Boolean = dialogsRepository.hasDialogs()

    override suspend fun createDialog(): AppResult<String> =
        when (val result = chatsApi.createDialog()) {
            is AppResult.Failure -> result
            is AppResult.Success -> {
                val dialog = result.value.toDomainOrNull()
                if (dialog == null) {
                    AppResult.Failure(AppError.Unknown)
                } else {
                    dialogsRepository.upsertFromServer(listOf(dialog))
                    AppResult.Success(dialog.id)
                }
            }
        }
}
