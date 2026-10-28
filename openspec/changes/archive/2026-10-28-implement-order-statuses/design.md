# Design

## Context

См. `proposal.md` — Why. Текущее состояние:

- `Message` уже хранит `eventType` и `payload` (сырой JSON), но `MessageItemUiState` их теряет, поэтому `SystemMessage` рисует только `text`.
- `Dialog` уже содержит `orderStatus`, `orderAmount` (`Money`), `orderDescription`, `orderNo`; `ChatListAssembler` использует только описание/статус/время.
- `ChatsApi` умеет создавать диалог и отправлять сообщения/файлы, но команды заказа отсутствуют.
- Тестовый `TestChatsStore` содержит неверный `eventType = "PAID"`, не содержит `ORDER_PROCESSING` и не умеет выполнять `ORDER_COMPLETED`.
- Согласованный дизайн лежит в `design/chat-shop.pen`, секция `Chat screens`: текущие экраны и состояния `processing`, `completed`, `cancelled`, `closed`.

Ограничения: БД — единственный источник истины для UI; деньги — `Money`/decimal-строка; общий код Android/iOS; новых зависимостей и миграций схемы нет.

## Goals / Non-Goals

**Goals:**

- Реализовать `ORDER_COMPLETED` и полный набор состояний заказа в общем коде.
- Сделать SYSTEM-события типизированными в UI: карточки заказа, плашки оператора, карточка закрытия.
- Показать проекцию заказа в списке и шапке диалога.
- Обеспечить предсказуемые `403`/`409` без авторетрая.
- Обновить тестовый режим и покрыть ключевую логику тестами.

**Non-Goals:**

- Не менять SQLDelight-схему: проекция уже хранится.
- Не реализовывать операторские команды и оплату в приложении.
- Не добавлять push-уведомления и новый протокол синхронизации.
- Не менять отправку TEXT/IMAGE/VOICE и outbox.
- Не делать отдельный экран истории заказов: история остаётся в ленте.

## Decisions

### 1. Команда заказа — через `ChatsInteractor` и `ChatsApi`

Добавить `OrderCommandRequest(type: String)` и метод `ChatsApi.sendOrderCommand(dialogId, request): AppResult<DialogDto>`. `DefaultChatsApi` выполняет `POST /dialogs/{id}/commands`; `TestChatsApi` делегирует в `TestChatsStore`.

`ChatsInteractor` получает `confirmOrderReceived(dialogId): AppResult<Unit>`: вызывает API, маппит ответ в доменный `Dialog`, сохраняет снапшот через `DialogsRepository.upsertFromServer` и возвращает результат. UI обновляется из БД, а не напрямую из ответа.

Почему не из компонента: компонент не должен знать HTTP и DTO; правило «сеть → DTO → mapper → БД → Flow» сохраняется. Почему не `MessagesInteractor`: команда меняет диалог/проекцию, а не сообщение.

### 2. Состояние команды — в `DialogVmState`

Добавить в `DialogVmState`:

- `isConfirmingReceipt: Boolean` — запрос выполняется;
- `commandError: UiText?` — текст ошибки;
- `isConfirmActionForbidden: Boolean` — после `403` действие скрыто до конца сессии экрана.

`DefaultDialogComponent.onConfirmReceipt()`:

1. игнорирует вызов, если запрос уже идёт или действие запрещено;
2. ставит `isConfirmingReceipt = true`, очищает ошибку;
3. вызывает `chatsInteractor.confirmOrderReceived(dialogId)`;
4. при успехе снимает флаг: проекция и кнопка обновятся из БД;
5. при `403` ставит `isConfirmActionForbidden = true` и показывает ошибку;
6. при `409` показывает ошибку и немедленно обновляет снапшот диалога и ленту (`syncDialogsOnce` + `syncMessagesOnce`);
7. при сети/другой ошибке показывает ошибку и разрешает ручной повтор.

Автоматического ретрая нет: `ORDER_COMPLETED` — команда перехода состояния, повтор после неоднозначного ответа может дать `409`.

### 3. SYSTEM-события парсятся в UI-модель один раз

Добавить `OrderEventUiState`:

```
eventType: String
orderNo: Int?
amount: String?      // уже форматированная decimal-строка
description: String?
```

`MessageItemUiState` получает `eventType`, `orderEvent` и `showConfirmAction`. `DialogAssembler`:

