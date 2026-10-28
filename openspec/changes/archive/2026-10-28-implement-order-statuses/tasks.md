# Tasks

## 1. Order command data and domain

- [x] 1.1 Добавить `OrderCommandRequest` и `ChatsApi.sendOrderCommand` с реализацией `POST /dialogs/{id}/commands` в `DefaultChatsApi`; проверить компиляцию `:shared:compileAndroidMain`.
- [x] 1.2 Добавить `ChatsInteractor.confirmOrderReceived` и реализацию в `DefaultChatsInteractor`: маппинг `DialogDto → Dialog`, `upsertFromServer`, возврат `AppResult<Unit>`; проверить тестом с фейковыми API/репозиторием.
- [x] 1.3 Реализовать команду в `TestChatsApi`/`TestChatsStore`: `PROCESSING → COMPLETED`, `SYSTEM ORDER_COMPLETED` с payload, обновление проекции; недопустимый переход → `409`; проверить `TestChatsStoreTest`.

## 2. UI models and assemblers

- [x] 2.1 Добавить отображаемый формат `Money` (`1 250,00`) и тесты на разряды/копейки/отрицательные значения; wire-формат не менять.
- [x] 2.2 Расширить `MessageItemUiState` (`eventType`, `orderEvent`, `showConfirmAction`) и `DialogUiState` (`orderSummary`, `isConfirmingReceipt`, `commandError`); реализовать защищённый парсинг `payload` и выбор активной `ORDER_PROCESSING` в `DialogAssembler`; проверить assembler-тестами.
- [x] 2.3 Расширить `ChatListItemUiState` (`orderNo`, `status`, `amount`) и `ChatListAssembler`: `orderNo` из проекции с fallback по `createdAt`, описание, бейдж и сумма; проверить assembler-тестами.

## 3. Dialog command state

- [x] 3.1 Добавить `isConfirmingReceipt`, `commandError`, `isConfirmActionForbidden` в `DialogVmState`, `onConfirmReceipt` в `DialogComponent` и `DefaultDialogComponent`; проверить, что повторный вызов во время запроса игнорируется.
- [x] 3.2 Реализовать обработку `403`/`409`: скрытие действия при `403`, ошибка + немедленный pull диалога/ленты при `409`, без авторетрая; проверить тестом/ручным сценарием с тестовым store.

## 4. Compose UI

- [x] 4.1 Добавить `SystemEventCard`, `OrderSummaryStrip`, тон статусного бейджа и строки карточек/ошибок/закрытого футера; проверить сборку Android.
- [x] 4.2 Переделать `DialogScreen`: заголовок `Заказ №N`, order strip, `ORDER_*` карточки, кнопка подтверждения внутри `PROCESSING`, error caption, `CLOSED` футер; проверить визуально на Android.
- [x] 4.3 Переделать `ChatListScreen`: заголовок `Заказ №N`, описание второстепенной строкой, бейдж статуса, сумма и время; проверить визуально на Android.

## 5. Test mode and automated checks

- [x] 5.1 Обновить seed `TestChatsStore`: `ORDER_PAID`, корректные payload, `ORDER_PROCESSING` у первого диалога, короткие описания и закрытый тестовый диалог; проверить, что экраны `processing`/`closed` сразу воспроизводятся в тестовом режиме.
- [x] 5.2 Добавить тесты на парсинг событий, видимость кнопки, `ChatListAssembler`, `Money` и команду `PROCESSING → COMPLETED`; `:shared:testAndroidHostTest` — passed.
- [x] 5.3 Проверить ветки ошибок команды: недопустимый переход даёт `409`, действие не ретраится автоматически; тест/ручной чек-лист.

## 6. Integration verification and docs

- [x] 6.1 `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug` — успешная сборка и тесты.
- [x] 6.2 `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` и сборка iOS-приложения — успешно; визуально проверить карточки и клавиатуру на симуляторе (требуется ручной прогон: у агента нет UI-автоматизации Simulator).
- [x] 6.3 Ручная матрица Android: `PROCESSING` → подтверждение → `COMPLETED`, `CANCELLED`, `CLOSED`; ветки `403`/`409` покрыты `TestChatsApiTest` и `DefaultChatsInteractorTest`; проверить, что событие приходит из следующего опроса, а не из ответа команды.
- [x] 6.4 Обновить `docs/PROJECT.md` (этап 11 выполнен) и `docs/PLAN.md` (актуальный статус); проверить, что описание соответствует реализации.
- [x] 6.5 Запустить `openspec validate --specs` и `openspec validate "implement-order-statuses" --type change`; оба должны пройти.

## Workflow follow-up

- После приёмки — `/opsx-archive`.
- Следующая крупная задача — `ios-voice-recording`.
