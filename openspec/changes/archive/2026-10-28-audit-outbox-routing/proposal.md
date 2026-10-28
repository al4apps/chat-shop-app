# Proposal

## Why

`OutboxService` — единственный путь доставки исходящих сообщений, но у него нет ни одного теста. Ошибка маршрутизации (`dialogId`), потеря `clientMessageId`, дубль после ретрая или зависание очереди проявятся только на живом backend и будут выглядеть как «сообщение не туда ушло». Это дешёвый аудит с высоким эффектом перед стабилизацией MVP.

## What Changes

- Добавить тестовый seam для файлового хранилища outbox, чтобы `OutboxService` можно было проверять без Android Context/реального диска.
- Добавить управляемые фейки `ChatsApi`, `MessagesRepository`, `FilesRepository`, `OutboxFileStore` и `TokenStorage`.
- Покрыть тестами:
  - маршрутизацию сообщений по `dialogId`;
  - сохранение `senderRole = CLIENT` и `clientMessageId`;
  - повторную отправку с тем же `clientMessageId` без дубля;
  - ошибку одного диалога и продолжение очереди других;
  - восстановление `SENDING → PENDING` после падения процесса;
  - привязку локальных байтов к серверному `file.id`;
  - отсутствие `clientMessageId` → `FAILED`.
- Зафиксировать результаты аудита в `docs/BACKLOG.md`: что подтверждено, что требует отдельного фикса.
- Прогнать `:shared:testAndroidHostTest` и зафиксировать, что тесты не меняют поведение production-кода.

**Вне scope:** исправление найденных дефектов. Если тест покажет реальную ошибку маршрутизации или блокировку, аудит фиксирует её и отдельный change делает фикс. Этот change не меняет пользовательское поведение.

## Capabilities

### New Capabilities

Нет.

### Modified Capabilities

Нет. Поведение не меняется; change помечен `skip_specs: true`. Существующая спека `messaging` уже описывает очередь, ретраи и идемпотентность; аудит проверяет соответствие ей.

## Impact

- `shared/src/commonMain/kotlin/.../services/outbox/OutboxService.kt` — минимальный test seam (интерфейс файлового хранилища и параметры retry-политики).
- `shared/src/commonMain/kotlin/.../core/media/FileStore.kt` и платформенные `actual` — реализация seam-интерфейса, если выбран этот вариант.
- `shared/src/commonMain/kotlin/.../di/AppModules.kt` — провайдер seam-интерфейса.
- `shared/src/androidHostTest/kotlin/.../outbox/` — новые тесты и фейки.
- `docs/BACKLOG.md` — результат аудита и решение по `CHAT-OUTBOX-1`.
- API, БД, UI и пользовательские сценарии не меняются.
