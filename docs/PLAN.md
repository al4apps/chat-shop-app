# ChatShop — архитектурный план разработки

> Основной документ, по которому идёт вся разработка. Изменения — только по согласованию
> с тимлидом. Каждый этап = одна итерация: план этапа → апрув → реализация → отчёт → ревью.
>
> Текущий статус этапов — [`PROJECT.md`](PROJECT.md) §5. Контракт с бэкендом —
> [`CLIENT_GUIDE.md`](CLIENT_GUIDE.md). Правила — [`CODING_RULES.md`](CODING_RULES.md).

Принятые решения (согласованы 2026-07-19):
- **Веб исключён**: модуль `webApp` и таргеты `js`/`wasmJs` удалены. Только Android + iOS.
  Domain/presentation держим без платформенных зависимостей.
- **Онбординг** — до авторизации, 2–4 интро-слайда, показывается один раз (флаг в хранилище).
- **Push (FCM)** — этап после MVP. MVP живёт на поллинге (он обязателен по контракту в любом случае).
- **Локализация** — только русский.

---

## 1. Технологический стек (библиотеки — фиксируются на этапе 0)

| Назначение | Библиотека | Комментарий |
|---|---|---|
| Навигация/компоненты | Decompose + extensions-compose (+ Essenty: lifecycle, stateKeeper, instanceKeeper, backHandler) | компонентная навигация по правилам проекта |
| DI | Koin (core) | ручная передача в компоненты через конструкторы, Koin — на уровне сборки графа |
| Многопоточность | kotlinx-coroutines | |
| Сериализация | kotlinx-serialization-json | DTO + payload SYSTEM-событий + vmState |
| Сеть | Ktor Client: core, content-negotiation, kotlinx-json, logging; engine OkHttp (Android) / Darwin (iOS) | |
| БД | SQLDelight 2.3.2: android-driver, native-driver, coroutines-extensions | единый источник истины для UI |
| Время | kotlinx-datetime (+ kotlin.time.Instant) | парсинг ISO-8601, форматирование |
| Токены | KSafe (шифрованное KMP-хранилище: Keystore/Keychain) | access/refresh, флаг онбординга — там же или Settings |
| Изображения | Coil 3 (compose multiplatform) | показ картинок из локального кэша/файлов |
| Аудио запись/плеер | без библиотек: expect/actual (MediaRecorder / AVAudioRecorder, MediaPlayer / AVAudioPlayer) | |
| Выбор фото | без библиотек: expect/actual (Android Photo Picker / iOS PHPicker) | |
| Деньги | без библиотек: `Money` value class (минорные единицы `Long`, парсинг decimal-строки) | Double запрещён контрактом |

Версии пиню на этапе 0 (последние стабильные, совместимые с Kotlin 2.4.10 / AGP 9.4.0).

---

## 2. Структура модулей и пакетов

Модули: `shared` (весь код), `androidApp`, `iosApp` — тонкие обёртки. `webApp` удалён.

