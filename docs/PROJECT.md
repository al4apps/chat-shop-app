# ChatShop — описание проекта

> Рабочая память о проекте: что это, как устроено и на каком этапе находится.
> Обновлять при значимых архитектурных решениях.
>
> - Правила кода и компонентов — [`CODING_RULES.md`](CODING_RULES.md)
> - Архитектурный план и этапы — [`PLAN.md`](PLAN.md)
> - Семантика backend-контракта — [`CLIENT_GUIDE.md`](CLIENT_GUIDE.md)
> - Техдолг — [`DEBT.md`](DEBT.md)
> - Backlog известных задач — [`BACKLOG.md`](BACKLOG.md)

---

## 0. Продукт и ответственность

**ChatShop** — клиентский сервис chat-shopping. Пользователь запускает приложение,
авторизуется и ведёт диалог о покупке: выбор товара, вопросы, оформление и статусы
происходят в чате. Платежей в приложении нет. Технически это мессенджер с
`SYSTEM`-сообщениями, который позиционируется как чат-шоп.

Модель работы:

- **Куратор продукта** задаёт приоритеты и принимает ключевые решения; обычно смотрит
  результат верхнеуровнево и подключается детально по необходимости.
- **Агент-разработчик** отвечает за продукт и архитектуру до конца: предлагает итерации,
  следит за качеством, ведёт roadmap, документацию и спеки, выявляет риски и не ждёт
  постановки каждой строки кода.

Итерации могут меняться по мере развития продукта; изменения поведения фиксируются
через OpenSpec, а осознанные компромиссы — в [`DEBT.md`](DEBT.md), задачи и дефекты —
в [`BACKLOG.md`](BACKLOG.md).

---

## 1. Что это за приложение

**ChatShop** — клиентское KMP-приложение (**только Android и iOS**) для сервиса заказа
через диалог с оператором вместо корзины/каталога. Тематическая легенда бэкенда —
«магазин автозапчастей», но UI и логика от неё не зависят: домен — универсальный
chat-commerce (диалог + лента событий + проекция заказа).

**Рынок — Таджикистан**: телефон `+992` + 9 цифр (например `+992 988009090`), язык —
русский, локализация только `ru`.

Ключевая идея (подробно — [`CLIENT_GUIDE.md`](CLIENT_GUIDE.md) §1, §5, §6):

- Диалог = лента сообщений (`seq`, монотонный курсор) — единственный источник истины.
- Отдельной REST-сущности заказа нет: заказ живёт как `SYSTEM`-сообщения в ленте +
  проекция текущего состояния на `Dialog` (`orderStatus/orderAmount/orderDescription/orderNo`).
- Клиент не создаёт `SYSTEM`-сообщения и не меняет статус заказа напрямую — только команды
  (`POST /commands`), из которых клиенту доступна ровно одна: `ORDER_COMPLETED`
  (из `PROCESSING`).
- Синхронизация — инкрементальный поллинг по `afterSeq`; push (FCM) — только сигнал
  «есть обновление», не источник данных (push — пост-MVP).
- Роль приложения на сервере — `CLIENT` (в отличие от `OPERATOR`/`ADMIN`).

---

## 2. Технологический стек

Версии — только в [`gradle/libs.versions.toml`](../gradle/libs.versions.toml).

| Назначение | Технология |
|---|---|
| Язык | Kotlin 2.4.10, JVM target 17 (Android/JVM) |
| UI | Compose Multiplatform 1.11.1, Material 3 (`1.11.0-alpha07`) |
| Таргеты | `android`, `iosArm64`, `iosSimulatorArm64`; Web/JS/Wasm удалены |
| Сборка | Gradle 9.6.0, AGP 9.4.0, Kotlin DSL, version catalog; daemon JVM 21 |
| Навигация/компоненты | Decompose 3.5.0 + Essenty 2.5.0 |
| DI | Koin 4.2.2 (core; в Android — koin-android) |
| Сеть | Ktor Client 3.5.1: core, content-negotiation, auth, logging; OkHttp / Darwin |
| Сериализация | kotlinx-serialization-json 1.11.0 |
| Многопоточность | kotlinx-coroutines 1.11.0 |
| БД | SQLDelight 2.3.2 (android-driver / native-driver, coroutines-extensions) |
| Время | kotlinx-datetime 0.8.0 (`Instant`/epoch millis) |
| Токены/настройки | KSafe 2.2.1 (Keystore/Keychain) |
| Изображения | Coil 3.5.0 |
| Списки | kotlinx-collections-immutable 0.4.0 |
| Аудио/фото | native expect/actual: Android MediaRecorder/MediaPlayer, iOS AVAudioRecorder/AVAudioPlayer, Photo Picker/PHPicker; native fallback после оценки KMP, см. `DEBT.md` |
| Деньги | собственный `Money` (минорные единицы `Long`, парсинг decimal-строки) |

