# Proposal

## Why

Этап 11 — последний крупный пользовательский сценарий MVP: клиент должен видеть историю заказа как понятные SYSTEM-карточки, подтверждать получение из `PROCESSING` и понимать, что диалог закрыт. Сейчас лента показывает только текст SYSTEM-плашек, проекция заказа не попадает в UI списка и шапки, а команда `ORDER_COMPLETED` отсутствует в клиенте. Без этого заказ в чате выглядит как обычная переписка, а завершить его из приложения нельзя.

## What Changes

- Добавить клиентскую команду `ORDER_COMPLETED` через `POST /dialogs/{id}/commands`; ответ-снапшот сохранять в БД, событие в ленте ждать из следующего опроса.
- Отображать SYSTEM-события заказа по `eventType` и `payload`, а не по тексту:
  - `ORDER_CREATED` — карточка оферты с номером, описанием и суммой;
  - `ORDER_PAID`, `ORDER_PROCESSING`, `ORDER_COMPLETED`, `ORDER_CANCELLED` — карточки статуса;
  - `ORDER_PROCESSING` — с действием «Подтвердить получение»;
  - `OPERATOR_ASSIGNED` / `OPERATOR_RELEASED` — компактные системные плашки;
  - `DIALOG_CLOSED` — карточка закрытия и блокировка ввода.
- Показывать проекцию заказа в шапке диалога: описание, сумма и бейдж статуса.
- Показывать в списке чатов номер/описание заказа, бейдж статуса, сумму и время.
- Обрабатывать `403` и `409` команды без автоматического ретрая; при `409` обновлять снапшот диалога.
- Обновить тестовый режим: корректные `eventType`/`payload`, событие `ORDER_PROCESSING`, выполнение `ORDER_COMPLETED`.
- Добавить тесты на сборку ленты/списка, переход `PROCESSING → COMPLETED` и ошибки команды.
- Обновить статус этапа 11 в документации.

Не меняются: авторизация, навигация, формат БД, способ синхронизации и отправки обычных сообщений/файлов.

## Capabilities

### New Capabilities

Нет.

### Modified Capabilities

- `orders`: карточки событий заказа, шапка заказа, команда `ORDER_COMPLETED` и обработка `403`/`409`.
- `chat-list`: номер/описание заказа, бейдж статуса и сумма в элементе списка.
- `messaging`: SYSTEM-события отображаются по `eventType` (карточки заказа/закрытия, плашки оператора), а не единой текстовой плашкой.
- `design-system`: новое требование к карточке системного события и действию внутри неё.

## Impact

- `shared/src/commonMain/kotlin/.../features/chats/data/` — DTO команды, `ChatsApi`, `DefaultChatsApi`, `TestChatsApi`.
- `shared/src/commonMain/kotlin/.../features/chats/domain/` — `ChatsInteractor`, модель команды/ошибки.
- `shared/src/commonMain/kotlin/.../features/chats/dialog/` — `DialogComponent`, `DialogVmState`, `DialogUiState`, `DialogAssembler`, `DefaultDialogComponent`.
- `shared/src/commonMain/kotlin/.../features/chats/dialog/ui/` — SYSTEM-карточки, order strip, кнопка подтверждения, закрытый футер.
- `shared/src/commonMain/kotlin/.../features/chats/list/` — `ChatListItemUiState`, `ChatListAssembler`, `ChatListScreen`.
- `shared/src/commonMain/composeResources/values/strings.xml` — строки карточек, действий и ошибок.
- `shared/src/commonMain/kotlin/.../features/chats/data/test/TestChatsStore.kt` — корректные события и команда.
- `shared/src/androidHostTest/kotlin/.../` — тесты store/assembler.
- `openspec/specs/{orders,chat-list,messaging,design-system}/spec.md` — delta-спеки.
- `docs/PROJECT.md`, `docs/PLAN.md` — статус этапа 11.
- `design/chat-shop.pen` — согласованные экраны (уже нарисованы).
- API: только существующий `POST /dialogs/{id}/commands`; новых эндпоинтов и зависимостей нет.
