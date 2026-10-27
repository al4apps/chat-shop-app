# Design

## Context

См. `proposal.md` — Why. `TestChatsStore` — in-memory «сервер» тестового режима, общий для `TestSyncApi` и `TestChatsApi`. SQLDelight-схема хранит `messages.seq` как глобально уникальный курсор, а `DefaultMessagesRepository` обновляет строки по `seq` без `dialogId`. Тестовый стор сейчас нарушает этот контракт.

## Goals / Non-Goals

**Goals:**

- Привести `seq` в тестовом сторе к глобальному монотонному контракту.
- Перевести тестовые dialog/file ID на UUID, как в production.
- Сохранить независимые курсоры `afterSeq` по диалогам.
- Сделать повторную отправку с тем же `clientMessageId` идемпотентной.
- Покрыть поведение регрессионным тестом на несколько диалогов.

**Non-Goals:**

- Не менять production-код, API, SQLDelight-схему и UI.
- Не заменять `seq` на UUID: `seq` — это глобальный курсор/порядок, а не идентификатор.
- Не переделывать `TestAutoReplyService` или сценарии тест-режима.
- Не вводить новые зависимости.

## Decisions

### 1. Один глобальный счётчик `nextSeq`

В `TestChatsStore` вместо `seqCounters: MutableMap<String, Long>` вводится `private var nextSeq: Long`. `appendMessageLocked` делает `val seq = ++nextSeq` независимо от `dialogId`.

Почему:

- совпадает с production-контрактом «`seq` глобально монотонный»;
- `messages.seq INTEGER UNIQUE` и `updateBySeq` остаются корректными;
- `afterSeq` по-прежнему фильтруется внутри конкретного диалога, поэтому независимые курсоры сохраняются.

Отклонённая альтернатива — составной уникальный ключ `(dialogId, seq)`: он замаскировал бы баг тестового стора и ослабил production-инвариант.

### 2. Seed получает глобальные `seq`

Seed-диалоги получают последовательные глобальные значения:

- первый диалог: `seq = 1..4`, `lastMessageSeq = 4`;
- второй диалог: `seq = 5..6`, `lastMessageSeq = 6`;
- `nextSeq = 6` после seed.

Значения `lastMessageSeq` обновляются вместе с лентой, чтобы `TestSyncApi` и UI не расходились.

### 3. Идемпотентность по `clientMessageId`

Добавляется поиск существующего сообщения по `clientMessageId` во всех диалогах. Если сообщение уже есть, `addClientText`/`addClientFile` возвращают его и не создают новую запись. `addClientText` эмитит `clientMessages` только для нового сообщения, чтобы авто-ответ не дублировался.

Поиск выполняется под тем же `Mutex`, что и запись. Для тестового объёма линейный поиск достаточен; отдельный индекс не нужен.

### 4. UUID для тестовых dialog/file ID

`dialogCounter` и `fileCounter` удаляются. `createDialog` и `addClientFile` генерируют `Uuid.random().toString()`; seed-диалоги тоже получают UUID. `clientMessageId` уже приходит UUID из `MessagesInteractor` и не меняется.

Почему:

- production-контракт использует строковые UUID для `dialog.id`, `file.id` и `clientMessageId`;
- UUID исключает коллизии при сбросе/повторной инициализации тестового стора;
- тесты перестают зависеть от предсказуемых `test-dialog-1`.

`seq` остаётся `Long`: это глобальный курсор ленты, а не идентификатор сущности. Замена `seq` на UUID потребовала бы менять API, SQLDelight-схему и логику `afterSeq` и не входит в задачу.

### 5. Регрессионный тест в `androidHostTest`

Тест на JVM использует `runBlocking` и проверяет:

- `seq` уникален при чередовании сообщений в трёх диалогах;
- `messages(dialogId)` возвращает только сообщения этого диалога;
- `senderRole` и `dialogId` сохраняются;
- dialog/file ID являются UUID и не совпадают с seed-счётчиками;
- повторный вызов с тем же `clientMessageId` возвращает то же сообщение и не добавляет дубль.

`commonTest` не используется, чтобы не добавлять `kotlinx-coroutines-test`; `androidHostTest` уже запускается штатной задачей.

## Risks / Trade-offs

- **Seed `seq` изменится** → UI не показывает `seq`; тесты и `lastMessageSeq` обновляются вместе.
- **UUID-идентификаторы seed недетерминированы** → тесты получают ID из `store.dialogs()`, а не хардкодят `test-dialog-1`.
- **Линейный поиск `clientMessageId`** → объём тестовых данных мал; при росте можно добавить map.
- **Тестовый стор и production всё ещё разные реализации** → контракт `seq`/идемпотентности теперь совпадает; UI-тесты станут надёжнее.
- **Авто-ответы могут приходить на старое сообщение** → эмит `clientMessages` только при создании новой записи.

## Migration Plan

1. Обновить `TestChatsStore`: глобальный `nextSeq`, UUID для dialog/file ID, seed, идемпотентность.
2. Добавить `TestChatsStoreTest` в `androidHostTest`.
3. Прогнать `:shared:testAndroidHostTest`.
4. Обновить `docs/BACKLOG.md` — закрыть `CHAT-TESTSEQ-1`.
5. Откат — revert тестового стора и теста; production и БД не затрагиваются.

## Open Questions

Нет.