```
shared/src/commonMain/kotlin/com/al4apps/chatshop/
├── root/                    # RootComponent + DefaultRootComponent (Onboarding | Auth | Home)
├── core/
│   ├── arch/                # CoroutineFeature, StateStore (vmState + stateKeeper), базовый Assembler
│   ├── ui/                  # UiText, общие compose-утилиты
│   ├── design/              # дизайн-система: theme/ (Color, Type, Shape, AppTheme), widgets/
│   ├── network/             # HttpClientFactory, AuthPlugin (Bearer + 401-refresh c Mutex),
│   │                        # ProblemJson → AppError, базовые DTO-утилиты
│   ├── db/                  # DatabaseFactory (expect/actual драйвер), транзакционные хелперы
│   ├── session/             # SessionManager (Authorized/Unauthorized Flow), TokenStorage
│   └── util/                # Money, форматирование времени, Uuid
├── features/
│   ├── onboarding/          # ui/, model/
│   ├── auth/                # ui/ (телефон, код), domain/ (AuthInteractor), data/, model/
│   ├── home/                # HomeComponent — контейнер и роутинг всего после авторизации
│   └── chats/
│       ├── domain/          # доменные модели (Dialog, Message, OrderStatus, DialogEvent...),
│       │                    # интеракторы: DialogsInteractor, MessagesInteractor, OrderInteractor (этап 11)
│       ├── data/            # repositories (impl над БД+API), DTO, мапперы
│       ├── list/            # компонент «Список чатов»: ui/, model/
│       ├── dialog/          # компонент «Диалог»: ui/ (лента, ввод, плашки), model/
│       └── firstorder/      # компонент «Первый заказ» (особый экран): ui/, model/
├── services/                # инфраструктурные сервисы данных (не UI)
│   ├── sync/                # SyncService: поллинг → БД (диалоги 12с / активный 4с)
│   ├── outbox/              # OutboxService: PENDING-записи БД → отправка → SYNCED
│   └── files/               # FileDownloadService: presigned url → кэш по file.id
└── di/                      # koin-модули: core, network, db, session, chats, services

shared/src/androidMain / iosMain — только actual-реализации:
  db-драйвер, аудиозапись/плеер, фотопикер, пути файлового кэша, платформенные фабрики.
```

Правила оформления компонентов — по `CODING_RULES.md`: interface + DefaultImpl, класс = файл,
`uiState`/`vmState` в `.model`, Assembler отдельным классом-параметром, `CoroutineFeature`,
vmState Serializable через StateStore/stateKeeper.

---

## 3. Поток данных (ключевая схема)

**БД — единственный источник истины для UI.** UI никогда не показывает данные «из сети напрямую».

Приём:
```
SyncService (поллинг afterSeq / список диалогов)
  → API (Ktor) → DTO → маппер → upsert в БД (транзакция)
  → SQLDelight Flow → Repository → Interactor → Component (vmState → uiState) → Compose
```

Отправка (outbox):
```
UI intent → Component → MessagesInteractor
  → INSERT в messages (sendState = PENDING, clientMessageId = UUID)   ← UI мгновенно видит из БД
OutboxService (подписан на PENDING):
  → отправка (POST /messages | /files, с clientMessageId), последовательно в рамках диалога
  → успех: замена локальной строки серверным сообщением (тот же clientMessageId → seq)
  → ошибка: sendState = FAILED (+ retry с backoff; ручной повтор из UI тем же clientMessageId)
```

Разделение ответственности сервисов:
- **SyncService** — только приём и кэширование (ничего не отправляет).
- **OutboxService** — только отправка (ничего не поллит).
- **FileDownloadService** — только файлы: кэш по `file.id`, url — одноразовый; протух → перезапросить ленту.
- Живут в app-scope CoroutineScope (Koin singleton), ритм управляется сигналами от компонентов
  (`dialogOpened(id)` / `dialogClosed()` / `appForeground/Background`).

---

## 4. Схема БД (SQLDelight, черновик — уточняется на этапе 4)

```sql
dialogs(
  id TEXT PK, status TEXT, orderStatus TEXT NULL, orderAmount TEXT NULL,
  orderDescription TEXT NULL, orderNo INTEGER, lastMessageAt INTEGER NULL,
  lastMessageSeq INTEGER NULL, createdAt INTEGER, hasOperator INTEGER,
  lastSyncedSeq INTEGER NOT NULL DEFAULT 0            -- lastSeq поллинга этого диалога
)

messages(
  localId INTEGER PK AUTOINCREMENT,                   -- стабильный ключ до получения seq
  seq INTEGER NULL UNIQUE,                            -- null у неотправленных
  dialogId TEXT, clientMessageId TEXT NULL UNIQUE,    -- у исходящих
  senderRole TEXT, type TEXT, text TEXT NULL,
  eventType TEXT NULL, payload TEXT NULL,             -- json SYSTEM-события
  createdAt INTEGER,
  sendState TEXT NOT NULL DEFAULT 'SYNCED',           -- SYNCED | PENDING | SENDING | FAILED
  fileId TEXT NULL
)

files(
  id TEXT PK, mime TEXT, size INTEGER, durationSec INTEGER NULL,
  status TEXT,                                        -- ACTIVE | DELETED
  localPath TEXT NULL,                                -- путь в кэше (или исходник исходящего)
  remoteUrl TEXT NULL,                                -- эфемерный presigned url (миграция 1.sqm)
  updatedAt INTEGER
)
```