Ограничения версий:

- `androidx-lifecycle = 2.11.0-beta01`: стабильная требует `compileSdk 37`, поэтому пока beta.
- `material3` — alpha-версия из каталога Compose Multiplatform; не поднимать без необходимости.

---

## 3. Структура модулей

```
/build.gradle.kts, /settings.gradle.kts   — корневая сборка, плагины apply false
/gradle/libs.versions.toml                 — version catalog (единственное место версий)

/shared            — весь код и UI
  src/commonMain    — фичи, core/, di/, services/, composeResources/, SQLDelight-схема
  src/androidMain   — actual-реализации и платформенные фабрики
  src/iosMain       — actual-реализации, MainViewController (вход для iOS)
  src/commonTest, src/androidHostTest, src/iosTest — smoke-тесты

/androidApp        — тонкая Android-обёртка (ChatShopApp → MainActivity → App(root))
/iosApp            — Xcode-обёртка (iosApp.xcodeproj, статический framework Shared)

/docs              — проект, архитектура, правила, API-контракт, техдолг
```

Package/namespace — `com.al4apps.chatshop`; shared namespace `com.al4apps.chatshop.shared`;
iOS bundle id `com.al4apps.chatshop.ChatShop`.

Пакеты `shared/src/commonMain/kotlin/com/al4apps/chatshop/`:

- `root/` — `RootComponent` (Onboarding | Auth | Home) и `RootContent`.
- `core/` — `arch` (CoroutineFeature, StateStore, Assembler), `ui` (UiText), `design`
  (тема, иконки, виджеты), `network` (HttpClientFactory, Auth-plugin с refresh, AppError),
  `db` (DatabaseFactory), `session` (TokenStorage, SessionManager), `settings`
  (AppPreferences), `media` (ImagePicker, AudioRecorder, AudioPlayer, сжатие, FileStore),
  `util` (Money, форматирование времени), `platform`, `coroutines`.
- `features/` — `onboarding`, `auth` (phone, code, data, domain), `home`,
  `chats` (`list`, `dialog`, `firstorder`, `data`, `domain`, `data/test`), `example`
  (dev-витрина дизайн-системы без входа из прод-UI).
- `services/` — `sync`, `outbox`, `files`, `testmode`.
- `di/` — `AppConfig`, `AppModules`, `InitKoin`, `PlatformModule` (expect/actual).

---

## 4. Архитектурные принципы

Детали — [`PLAN.md`](PLAN.md), [`CODING_RULES.md`](CODING_RULES.md).

- Clean Architecture: domain / use cases / repository abstraction / data / presentation.
  Domain не зависит от платформы.
- Component-based + Decompose: состояние и навигация — в компоненте, Compose — рендер
  состояния. Навигация только через компоненты, не внутри UI.
- **БД — единственный источник истины для UI**: API → DTO → mapper → SQLDelight
  (транзакционный upsert) → `Flow` из БД → repository → interactor → component
  (`vmState` → `uiState`) → Compose.
- Shared UI — приоритет; platform-specific код — только при объективной необходимости
  (permissions, camera, файлы, аудио, push).
- Один цикл разработки = одна согласованная фича: план → апрув → реализация → отчёт.
- Библиотеки, архитектура, публичные API и структура проекта не меняются без согласования.

---

## 5. Текущий статус

- [x] Инициализация: KMP/Compose Multiplatform каркас (Android + iOS), JVM target 17,
      `.gitignore`, ветка `develop`.
