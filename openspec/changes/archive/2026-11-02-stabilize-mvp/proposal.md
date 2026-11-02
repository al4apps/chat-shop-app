# Proposal

## Why

`testMode = BuildConfig.DEBUG` смешивает тип сборки и источник данных: debug на физическом
устройстве всегда идёт в test mode, а debug с реальным локальным backend требует правки
кода. Проверки в `AppConfig.init` тоже мешают dev-комбинациям и сейчас временно
закомментированы.

Нужно вернуть простой переключатель в коде, как старый `DiConfig.TEST_MODE`, но не
привязывать его к build variant. Debug и release не должны автоматически определять
режим данных.

Локальный backend поднят: `/health` и `/ready` отвечают `200`, базовый URL
`http://localhost:8080/api/v1` актуален.

## What Changes

- Добавить `DevConfig.TEST_MODE` — константу в common-коде:
  - `true` — моки/test mode;
  - `false` — реальный backend.
- Убрать `TEST_MODE` из build-конфигурации полностью:
  - никакого `BuildConfig.TEST_MODE` на Android;
  - никаких `TEST_MODE_DEBUG`/`TEST_MODE_RELEASE` и ключа `TestMode` в iOS;
  - `CheckReleaseConfig.sh` проверяет только release URL.
- `ChatShopApp` и `MainViewController` передают `DevConfig.TEST_MODE` в `AppConfig`.
- Убрать `init`-проверки из `AppConfig`; конфигурация — чистые данные.
- Сохранить build-настройки только для адресов:
  - Android `API_BASE_URL_DEBUG` / `API_BASE_URL_RELEASE`;
  - iOS `API_BASE_URL_DEBUG` / `API_BASE_URL_RELEASE`.
- Зафиксировать режимы:
  1. эмулятор/симулятор — `TEST_MODE=true`, dev URL;
  2. физическое устройство без backend — `TEST_MODE=true`;
  3. debug с локальным backend — `TEST_MODE=false` + доступный URL;
  4. release — вручную `TEST_MODE=false` + HTTPS production URL.
- Проверить на локальном backend: health/ready, запрос кода, verify, список чатов, диалог,
  текст, изображение, голос, команду заказа.
- Обновить `DEBT.md`/`PROJECT.md`/`PLAN.md`: описать code-константу и release-чеклист.

**Вне scope:** production URL, backend, signing, runtime-переключатель внутри приложения,
новые фичи и крупные UI-переделки.

## Capabilities

### New Capabilities

Нет.

### Modified Capabilities

Нет. Это конфигурация разработки/релиза и smoke-проверка; поведение продукта не меняется.
Установлен `skip_specs: true`.

## Impact

- `shared/src/commonMain/kotlin/.../di/DevConfig.kt` — новая константа.
- `shared/src/commonMain/kotlin/.../di/AppConfig.kt` — убрать runtime-проверки.
- `shared/src/commonMain/kotlin/.../di/InitKoin.kt`, `AppModules.kt` — без изменения контракта.
- `androidApp/src/main/kotlin/.../ChatShopApp.kt` — `DevConfig.TEST_MODE`.
- `androidApp/build.gradle.kts` — только `API_BASE_URL`, без `TEST_MODE`.
- `iosApp/Configuration/Config.xcconfig`, `iosApp/iosApp/Info.plist`,
  `iosApp/iosApp.xcodeproj/project.pbxproj`, `CheckReleaseConfig.sh` — убрать `TEST_MODE`.
- `shared/src/iosMain/kotlin/.../MainViewController.kt` — `DevConfig.TEST_MODE`, только
  `ApiBaseUrl` из plist.
- `shared/src/commonTest/.../AppConfigTest.kt` — убрать guard-тесты.
- `docs/DEBT.md`, `docs/PROJECT.md`, `docs/PLAN.md`.
- API, БД, UI и зависимости не меняются.
