# ChatShop — описание проекта (для себя)

> Рабочая память агента о проекте. Обновлять при значимых архитектурных решениях.
> Формальные роль/правила — в [`AGENT_ROLE.md`](AGENT_ROLE.md) и [`CODING_RULES.md`](CODING_RULES.md).
> Контракт с бэкендом — в [`CLIENT_GUIDE.md`](CLIENT_GUIDE.md) (дополнение к `/openapi.yaml`).

---

## 1. Что это за приложение

**ChatShop** — клиентское KMP-приложение (Android / iOS / Web) для сервиса заказа через
диалог с оператором вместо привычной корзины/каталога. Тематическая легенда бэкенда —
«магазин автозапчастей», но на неё в UI/логике не завязываемся: домен приложения — это
универсальный chat-commerce (диалог + лента событий + проекция заказа).

Ключевая идея с точки зрения клиента (см. `CLIENT_GUIDE.md` §1, §5, §6):
- Диалог = лента сообщений (`seq`, монотонный курсор), это единственный источник истины.
- Отдельной REST-сущности заказа нет — заказ живёт как `SYSTEM`-сообщения в ленте +
  проекция текущего состояния на `Dialog` (`orderStatus/orderAmount/orderDescription/orderNo`).
- Клиент не создаёт `SYSTEM`-сообщения и не меняет статус заказа напрямую — только команды
  (`POST /commands`), и из них клиенту доступна ровно одна: `ORDER_COMPLETED`.
- Синхронизация — инкрementальный поллинг по `afterSeq`, push (FCM) — только сигнал «есть
  обновление», не источник данных.
- Роль приложения на сервере — `CLIENT` (в отличие от `OPERATOR`/`ADMIN`).

---

## 2. Технологический стек

Текущее состояние — **пустой каркас** (голый Compose Multiplatform), без бизнес-фич.
Архитектурные библиотеки будут добавляться по мере фич, согласовано с тимлидом.

- **Язык**: Kotlin 2.4.10, JVM target — **17** (Android/JVM бинарники).
- **UI**: Compose Multiplatform (Shared UI) — общий UI для Android/iOS/Web,
  Material 3 (`material3` alpha, версии — `gradle/libs.versions.toml`).
- **Таргеты KMP**: `android`, `iosArm64` + `iosSimulatorArm64` (статический framework `Shared`),
  `js` (browser), `wasmJs` (browser).
- **Сборка**: Gradle Kotlin DSL, AGP 9.0.1, version catalog (`gradle/libs.versions.toml`).
- **Пока не подключено** (появится вместе с первыми фичами по плану): Decompose (навигация/
  компоненты), Ktor Client + kotlinx-serialization (сеть), kotlinx-coroutines/datetime,
  защищённое хранилище токенов (KSafe — см. одноимённый skill), SQLDelight (если понадобится
  локальный кэш).

Полный список ожидаемой экспертизы и архитектурных принципов — `AGENT_ROLE.md`.

---

## 3. Структура модулей

```
/build.gradle.kts, /settings.gradle.kts   — корневая сборка, только apply false плагины
/gradle/libs.versions.toml                 — version catalog (единственное место версий)

/shared            — общий модуль (commonMain/androidMain/iosMain/jsMain/wasmJsMain)
  src/commonMain    — Shared UI (App.kt — точка входа Compose) + вся общая логика
  src/androidMain   — android-специфичный код (при необходимости)
  src/iosMain       — MainViewController (вход для iOS через ComposeUIViewController)
  src/jsMain        — код общий для js/wasmJs целей (browser API и т.п.)
  src/wasmJsMain    — wasmJs-специфика
  src/*Test         — smoke-тесты по каждому sourceSet (compile sanity)

/androidApp         — тонкая Android-обёртка (MainActivity → App())
/iosApp             — Xcode-проект обёртки (iosApp/iosApp, .xcodeproj)
/webApp             — тонкая Web-обёртка (js/wasmJs executable, main() → ComposeViewport)

/local              — рабочие материалы агента (эта директория, не часть runtime-кода)
```

Namespace/package — `com.al4apps.chatshop` везде (Android namespace `com.al4apps.chatshop`,
shared namespace `com.al4apps.chatshop.shared`, iOS bundle id `com.al4apps.chatshop.ChatShop`).

---

## 4. Архитектурные принципы (кратко, детали — `AGENT_ROLE.md`/`CODING_RULES.md`)

- Clean Architecture: domain / use cases / repository abstraction / data / presentation.
  Domain не зависит от платформы.
- Component-based + Decompose (когда будет подключён): состояние и навигация — в компоненте,
  Compose — тупой рендер состояния.
- Shared UI — приоритет; platform-specific код — только когда невозможно иначе
  (permissions, camera, push, platform storage и т.п.).
- Один цикл разработки = одна согласованная фича. План → апрув → реализация → отчёт.
- Библиотеки и архитектурные решения не меняются без согласования с тимлидом.

---

## 5. Текущий статус

- [x] Инициализация: пустой KMP/Compose Multiplatform каркас (android/ios/web/shared).
- [x] JVM target 17 (androidApp, shared/android).
- [x] `.gitignore`, ветка `develop` для дальнейшей разработки.
- [ ] План фич — будет описан тимлидом на следующем шаге.

---

## 6. Ссылки

- Правила поведения агента: [`AGENT_ROLE.md`](AGENT_ROLE.md)
- Кодстайл/процесс: [`CODING_RULES.md`](CODING_RULES.md)
- Контракт с бэкендом (auth, лента, команды, файлы, push): [`CLIENT_GUIDE.md`](CLIENT_GUIDE.md)
- Реестр техдолга: [`DEBT.md`](DEBT.md)