- [x] Этап 0 (каркас): Web удалён; Decompose + Koin + coroutines/serialization/datetime;
      core/arch (CoroutineFeature, StateStore, Assembler), core/ui (UiText); Root-стек
      Onboarding → Auth → Home (заглушки); entrypoints Android (`retainedComponent`) и iOS
      (`ApplicationLifecycle`). Проверен запуск и навигация на устройстве.
- [x] Этап 1 (дизайн-система): core/design — AppTheme (белый/синий), AppButton,
      AppTextField, AppTopBar, AppIconButton, AppIcons (свои вектора); витрина
      `features/example`; UiText сериализуем (ключ ресурса), ButtonUiState.
- [x] Этап 2 (авторизация): Ktor + KSafe; core/network (HttpClientFactory, Bearer +
      refresh под Mutex, problem+json → AppError, apiCall → AppResult); core/session
      (TokenStorage/KSafe, SessionManager); фича auth: Phone → Code (vmState через
      StateStore, Assembler, маска `+992`, таймер повтора 60 с, автоотправка 6 цифр);
      Root: старт по сессии, разлогин по `SessionState.Unauthorized`.
- [x] Этап 3 (онбординг): 3 слайда (HorizontalPager, точки, Далее/Начать), флаг показа
      в `core/settings/AppPreferences` (KSafe `prefs`, Plain; секреты — отдельный `vault`);
      Root: Home → Auth → Onboarding по сессии и флагу.
- [x] Этап 4 (БД): SQLDelight — `dialogs`/`messages`/`files` (диалект старый: upsert =
      insertOrIgnore + update в транзакции), DatabaseFactory (expect/actual), `Money`
      (минорные единицы), доменные модели и репозитории чатов (Flow из БД,
      замена локальных сообщений по `clientMessageId`), `AppDispatchers.io`.
- [x] Этап 5 (sync): `services/sync` — SyncApi (GET dialogs/messages), DTO + мапперы
      (битые записи пропускаются), SyncService (диалоги 12 с / активный диалог 4 с по
      `afterSeq` с пагинацией; только `Authorized` + foreground; сигналы
      onDialogOpened/Closed, foreground через retained lifecycle Root).
- [x] Этап 6 (список чатов): Home — стек Bootstrap → ChatList | FirstOrder | Dialog(id);
      Bootstrap: syncDialogsOnce → hasDialogs → роутинг. ChatList: список из БД,
      «+» создаёт диалог (ChatsInteractor → POST /dialogs → кэш → push Dialog).
      DTO — в `features/chats/data/dto`; иконка Plus; `core/util/InstantFormat`.
- [x] Этап 7 (диалог + outbox): экран диалога (лента из БД, пузыри мой/чужой/SYSTEM,
      автоскролл, ввод и отправка); MessagesRepository.enqueueText/observePending/mark*
      (очередь в `messages.sq`); MessagesInteractor (UUID `clientMessageId`); OutboxService
      (PENDING → доставка по одному, upsert по `clientMessageId` без дублей, ретраи с
      backoff → FAILED, resetStuckSending на старте); ручной повтор тапом; блок ввода
      при `CLOSED`.
- [x] Этап 8 (первый заказ): FirstOrder — описание, крупная кнопка микрофона, поле ввода;
      первая отправка → createDialog → sendText → Home replaceAll(ChatList, Dialog)
      (back ведёт в список). Общий `MessageInputBar` вынесен в design/widgets.
- [x] Этап 9a (отправка изображений): Coil; core/media — ImagePicker (Android
      PickVisualMedia через мост Activity; iOS — PHPickerViewController), compressImageToJpeg
      (≤ 5 МБ), FileStore (expect/actual). ChatsApi.sendImage (multipart),
      MessagesInteractor.sendImage, OutboxService ветка IMAGE (+ привязка своих байтов к
      серверному `file.id`), FilesRepository. UI: скрепка, пузырь-картинка (Coil из
      localPath), заглушка DELETED.
- [x] Этап 9b (входящие изображения): `files.remoteUrl` (эфемерный, миграция `1.sqm`),
      FileDownloadService (событийный, отдельный клиент без Auth — presigned GET, кэш по
      `file.id` → localPath), заглушка DELETED, полноэкранный просмотр (vmState + перехват
      «назад» через BackCallback).