Актуальная схема и запросы — `shared/src/commonMain/sqldelight/com/al4apps/chatshop/db/*.sq`.
Деньги хранятся строкой как на проводе, парсятся в `Money` при маппинге. Время — epoch millis.
Изменения схемы — только с новым `.sqm` в `migrations/` (текущая миграция: `1.sqm` — `files.remoteUrl`).

---

## 5. Дерево компонентов (Decompose)

```
DefaultRootComponent (childStack)
├── Onboarding            — первый запуск (флаг не выставлен), до авторизации
├── Auth                  — нет сессии: экран телефона → экран кода
└── Home (DefaultHomeComponent, childStack)
    ├── Bootstrap         — короткое состояние: первичная синхронизация списка диалогов
    ├── FirstOrder        — список чатов пуст: особый экран первого заказа
    │                       (описание сервиса, большая кнопка аудиозаписи, текстовое поле снизу;
    │                        диалог создаётся при первой отправке → replace на Dialog)
    ├── ChatList          — список чатов не пуст: стартовый экран
    └── Dialog(id)        — экран диалога (лента + ввод)
```

Роутинг Home: после авторизации — синхронизация списка → пусто → `FirstOrder`, иначе `ChatList`.
Возврат из `Dialog` — в `ChatList`. Дублей в стеке не допускаем.

Сессия: `SessionManager` выдаёт Flow состояния; refresh невозможен → Root переключает на Auth.

---

## 6. Дизайн-система (принципы)

- Стиль: простота и чистота, белый фон, синий — единственный акцент, серые — вспомогательные.
  Без сложных цветов и градиентов. Одно основное действие на экран.
- Светлая тема (тёмная — вне MVP). Системные шрифты (SF / Roboto).
- Палитра (стартовая, уточняется на этапе 1): Primary #1A73E8 (синий), фон #FFFFFF,
  поверхность #F5F7FA, текст #1A1C1E / вторичный #6B7280, ошибки — стандартный красный M3.
- Базовые компоненты этапа 1: `AppButton` (primary/secondary/text), `AppTextField`,
  `AppTopBar`, `AppIconButton`, стили текста. Дополняется по мере фич
  (MessageBubble, SystemEventCard, RecordButton, AudioPlayerBar, StatusBadge...).
- Все строки — через UiText + compose-ресурсы (ru), хардкод запрещён.

---

## 7. Этапы разработки (один этап = одна итерация с апрувом)

Статус: этапы 0–10b и dev-тест-режим выполнены; этап 11 не начат (детали — [`PROJECT.md`](PROJECT.md) §5).

