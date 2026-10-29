# Tasks

## 1. Common configuration

- [x] 1.1 Заменить `DiConfig` на `AppConfig(testMode, apiBaseUrl)`, передавать его через `initKoin/appModules` и создавать `ApiConfig` из `appConfig.apiBaseUrl`; проверить компиляцию `./gradlew :shared:compileAndroidMain` и `./gradlew :shared:compileKotlinIosSimulatorArm64`.
- [x] 1.2 Убрать захардкоженные `ApiConfig` из `PlatformModule.android` и `PlatformModule.ios`; добавить в `AppConfig` проверки: URL не пустой, release требует `https://`; проверить тестами `AppConfigTest` (debug+http, release+https, release+http rejected) через `./gradlew :shared:testAndroidHostTest --tests "com.al4apps.chatshop.di.AppConfigTest"`.

## 2. Android release configuration

- [x] 2.1 В `androidApp/build.gradle.kts` включить `buildConfig`, добавить `API_BASE_URL_DEBUG` (default `http://10.0.2.2:8080/api/v1`) и обязательный `API_BASE_URL_RELEASE`; `preReleaseBuild` должен падать при отсутствии/некорректном URL или dev-хосте; проверить `./gradlew :androidApp:assembleRelease -PAPI_BASE_URL_RELEASE=https://api.example.invalid/api/v1`.
- [x] 2.2 Передать `AppConfig(testMode = BuildConfig.DEBUG, apiBaseUrl = BuildConfig.API_BASE_URL)` из `ChatShopApp`; проверить `./gradlew :androidApp:assembleDebug`.
- [x] 2.3 Убрать `usesCleartextTraffic` из main-manifest, добавить `androidApp/src/debug/AndroidManifest.xml` с cleartext только для debug; проверить merged debug/release manifests: в release нет `usesCleartextTraffic=true`, в debug есть.

## 3. iOS release configuration

- [x] 3.1 Добавить в `Config.xcconfig` `API_BASE_URL_DEBUG`, пустой `API_BASE_URL_RELEASE`, `TEST_MODE_DEBUG=YES`, `TEST_MODE_RELEASE=NO`; задать `API_BASE_URL`/`TEST_MODE` в Debug/Release build settings; проверить `xcodebuild -showBuildSettings` для обеих конфигураций.
- [x] 3.2 Добавить в `Info.plist` `ApiBaseUrl=$(API_BASE_URL)`, `TestMode=$(TEST_MODE)` и локальную ATS-настройку для debug/localhost; читать значения в `MainViewController` и передавать `AppConfig` в `initKoin`; проверить `make ios` в Debug (test mode остаётся).
- [x] 3.3 Добавить `CheckReleaseConfig.sh` и Run Script phase: Release требует непустой `https://` URL без `localhost`/`10.0.2.2` и `TEST_MODE=NO`; проверить `xcodebuild -configuration Release API_BASE_URL_RELEASE=https://api.example.invalid/api/v1 ...` и падение без URL.
- [x] 3.4 Проверить собранный `ChatShop.app/Info.plist` через `plutil -p`: Debug `TestMode=YES`/localhost, Release `TestMode=NO`/production URL; зафиксировать результат.

## 4. Documentation

- [x] 4.1 Обновить `docs/DEBT.md`, `docs/PROJECT.md`, `docs/PLAN.md`: закрыть `TEST_MODE`, dev-хосты и глобальный cleartext; записать обязательный `API_BASE_URL_RELEASE` и команду release-сборки; проверить `rg` по старым формулировкам.

## 5. Integration checks

- [x] 5.1 Прогнать `./gradlew :shared:testAndroidHostTest`, `./gradlew :androidApp:assembleDebug` и `./gradlew :androidApp:assembleRelease -PAPI_BASE_URL_RELEASE=https://api.example.invalid/api/v1`; все сборки успешны.
- [x] 5.2 Прогнать `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`, release-сборку iOS с тестовым HTTPS URL и `openspec validate --type change harden-release-config --strict`; ошибок нет.

## Workflow follow-up

- Архив change-а — после приёмки и отдельного указания пользователя.
