# ChatShop — backlog

> Известные дефекты, исследования и будущие задачи. В отличие от [`DEBT.md`](DEBT.md),
> здесь фиксируются **что нужно сделать**, а не осознанно принятый компромисс.
>
> Формат записи: ID, статус, приоритет, контекст, факт/гипотеза, acceptance criteria,
> следующий шаг. Перед началом работы запись превращается в OpenSpec change.

## Открыто

## [IOS-VOICE-1] iOS: запись голосовых сообщений

- **Статус:** backlog
- **Приоритет:** высокий (функциональный паритет Android/iOS)
- **Контекст:** `IosAudioRecorder` — заглушка (`isAvailable = false`), на Android запись
  работает через `AndroidAudioRecorder` (MediaRecorder). Воспроизведение на iOS
  реализовано (`AVAudioPlayer`).
- **Факт:** исторически аудио делалось через `expect/actual` без библиотек. Сейчас
  приоритет — сначала стабильная KMP-библиотека.
- **Что проверить:** `KRecorder`, `kmp-record`, `kmp-audio-recorder-player`, `Klarinet`
  и другие актуальные KMP-решения. Критерии: стабильный релиз, поддержка текущих
  Kotlin/Compose Multiplatform, iOS Simulator, AAC/M4A, разрешения, лицензия, размер,
  активность поддержки.
- **Acceptance criteria:**
  - запись, стоп и отмена работают на iOS и Android;
  - запрос разрешения на микрофон и корректная деградация при отказе;
  - длительность 1–120 секунд, реальный `durationSec` уходит в `VOICE`;
  - запись отправляется через outbox и воспроизводится на обеих платформах;
  - есть тест/ручной чек-лист и обновлённый `DEBT.md`.
- **Следующий шаг:** spike-сравнение библиотек, решение в `design.md` change-а.

## [CHAT-OUTBOX-2] Outbox: сетевые ретраи одного диалога задерживают другие

- **Статус:** backlog
- **Приоритет:** средний
- **Контекст:** `OutboxService` обрабатывает глобальную голову `selectPending`, поэтому
  PENDING-сообщения всех диалогов сериализуются. При сетевой ошибке первого диалога
  второй ждёт до исчерпания трёх попыток с backoff.
- **Факт:** тест `OutboxServiceRoutingTest.networkExhaustionFailsFirstDialogAndStillSendsSecond`
  подтверждает, что второй диалог не блокируется навсегда, но отправляется только после
  трёх неудачных попыток первого. С production-политикой это задержка около 6 секунд.
- **Acceptance criteria:**
  - принять решение: оставить глобальную очередь как осознанный компромисс или
    перейти на очередь/воркер на диалог;
  - порядок сообщений внутри одного диалога сохраняется;
  - есть тест на два диалога, где ретраи первого не задерживают отправку второго.
- **Следующий шаг:** design decision и отдельный change; в `audit-outbox-routing`
  поведение намеренно не менялось.

## Закрыто

## [CHAT-OUTBOX-1] Аудит маршрутизации и отправки сообщений

- **Статус:** закрыто (2026-10-28)
- **Проверено:** change `audit-outbox-routing`
- **Что сделано:** добавлены `OutboxServiceRoutingTest` (9 сценариев) и фейки
  `ChatsApi`/`MessagesRepository`/`FilesRepository`/`OutboxFileStore`/`TokenStorage`;
  production-поведение не менялось, добавлены только test seam `OutboxFileStore` и
  `RetryPolicy`.
- **Покрытие:** маршрутизация `dialogId`, порядок в диалоге, автоматический повтор с тем же
  `clientMessageId`, ручной повтор `FAILED`, ошибка одного диалога, исчерпание трёх сетевых
  попыток, восстановление `SENDING` после старта, привязка IMAGE к серверному `file.id`,
  отсутствие `clientMessageId` → `FAILED`. `senderRole = CLIENT` для тестового store
  проверяется `TestChatsStoreTest`; production-запросы outbox сторону не подменяют.
- **Проверка:**
  `./gradlew :shared:testAndroidHostTest --tests "com.al4apps.chatshop.services.outbox.OutboxServiceRoutingTest"`
  — passed (9/9). Полный прогон `:shared:testAndroidHostTest` — passed.
- **Результат:** acceptance criteria аудита закрыты на уровне сервиса. SQL-транзакции и
  реальный backend в этот change не входили. Наблюдение о задержке других диалогов
  вынесено в `CHAT-OUTBOX-2`.

## [CHAT-TESTSEQ-1] Тестовый режим: коллизия `seq` между диалогами

- **Статус:** закрыто (2026-10-27)
- **Исправлено:** change `fix-test-chat-seq`
- **Что сделано:** глобальный `nextSeq` в `TestChatsStore`, seed `seq = 1..6`,
  UUID для dialog/file ID, идемпотентность по `clientMessageId`.
- **Проверка:** `TestChatsStoreTest` — уникальность `seq`, изоляция диалогов,
  `senderRole`/`dialogId`, UUID, повтор без дубля; `:shared:testAndroidHostTest` — passed.
- **Результат:** сообщения разных диалогов больше не конкурируют за один `seq` в тестовом режиме.

## [CHAT-IME-1] Android: клавиатура и IME-отступы

- **Статус:** закрыто (2026-10-27; Android; iOS — только сборка)
- **Исправлено:** change `fix-chat-ime-insets`
- **Что сделано:** IME-padding перенесён с корневого `Column` в `DialogScreen` на
  `MessageInputBar` (Dialog и FirstOrder); Auth оставлен только на `safeContentPadding`;
  в `MainActivity` добавлен `windowSoftInputMode="adjustResize"`.
- **Проверка:** Android API 37.2 (Pixel 10 Pro), API 36.1 и API 30 (Medium Tablet):
  Auth phone/code, Dialog и FirstOrder — при открытой клавиатуре нет ложного верхнего
  отступа, поле ввода видно, TopBar на месте; отправка сообщения работает; Back сначала
  закрывает клавиатуру, затем экран. iOS: `linkDebugFrameworkIosSimulatorArm64` и сборка
  приложения — passed; визуальная проверка клавиатуры на iOS в этой итерации не выполнялась.
- **Результат:** общий UI-фикс, не только test-mode.
