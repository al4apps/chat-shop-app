# AGENTS.md

ChatShop — клиент chat-commerce: переписка с оператором, а заказ живёт как event-log
(`SYSTEM`-сообщения + проекция на `Dialog`). Kotlin Multiplatform + Compose Multiplatform
(общий UI), **только Android и iOS**. Web/JS/Wasm удалён. Рынок — Таджикистан (`+992`,
9 цифр), язык — русский.

## Документация

- `docs/README.md` — индекс и приоритет источников.
- `docs/PROJECT.md` — описание, стек, структура, текущий статус.
- `docs/PLAN.md` — архитектура, этапы, схема БД, дерево компонентов.
- `docs/CODING_RULES.md` — обязательные правила кода.
- `docs/AGENT_ROLE.md` — роль и процесс работы.
- `docs/CLIENT_GUIDE.md` — семантика API (на § ссылаются комментарии в коде).
- `docs/openapi.yaml` — OpenAPI-контракт (это не конфиг приложения).
- `docs/DEBT.md` — осознанные компромиссы; читать перед релизом.
- `docs/BACKLOG.md` — известные дефекты, исследования и будущие задачи.

`opencode.json` подключает `docs/CODING_RULES.md` как дополнительные инструкции.

## Ответственность за продукт

- Продукт — клиент chat-shop: мессенджер с `SYSTEM`-сообщениями; платежей в приложении нет.
- Ты ведёшь архитектуру, качество, roadmap и документацию до конца, а не только выполняешь
  постановку. Куратор смотрит верхнеуровнево.
- Сначала оцени стабильную KMP-библиотеку; native/expect-actual — только с обоснованием
  и записью в `docs/DEBT.md` или `docs/BACKLOG.md`.
- Новые дефекты и будущие задачи фиксируй в `docs/BACKLOG.md`, а не только в переписке.

## OpenSpec

Проект использует spec-driven development. Поведение системы описывается в `openspec/specs/`:

- `openspec/specs/<capability>/spec.md` — baseline-спеки (источник истины для поведения).
- Новое поведение и изменения оформляются через `openspec/changes/<change>/`, а не правкой main specs напрямую.
- Команды: `/opsx-propose` — план изменения, `/opsx-apply` — реализация, `/opsx-archive` — архивация.
- Проверка: `openspec validate --specs` и `openspec validate --changes`.
- Спеки пишутся на русском; структурные заголовки и `SHALL`/`WHEN`/`THEN` остаются на английском.

## Сборка и проверки

Toolchain: Gradle daemon JVM 21 (авто-провижининг), Kotlin/JVM target 17, Gradle 9.6.0,
AGP 9.4.0, Kotlin 2.4.10; `compileSdk = 36`, `minSdk = 24`. Путь к Android SDK — `local.properties`
(`sdk.dir`) или `ANDROID_HOME`.

```bash
./gradlew :androidApp:assembleDebug          # debug APK
./gradlew :androidApp:installDebug           # установка на устройство/эмулятор
adb shell am start -n com.al4apps.chatshop/.MainActivity
```

Тесты:

```bash
./gradlew :shared:testAndroidHostTest        # commonTest + androidHostTest на JVM
./gradlew :shared:iosSimulatorArm64Test      # нужен Xcode / iOS Simulator
./gradlew :shared:allTests                   # все таргеты
./gradlew :shared:testAndroidHostTest --tests "com.al4apps.chatshop.SharedLogicAndroidHostTest"
```

- AGP 9.4 использует новый Android-KMP плагин: задача называется `:shared:testAndroidHostTest`,
  а не `testDebugUnitTest`.
- iOS: сборка и запуск на симуляторе — `make ios` (или `make ios-build` / `make ios-run`);
  конкретный симулятор — `make ios SIM_UDID=<udid>`. Gradle-задачи:
  `./gradlew :shared:compileKotlinIosSimulatorArm64` или
  `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`; Xcode-проект — `iosApp/iosApp.xcodeproj`,
  framework статический, `baseName = "Shared"`.
- CI, ktlint/detekt, formatter, pre-commit и `verifyMigrations` **не настроены**. Не выдумывай
  отдельный lint-gate: минимальная проверка — сборка + тесты.
- Тесты покрывают `Money`, assemblers, тестовый store/API и команду заказа; UI проверяется
  ручной матрицей на Android.

## Модули и entrypoints

- `shared` — весь код и UI.
  - `commonMain` — фичи, `core/`, `di/`, `services/`, `composeResources/`, SQLDelight-схема.
  - `androidMain` / `iosMain` — только `actual` и платформенные реализации.
  - `commonTest` / `androidHostTest` / `iosTest` — smoke-тесты.
- `androidApp` — тонкая обёртка: `ChatShopApp` вызывает `initKoin { androidContext(...) }`,
  `MainActivity` создаёт `DefaultRootComponent` через `retainedComponent` и рендерит `App(root)`.
- `iosApp` — Xcode-обёртка; `shared/src/iosMain/.../MainViewController.kt` один раз стартует Koin
  и создаёт `DefaultRootComponent` с `ApplicationLifecycle`.
- Koin-граф — `di/AppModules.kt`; платформенные зависимости — `expect val platformModule`
  и `PlatformModule.android.kt` / `PlatformModule.ios.kt`.

## Архитектурные инварианты

- Навигация — только Decompose-компоненты: `DefaultRootComponent` → `Onboarding | Auth | Home`.
  Home: `Bootstrap` → `FirstOrder` (диалогов нет) | `ChatList` | `Dialog(id)`.
