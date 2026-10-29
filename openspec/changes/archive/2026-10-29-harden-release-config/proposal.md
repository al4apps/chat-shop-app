# Proposal

## Why

Сейчас `DiConfig.TEST_MODE = true` — константа в общем коде, а dev-хосты (`10.0.2.2`,
`localhost`) и Android cleartext захардкожены. В release-сборке это может включить вход
без backend, направить запросы на dev-хост и разрешить незашифрованный HTTP. Это
security-блокер перед стабилизацией MVP.

Production URL в репозитории отсутствует, поэтому change не придумывает домен: release
должен получать URL из обязательного build-time параметра и падать при его отсутствии.

## What Changes

- **BREAKING для release-сборки:** добавить обязательный build-time параметр
  `API_BASE_URL`/`API_BASE_URL_RELEASE`; release без него не собирается.
- Заменить глобальную константу `DiConfig.TEST_MODE` на `AppConfig`, создаваемый на краю
  приложения:
  - Android — из `BuildConfig.DEBUG` и `BuildConfig.API_BASE_URL`;
  - iOS — из значений `Info.plist`, подставляемых Xcode-конфигурацией.
- Debug использует dev-хост и test mode; release — production URL и
  `testMode = false`.
- Убрать `android:usesCleartextTraffic="true"` из main-манифеста; разрешить cleartext
  только в debug-манифесте.
- На iOS добавить локальную ATS-настройку только для dev/localhost; release-URL обязан
  быть `https://`.
- Добавить build-time проверки:
  - Android: release-задача без `API_BASE_URL_RELEASE` завершается ошибкой;
  - iOS: Run Script для `Release` проверяет непустой HTTPS URL и отсутствие dev-хостов.
- Проверить debug и release сборки обеих платформ; release собран с тестовым HTTPS URL,
  фактический production URL задаёт release-владелец.
- Обновить `DEBT.md`, `PROJECT.md` и `PLAN.md`: убрать записи о `TEST_MODE`, dev-хостах
  и глобальном cleartext.

**Вне scope:** выбор фактического production-домена, signing/provisioning, minify/R8,
App Store metadata, изменения пользовательского UI и бизнес-логики.

## Capabilities

### New Capabilities

Нет.

### Modified Capabilities

Нет. `auth` и остальные спеки уже описывают production-поведение; change исправляет
build-конфигурацию, чтобы release не попадал в тестовый/девелоперский режим. Установлен
`skip_specs: true`.

## Impact

- `shared/src/commonMain/kotlin/.../di/DiConfig.kt` — заменить/удалить.
- `shared/src/commonMain/kotlin/.../di/InitKoin.kt`, `AppModules.kt` — `AppConfig` и
  `ApiConfig` из entrypoint.
- `shared/src/androidMain/kotlin/.../di/PlatformModule.android.kt` — убрать hardcoded URL.
- `shared/src/iosMain/kotlin/.../di/PlatformModule.ios.kt`, `MainViewController.kt` — убрать
  hardcoded URL, читать `Info.plist`.
- `androidApp/build.gradle.kts`, `androidApp/src/main/AndroidManifest.xml`,
  новый `androidApp/src/debug/AndroidManifest.xml`, `ChatShopApp.kt`.
- `iosApp/Configuration/Config.xcconfig`, `iosApp/iosApp/Info.plist`,
  `iosApp/iosApp.xcodeproj/project.pbxproj`, новый release-check script.
- `docs/DEBT.md`, `docs/PROJECT.md`, `docs/PLAN.md`.
- API, БД, пользовательские сценарии и зависимости не меняются.
