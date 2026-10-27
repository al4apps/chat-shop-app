# Tasks

## 1. Test Store Seq Contract

- [x] 1.1 Заменить в `TestChatsStore` per-dialog `seqCounters` на глобальный `nextSeq`; обновить seed так, чтобы сообщения двух диалогов имели уникальные `seq = 1..6`, а `lastMessageSeq` совпадал с лентой; проверить компиляцию `:shared:compileAndroidMain`.
- [x] 1.2 Сохранить независимые курсоры `afterSeq`: `messages(dialogId, afterSeq, limit)` должен фильтровать только сообщения этого диалога; проверить тестом на два диалога с чередующимися сообщениями.
- [x] 1.3 Убрать `dialogCounter`/`fileCounter`, генерировать dialog/file ID через `Uuid.random().toString()` (включая seed); проверить, что тесты получают ID из `store.dialogs()`, а не хардкодят `test-dialog-1`.

## 2. Idempotency

- [x] 2.1 Добавить в `TestChatsStore` поиск по `clientMessageId` под общим `Mutex`; `addClientText` и `addClientFile` должны возвращать существующее сообщение без создания дубля; проверить тестом повторной отправки.
- [x] 2.2 Не эмитить `clientMessages` повторно для уже существующего текста, чтобы `TestAutoReplyService` не создавал лишний ответ; проверить тестом/логом.

## 3. Regression Tests

- [x] 3.1 Добавить `TestChatsStoreTest` в `shared/src/androidHostTest`; проверить, что `seq` уникален для трёх диалогов, `dialogId` и `senderRole` не смешиваются, `messages()` возвращает только ленту запрошенного диалога, а dialog/file ID — UUID.
- [x] 3.2 Добавить тест идемпотентности: повторный `addClientText`/`addClientFile` с тем же `clientMessageId` возвращает тот же `seq` и не увеличивает число сообщений; проверить прохождение теста.

## 4. Documentation

- [x] 4.1 Обновить `docs/BACKLOG.md`: перевести `CHAT-TESTSEQ-1` в закрытые, указать коммит/тест и результат; проверить, что запись больше не висит в открытых.

## 5. Verification

- [x] 5.1 Запустить `./gradlew :shared:testAndroidHostTest`; все тесты, включая новые, должны пройти.
- [x] 5.2 Запустить `./gradlew :androidApp:assembleDebug`; тестовый стор компилируется в составе приложения.
- [x] 5.3 Проверить в тестовом режиме на Android: отправить сообщения в два диалога по очереди, убедиться, что они остаются в своих чатах, стороны отправителей корректны, `FAILED`/повтор не создаёт дубль.
- [x] 5.4 Проверить `git diff`: изменены только `TestChatsStore`, тест и `docs/BACKLOG.md`; production-код, API, SQLDelight-схема и UI не затронуты.
- [x] 5.5 Запустить `openspec validate --specs` и `openspec validate "fix-test-chat-seq" --type change`; убедиться, что `skip_specs: true` принимается.

## Workflow follow-up

- После приёмки архивировать change: `/opsx-archive`.
- Следующий шаг — `fix-chat-ime-insets`, затем `implement-order-statuses`.