- Поток данных: API → DTO → mapper → SQLDelight (транзакционный upsert) → `Flow` из БД →
  repository → interactor → component (`vmState` → `uiState`) → Compose.
  **БД — единственный источник истины для UI**; UI не показывает данные из сети напрямую.
- `services/sync/SyncService` — только приём: список диалогов раз в 12 с, открытый диалог раз
  в 4 с по `afterSeq`; работает только при `Authorized` + foreground, на resume — немедленный pull.
- `services/outbox/OutboxService` — только отправка: PENDING-записи из БД, по одной в рамках
  диалога; до 3 сетевых попыток с backoff, затем FAILED; ответ сервера заменяет локальную
  строку по `clientMessageId`.
- `services/files/FileDownloadService` — presigned URL качается отдельным `HttpClient` **без**
  `Authorization`; кэш по `file.id`. `file.url` временный, постоянной ссылкой не считается.
- Компоненты живут в `Root`/`Home`/`Auth`/`ChatList`/`Dialog`/`FirstOrder`; состояние — в
  компоненте, Compose — stateless-рендер.

## Тестовый режим без бэкенда

`di/DiConfig.kt` → `TEST_MODE` сейчас **`true`**: `testDataModule` подменяет API моками,
принимает любой телефон/код, сбрасывает БД и наполняет её тестовыми данными, авто-ответы
оператора работают. Для реального бэкенда поставь `false`:

- Android-эмулятор: `http://10.0.2.2:8080/api/v1`
- iOS-симулятор: `http://localhost:8080/api/v1`

Хосты захардкожены в `PlatformModule.android.kt` / `.ios.kt`, cleartext HTTP разрешён глобально.
Код тест-режима попадает в release-сборку — перед релизом проверить флаг (см. `docs/DEBT.md`).
Тест-режим при старте **очищает БД** (`TestDataInitializer`) — не удивляйся пропавшим данным.

## Правила кода, которые легко нарушить

- Один класс — один файл. Компонент: `interface` + `Default*`-реализация (`PreviewImpl` — только
  по указанию). `vmState`/`uiState` — в пакете `.model`; `StateStore` + `stateKeeper` для vmState;
  `Assembler` — отдельный класс-параметр; в каждом Default-компоненте — `CoroutineFeature`.
- Compose — stateless/state hoisting; списки — `ImmutableList`; при необходимости `@Stable`.
- Строки только через `UiText` + compose resources
  (`shared/src/commonMain/composeResources/values/strings.xml`, сейчас только `ru`).
  Хардкод текста запрещён. В Kotlin-коде ставь trailing commas.
- Деньги — decimal-строка → `Money`; **никогда `Double`/`Float`**. Время — `Instant`/epoch millis.
- Platform-specific зависимости не протаскивать в `commonMain`; создавать их на краю приложения
  и передавать через интерфейс/фабрику.
- Версии — только в `gradle/libs.versions.toml`. Не поднимай без нужды
  `androidx-lifecycle = 2.11.0-beta01` (стабильная требует `compileSdk 37`) и alpha-версию
  `material3`.
- SQLDelight-схема и запросы — `shared/src/commonMain/sqldelight/com/al4apps/chatshop/db/`.
  Диалект старый (Android API 24, SQLite < 3.24): **`UPSERT` нельзя**, используй
  `insertOrIgnore` + `update` в одной транзакции. Изменения схемы сопровождай новым `.sqm`
  в `migrations/` (сейчас есть `1.sqm`: `files.remoteUrl`) и проверяй миграцию на уже
  установленной БД.
- Методы интерфейсов и интеракторов документируй блоком: цель, поведение, возвращаемое значение.

## Правила API/данных

- Роль приложения — `CLIENT`. Из команд заказа клиенту доступна только `ORDER_COMPLETED`
  (из `PROCESSING`); статусы двигает сервер.
- `POST /messages` — только `TEXT`. `SYSTEM`/`IMAGE`/`VOICE` через него не отправлять;
  файлы — через `POST /files` (multipart).
- У каждого исходящего сообщения/файла должен быть `clientMessageId`; ретраи — с тем же id.
- Лента запрашивается инкрементально по `afterSeq`, не целиком; `lastSyncedSeq` двигается только вперёд.
- В `CLOSED`-диалог писать нельзя (`409`); UI блокирует ввод.
- `file.url` не сохранять как постоянную ссылку; кэш — по `file.id`, протухший url получать
  повторным запросом ленты.
- Ошибки — `application/problem+json` → `AppError`; `detail` можно показывать пользователю.
- Push (FCM) — пост-MVP и только сигнал «есть обновление», не источник данных.

## Известные ограничения

- iOS: выбор фото (`IosImagePicker` → null) и запись голоса (`IosAudioRecorder.isAvailable = false`) —
  заглушки; iOS-плеер реализован. Android-медиа реализованы полностью.
- Happy-path авторизации не прогнан с реальным бэкендом (проверены UI, валидация, обработка ошибок).
- `TEST_MODE = true` и dev-хосты/cleartext — см. `docs/DEBT.md` перед релизом.

## Процесс

- Одна итерация = одна фича. Перед реализацией — краткий план (модули/source sets/компоненты/
  use cases/риски) и **ждать апрув**. Не переходить к следующей фиче без приёмки текущей.
- Коммит — только по прямому указанию. Не делать несвязанный рефакторинг. Смена архитектуры,
  публичных API, структуры проекта или библиотек — только после согласования.
- После фичи — структурированный отчёт: что изменено, где хранится state, поток данных,
  навигация, риски/что проверить на ревью. Значимые решения фиксировать в `docs/PROJECT.md`,
  компромиссы — в `docs/DEBT.md`.
- Общение с пользователем — на русском.
