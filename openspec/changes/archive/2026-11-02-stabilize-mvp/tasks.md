# Tasks

## 1. Code-level switch

- [x] 1.1 Добавить `DevConfig.TEST_MODE` (default `true`) в common-код и убрать `init`-проверки из `AppConfig`; обновить `AppConfigTest`, убрав тесты runtime-guard и оставив проверку значений; проверить `./gradlew :shared:testAndroidHostTest --tests "com.al4apps.chatshop.di.AppConfigTest"`.
- [x] 1.2 Перевести `ChatShopApp` и `MainViewController` на `DevConfig.TEST_MODE`; убедиться, что `testMode` не читается из `BuildConfig.DEBUG`, `Info.plist` или Xcode build setting; проверить `./gradlew :shared:compileAndroidMain` и `:shared:compileKotlinIosSimulatorArm64`.

## 2. Android cleanup

- [x] 2.1 Убедиться, что в `androidApp/build.gradle.kts` нет `TEST_MODE`/`TEST_MODE_DEBUG`; оставить build-настройки только для `API_BASE_URL`. Проверить `./gradlew :androidApp:assembleDebug` и отсутствие `TEST_MODE` в сгенерированном `BuildConfig.java`.
- [x] 2.2 Проверить debug-сценарий по умолчанию: `DevConfig.TEST_MODE=true`, test mode работает; отдельно временно поставить `false` и убедиться, что приложение идёт в реальный API через настроенный `API_BASE_URL_DEBUG`.

## 3. iOS cleanup

- [x] 3.1 Удалить `TEST_MODE_DEBUG`/`TEST_MODE_RELEASE` из `Config.xcconfig`, ключ `TestMode` из `Info.plist`, `TEST_MODE` из build settings и проверку `TEST_MODE` из `CheckReleaseConfig.sh`; проверить `xcodebuild -showBuildSettings` и `plutil -p` — в build/plist остаётся только `ApiBaseUrl`.
- [x] 3.2 Собрать iOS Debug (`make ios`) с `DevConfig.TEST_MODE=true` и проверить test mode; временно поставить `false` и проверить, что приложение использует реальный backend, затем вернуть `true`.

## 4. Local backend smoke

- [x] 4.1 Проверить `GET http://localhost:8080/health` и `/ready` — `200`; при `DevConfig.TEST_MODE=false` пройти на Android-эмуляторе (`10.0.2.2`) и iOS Simulator (`localhost`): запрос кода (код из лога backend), verify, список чатов, диалог, текст, изображение, голос, команду заказа.
- [x] 4.2 Перед real-backend проверкой очистить данные приложения, чтобы мок-диалоги не смешались с серверными; после проверки вернуть `DevConfig.TEST_MODE=true` для обычной разработки и зафиксировать результат.

## 5. Documentation

- [x] 5.1 Обновить `docs/DEBT.md`, `docs/PROJECT.md`, `docs/PLAN.md`: описать `DevConfig.TEST_MODE`, матрицу режимов и ручной release-чеклист (перед release выставить `false`); убрать упоминания `TEST_MODE_DEBUG`/`BuildConfig.TEST_MODE`/`Info.plist TestMode`.

## 6. Integration checks

- [x] 6.1 Прогнать `./gradlew :shared:testAndroidHostTest`, `./gradlew :androidApp:assembleDebug`, `:androidApp:assembleRelease -PAPI_BASE_URL_RELEASE=https://api.example.invalid/api/v1` и `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`; ошибок нет.
- [x] 6.2 Проверить `openspec validate --type change stabilize-mvp --strict` и итоговый статус задач; ошибок нет.

## Workflow follow-up

- Архив change-а — после приёмки и отдельного указания пользователя.
