# Proposal

## Why

В тестовом режиме `TestChatsStore` ведёт `seq` отдельно для каждого диалога, тогда как в SQLDelight `messages.seq` глобально уникален, а repository обновляет сообщения по `seq`. Из-за этого сообщения разных диалогов могут перезаписывать друг друга, попадать не в тот чат или не отображаться. Это ломает доверие к тестовому режиму и мешает проверять сценарии чатов и будущий этап статусов заказа.

## What Changes

- Заменить per-dialog `seqCounters` в `TestChatsStore` на один глобальный монотонный счётчик, как в production-контракте.
- Перевести тестовые идентификаторы диалогов и файлов с счётчиков `test-dialog-N`/`test-file-N` на UUID, как в production; `seq` при этом остаётся глобальным `Long`-курсором, а не идентификатором.
- Обновить тестовый seed: все сообщения получают уникальные глобальные `seq`, `lastMessageSeq` диалогов согласован с лентой.
- Сохранить семантику курсора `afterSeq`: каждый диалог по-прежнему отдаёт только свои сообщения после курсора.
- Сделать `clientMessageId` идемпотентным в тестовом сторе для текста и файлов: повторный вызов возвращает существующее сообщение и не создаёт дубль/лишний авто-ответ.
- Добавить регрессионные тесты: два-три диалога с чередующимися сообщениями, уникальность `seq`, корректный `senderRole`/`dialogId`, повтор с тем же `clientMessageId`.
- Обновить `docs/BACKLOG.md`: `CHAT-TESTSEQ-1` перевести в закрытые после проверки.
- Не менять production-код, API, SQLDelight-схему, UI и бизнес-логику.

## Capabilities

### New Capabilities

Нет.

### Modified Capabilities

Нет. Change исправляет тестовую инфраструктуру и не меняет spec-level поведение продукта; `skip_specs: true` установлен в `.openspec.yaml`.

## Impact

- `shared/src/commonMain/kotlin/com/al4apps/chatshop/features/chats/data/test/TestChatsStore.kt`.
- Тесты в `shared/src/androidHostTest` (JVM) для `TestChatsStore`.
- `docs/BACKLOG.md` — закрытие `CHAT-TESTSEQ-1`.
- Production-код, API, БД, UI и зависимости не затрагиваются.