| # | Этап | Содержание | Результат |
|---|---|---|---|
| 0 | Каркас | Удаление веба (webApp, js/wasmJs, jsMain/wasmJsMain/webTest); подключение Decompose, Koin, coroutines, serialization, datetime; core/arch (CoroutineFeature, StateStore, UiText); DefaultRootComponent с заглушками; интеграция в Android/iOS entrypoints | приложение собирается и запускается на обеих платформах со стековой навигацией-заглушкой |
| 1 | Дизайн-система | Тема (цвета/типографика/формы), базовые виджеты, previews | core/design готов к использованию фичами |
| 2 | Авторизация | core/network (Ktor, AuthPlugin, problem+json), TokenStorage (KSafe), SessionManager; экраны телефона и кода (таймер повтора, 429); роутинг Root по сессии | рабочий вход по SMS, автологин по refresh |
| 3 | Онбординг | Слайды, флаг «показан», встраивание в Root до Auth | одноразовый онбординг |
| 4 | БД | SQLDelight: схема, DatabaseFactory (expect/actual), репозитории Dialogs/Messages с Flow | data-слой чатов на БД |
| 5 | Sync-сервис | Поллинг списка диалогов и активного диалога по afterSeq, upsert, SyncService, догрузка при возврате в foreground | данные сервера появляются в БД |
| 6 | Список чатов | ChatListComponent + UI (подписка на БД), создание нового чата, Bootstrap-роутинг Home (пусто → FirstOrder) | стартовый экран с реальными данными |
| 7 | Диалог: текст | DialogComponent + UI (лента: TEXT + SYSTEM-плашки текстом, ввод); OutboxService: отправка TEXT (clientMessageId, optimistic, FAILED + повтор) | полноценная текстовая переписка |
| 8 | Первый заказ | FirstOrderComponent + UI (описание, большая кнопка аудио — пока неактивна, текстовое поле); создание диалога при первой отправке, переход в Dialog | сценарий первого действия |
| 9 | Изображения | Фотопикер (expect/actual), сжатие в jpeg ≤5МБ, отправка через outbox (multipart), FileDownloadService, показ в ленте (Coil), простой полноэкранный просмотр | сценарий изображений |
| 10 | Аудио | Запись (AAC/m4a, ≤120с, durationSec), RecordButton UX, отправка через outbox, плеер в ленте (прогресс/пауза), кэш; активация большой кнопки FirstOrder | сценарий аудиосообщений |
| 11 | Статусы заказа | Карточки SYSTEM-событий по eventType/payload (оферта ORDER_CREATED, PAID, PROCESSING с кнопкой «Подтвердить получение» → команда ORDER_COMPLETED, COMPLETED/CANCELLED, DIALOG_CLOSED — блокировка ввода, OPERATOR_*); бейджи статуса в списке и шапке диалога | сценарий просмотра статусов |
| 12 | Стабилизация MVP | Единая обработка ошибок/empty states, оффлайн-индикация, ретраи, прогон всех сценариев на обеих платформах, чистка DEBT.md | готовый MVP |
| 13+ | Пост-MVP | Push (FCM): регистрация устройства, pull-по-сигналу; далее — по плану тимлида | |

Порядок 2↔3, 9↔10 можно переставить без вреда; менять — по согласованию.

---

## 8. Ключевые инженерные решения (фиксация)

- **Идемпотентность**: каждый исходящий — `clientMessageId` (UUID) один раз при создании записи
  в БД; ретраи тем же id; сопоставление серверного ответа по нему.
- **401**: единый Ktor-plugin, refresh под Mutex, параллельные 401 ждут общий refresh;
  rotating refresh — новая пара сохраняется атомарно.
- **Поллинг**: только `afterSeq` (никогда всю ленту); открытый диалог 3–5с, список 10–15с,
  в фоне — стоп; на foreground — немедленный pull.
- **file.url временный**: никогда не хранится как постоянный; кэш по `file.id`; протухший url →
  перечитать ленту; `status=DELETED` → заглушка «файл удалён».
- **Деньги** — только `Money` из decimal-строки; **время** — `Instant`/epoch millis.
- **Ошибки** — доменная модель `AppError` из problem+json; `detail` показываем пользователю.
- **Аудиоформат** — AAC/M4A на обеих платформах (кросс-платформенное воспроизведение).
- **Логика по SYSTEM-событиям** — только `eventType` + `payload` + проекция; `text` — для показа.

## 9. Риски

- Alpha-версии material3 из wizard-каталога — при проблемах откат на стабильную связку CMP.
- Совместимость SQLDelight/KSafe/Coil с Kotlin 2.4.10 — проверяется на этапе 0/4, при
  конфликте — согласованная замена (например, KSafe → multiplatform-settings + crypto).
- iOS-специфика аудиосессий (запись/плеер, наушники/динамик) — закладываю время на этапе 10.