- [x] Этап 10a (запись голосовых): core/media — AudioRecorder (Android MediaRecorder,
      iOS AVAudioRecorder → AAC/M4A; разрешение микрофона на обеих платформах),
      VoiceRecordingController (таймер, авто-стоп 120 с). ChatsApi.sendVoice
      (multipart + durationSec), OutboxService ветка VOICE, MessagesInteractor.sendVoice,
      SQL insertOutgoingVoice. UI: микрофон в MessageInputBar, запись-бар, большая кнопка
      FirstOrder.
- [x] Этап 10b (прослушивание голосовых): core/media/AudioPlayer (Android MediaPlayer,
      iOS AVAudioPlayer) — один плеер на приложение, StateFlow (playingId/isPlaying/
      positionSec), toggle/stop. DialogComponent: 5-й flow плеера в combine, onVoiceClick,
      стоп при уходе с экрана. Пузырь VOICE (play/pause + прогресс + таймер), заглушки
      «скачивается»/DELETED. Входящие голосовые качает FileDownloadService.
- [x] Dev (вне этапов): тестовый режим без бэкенда, выбор **только через DI**:
      `di/AppConfig(testMode, apiBaseUrl)` создаётся entrypoint из Android
      `BuildConfig.DEBUG` / iOS `Info.plist`; `appModules` подставляет
      `productionDataModule` или `testDataModule`. Debug использует test mode и
      dev-хост; release — production URL и test mode off. Без рантайм-переключения:
      `AuthApi`/`ChatsApi`/`SyncApi` — интерфейсы с парой реализаций `Default*` / `Test*`.
      В тест-модуле: мок-auth (любой номер/код), `TestChatsApi`/`TestSyncApi` над
      `TestChatsStore` (in-memory «сервер»), `InMemoryTokenStorage`, `TestDataInitializer`
      (очистка БД + reset мок-стора на старте), `TestAutoReplyService` (авто-ответ
      оператора).
- [x] Этап 11 (статусы заказа): SYSTEM-события заказа по `eventType`/`payload` —
      карточки `ORDER_CREATED`/`PAID`/`PROCESSING`/`COMPLETED`/`CANCELLED`, плашки
      `OPERATOR_*`, карточка `DIALOG_CLOSED`; кнопка «Подтвердить получение» внутри
      `PROCESSING` → `POST /dialogs/{id}/commands` (`ORDER_COMPLETED`), снапшот в БД;
      order strip и бейджи статуса в шапке; список чатов с номером, описанием, бейджем
      и суммой; закрытый футер вместо ввода; обработка `403`/`409` без авторетрая.
      Тесты: `Money`, `ChatListAssembler`, `DialogAssembler`, `TestChatsStore`,
      `TestChatsApi`, `DefaultChatsInteractor`; ручная матрица Android.

### Известные ограничения

- iOS media: выбор фото и запись голоса реализованы и проверены на симуляторе;
  на физическом iOS-устройстве не проверялись. Плеер на iOS реализован.
- Happy-path авторизации не прогнан с реальным бэкендом (проверены UI, валидация,
  обработка ошибок; verify/refresh — компиляцией и логикой).
- Release-сборка требует `API_BASE_URL_RELEASE` (HTTPS); фактический production URL
  не хранится в репозитории. Debug использует test mode и dev-хост, cleartext разрешён
  только в debug-manifest.
- Тесты покрывают `Money`, assemblers, тестовый store/API и команду заказа; UI
  проверяется ручной матрицей на Android.

---

## 6. Документация

- [`../AGENTS.md`](../AGENTS.md) — инструкции для агентов и быстрый вход.
- [`README.md`](README.md) — индекс документации и приоритет источников.
- [`PLAN.md`](PLAN.md) — архитектурный план, этапы, схема БД, дерево компонентов.
- [`CODING_RULES.md`](CODING_RULES.md) — обязательные правила кода.
- [`AGENT_ROLE.md`](AGENT_ROLE.md) — роль, экспертиза и процесс работы.
- [`CLIENT_GUIDE.md`](CLIENT_GUIDE.md) — семантика API (дополнение к OpenAPI).
- [`openapi.yaml`](openapi.yaml) — OpenAPI-контракт ChatShop API.
- [`DEBT.md`](DEBT.md) — реестр технического долга.