- разбирает `payload` защищённо (`runCatching` + `jsonObject`); при ошибке оставляет `orderEvent = null`;
- для `ORDER_CREATED` берёт `description`, `amount`, `orderNo`;
- для остальных `ORDER_*` — `amount`, `orderNo`;
- не использует `text` для ветвления, только как fallback/пояснение;
- вычисляет `showConfirmAction` только у последнего `ORDER_PROCESSING` текущего заказа, если `orderStatus = PROCESSING`, запрос не идёт и действие не запрещено.

Почему в assembler: UI получает готовое состояние, Compose остаётся stateless; парсинг не размазывается по экрану. Альтернатива — парсить в каждом Composable — отвергнута.

### 4. Новый виджет карточки и статусные бейджи

Добавить `SystemEventCard` в `core/design/widgets`: иконка, заголовок, пояснение, опциональное действие. Карточка использует `AppTheme`-токены: `background`, радиус 16, divider, мягкая тень; действие — высота 44, радиус 12.

`DialogScreen`:

- TopBar: `Заказ №N` (или fallback `orderDescription`/«Диалог»);
- под TopBar — `OrderSummaryStrip`: описание, сумма, `AppBadge` статуса;
- лента: `ORDER_*` — `SystemEventCard`, `OPERATOR_*` — существующая плашка, `DIALOG_CLOSED` — карточка закрытия;
- ошибка команды — caption над панелью ввода;
- `CLOSED` — вместо `MessageInputBar` футер с замком и пояснением.

`ChatListScreen`: `Заказ №N`, описание второстепенной строкой, `AppBadge` статуса, сумма и время. Тон бейджа: accent для `CREATED/PAID/PROCESSING`, neutral для `COMPLETED/CLOSED`, error для `CANCELLED`.

### 5. Номер заказа

Для списка и шапки использовать `Dialog.orderNo`, если он больше нуля; иначе — прежний расчёт по `createdAt`. Это сохраняет стабильность и убирает расхождение между списком и диалогом. Серверный `orderNo` — часть проекции по контракту.

### 6. Деньги

Добавить в `Money` отображаемое представление (разряды и запятая, например `1 250,00`), без `Double`/`Float`; символ валюты добавляется строкой UI. Форматирование используется в списке, шапке и карточках; wire-формат остаётся `1250.00`.

### 7. Тестовый режим

`TestChatsStore`:

- исправить seed: `ORDER_PAID` вместо `PAID`, корректные payload `{orderNo, amount, description}`, добавить `ORDER_PROCESSING` в первый диалог;
- описания сделать короткими (`Тормозные колодки`, `Масляный фильтр`);
- добавить `completeOrder(dialogId)`: разрешает только `PROCESSING`, добавляет `SYSTEM ORDER_COMPLETED` с payload, обновляет проекцию на `COMPLETED` и возвращает `DialogDto`; иначе — ошибка `409`.

`TestChatsApi.completeOrder` возвращает `AppResult.Failure(AppError.Api(409, ...))` для недопустимого перехода, чтобы UI-ветка ошибки проверялась без сети.

## Risks / Trade-offs

- **`payload` может быть неполным или невалидным** → парсинг защищённый, `text` fallback; карточка всё равно отображается.
- **Несколько `ORDER_PROCESSING` в истории** → действие показывается только у последнего события текущего заказа; старые карточки остаются без кнопки.
- **`403` скрывает действие до конца экрана** → если права изменятся, потребуется переоткрыть диалог; это безопаснее повторных отказов.
- **`409` после успешной команды** → нет авторетрая; немедленный pull обновит проекцию, пользователь увидит актуальный статус.
- **`orderNo` от сервера vs расчёт по созданию** → используем серверный при `>0`, fallback сохраняет совместимость; проверить на тестовом seed.
- **Карточки увеличивают высоту ленты** → `LazyColumn` с `reverseLayout` уже прокручивается; проверить на маленьких экранах.
- **iOS** → изменения только в common Compose; отдельной платформенной логики нет, но нужен визуальный прогон клавиатуры/карточек на симуляторе.

## Migration Plan

1. Добавить DTO/API/интерактор команды и тестовую реализацию; собрать.
2. Расширить UI-модели и assembler; обновить строки.
3. Переделать `DialogScreen` и `ChatListScreen` под карточки/бейджи/футер.
4. Обновить seed и тесты; прогнать `:shared:testAndroidHostTest`, Android/iOS сборки.
5. Ручной сценарий: processing → подтверждение → completed; cancelled; closed; 403/409 через тестовый store.
6. Обновить `docs/PROJECT.md`, `docs/PLAN.md`.

Откат — revert изменений UI/данных; схема БД не меняется, миграции не требуются.
