# Design

## Context

См. `proposal.md` — Why. Сейчас конфигурация задаётся в трёх местах: `DiConfig.TEST_MODE`
(общий код), `PlatformModule.*` (захардкоженный dev-хост) и `AndroidManifest.xml`
(глобальный cleartext). `initKoin()` не принимает конфигурацию, поэтому entrypoint не может
передать release-значения.

Ограничения:

- production URL в репозитории нет; его нельзя придумывать;
- debug должен сохранить текущий сценарий: test mode и локальный backend;
- release Android не должен содержать test mode и cleartext;
- iOS `Info.plist` уже обрабатывается Xcode и может получать build settings;
- `Config.xcconfig` общий для Debug/Release, но release-значения нужно задавать отдельно.

## Goals / Non-Goals

**Goals:**

- Один типизированный `AppConfig`, создаваемый на краю приложения.
- Release не может случайно использовать test mode, localhost или `http://`.
- Debug остаётся удобным: test mode, `10.0.2.2`/`localhost`, cleartext.
- Ошибка конфигурации видна на сборке, а не только в рантайме.
- Проверить обе платформы и обе конфигурации.

**Non-Goals:**

- Выбор и проверка реального production-домена.
- Настройка signing/provisioning/App Store.
- Minify/R8, versionCode/versionName, CI-секреты.
- Изменение auth/чатов/outbox или UI.

## Decisions

### 1. `AppConfig` на entrypoint вместо глобального `DiConfig`

В common-код добавляется:

```kotlin
data class AppConfig(
    val testMode: Boolean,
    val apiBaseUrl: String,
)
```

`initKoin(appConfig, platformConfig)` передаёт `testMode` в `appModules(...)`, а
`apiBaseUrl` — в `ApiConfig`. `DiConfig.TEST_MODE` и `ApiConfig` из платформенных модулей
удаляются.

Почему так: build-конфигурация принадлежит платформенному entrypoint (Android
`BuildConfig`, iOS `Info.plist`), а не общему коду. Альтернатива — оставить `DiConfig` и
менять его перед релизом — снова создаёт ручной шаг, который можно забыть.

`AppConfig` валидирует инвариант:

- URL не пустой;
- release (`testMode = false`) допускает только `https://`;
- debug/test mode может использовать `http://`.

### 2. Android: `BuildConfig` и отдельный debug-manifest

`androidApp`:

- `buildFeatures.buildConfig = true`;
- `API_BASE_URL_DEBUG` — Gradle property/environment, default
  `http://10.0.2.2:8080/api/v1`;
- `API_BASE_URL_RELEASE` — обязательный Gradle property/environment;
- `buildConfigField("String", "API_BASE_URL", ...)` в debug/release;
- `preReleaseBuild` проверяет: значение задано, начинается с `https://`, не содержит
  `localhost` и `10.0.2.2`.

`ChatShopApp` передаёт:

```kotlin
AppConfig(
    testMode = BuildConfig.DEBUG,
    apiBaseUrl = BuildConfig.API_BASE_URL,
)
```

`AndroidManifest.xml` больше не содержит `usesCleartextTraffic`. Новый
`androidApp/src/debug/AndroidManifest.xml` включает cleartext только для debug.

Альтернатива — `networkSecurityConfig` с доменом `10.0.2.2`: строже, но добавляет XML и
не даёт выигрыша для тестового debug-хоста. Debug-manifest проще; release по умолчанию
не разрешает cleartext (targetSdk 36).

### 3. iOS: значения через Xcode, чтение через `Info.plist`

В `Config.xcconfig`:

```text
API_BASE_URL_DEBUG = http://localhost:8080/api/v1
API_BASE_URL_RELEASE =
TEST_MODE_DEBUG = YES
TEST_MODE_RELEASE = NO
```

В build settings Debug/Release задаётся:

```text
API_BASE_URL = $(API_BASE_URL_DEBUG)   // Debug
TEST_MODE = $(TEST_MODE_DEBUG)
API_BASE_URL = $(API_BASE_URL_RELEASE) // Release
TEST_MODE = $(TEST_MODE_RELEASE)
```

`Info.plist` получает ключи:

```xml
<key>ApiBaseUrl</key><string>$(API_BASE_URL)</string>
<key>TestMode</key><string>$(TEST_MODE)</string>
```

`MainViewController` читает их из `NSBundle.mainBundle` и передаёт в `initKoin`.
`API_BASE_URL_RELEASE` не хранится в репозитории: release-владелец задаёт его через
Xcode user-defined setting или `xcodebuild API_BASE_URL_RELEASE=...`.

### 4. Build-time проверка iOS Release

Добавляется `iosApp/Configuration/CheckReleaseConfig.sh` и Run Script phase:

- для `Debug` — no-op;
- для `Release` — `API_BASE_URL` задан, начинается с `https://`, не содержит
  `localhost`/`10.0.2.2`, `TEST_MODE = NO`.

Ошибка скрипта останавливает `xcodebuild`, поэтому release без production URL не
соберётся. Альтернатива — только runtime `require`: она обнаруживает проблему позже и
позволяет собрать потенциально опасный артефакт.

### 5. ATS и cleartext

iOS `Info.plist` получает `NSAllowsLocalNetworking = true` для debug/localhost. Это не
открывает произвольный HTTP; release URL обязан быть HTTPS и проверяется скриптом.
Android cleartext ограничивается debug-манифестом.

### 6. Проверка конфигурации

Сценарии:

| Сборка | Ожидание |
| --- | --- |
| Android debug | test mode, `10.0.2.2`, cleartext разрешён |
| Android release с `API_BASE_URL_RELEASE=https://api.example.invalid/api/v1` | test mode выключен, URL в `BuildConfig`, cleartext нет |
| Android release без URL | `preReleaseBuild` падает |
| iOS debug | `TestMode=YES`, `ApiBaseUrl=http://localhost...`, test login |
| iOS Release с HTTPS URL | `TestMode=NO`, `ApiBaseUrl` production, ATS cleartext нет |
| iOS Release без URL | Run Script падает |

`api.example.invalid` используется только для проверки механизма; реальный URL в
репозиторий не попадает.

## Risks / Trade-offs

- **[Release-сборка теперь требует обязательный параметр]** → это намеренно; ошибка
  сборки лучше, чем production-артефакт с dev-хостом. В `DEBT.md`/`PROJECT.md` указать
  команду и имя параметра.
- **[Xcode build setting не долетит до `Info.plist`]** → проверить
  `xcodebuild -showBuildSettings` и `plutil -p` собранного `Info.plist`.
- **[`NSAllowsLocalNetworking` недостаточно для `localhost`]** → при необходимости
  добавить `NSExceptionDomains` только для `localhost`; release остаётся HTTPS-only.
- **[`BuildConfig` не генерируется в app-модуле]** → включить
  `buildFeatures.buildConfig = true` и проверить `assembleDebug`/`assembleRelease`.
- **[Release не проверен с реальным backend]** → механизм проверяется тестовым HTTPS
  URL; фактический URL и smoke-тест остаются задачей release-владельца.
- **[Debug-манифест случайно попадёт в release]** → release-проверка `aapt2 dump xmltree`
  и отсутствие `usesCleartextTraffic=true`.

## Migration Plan

1. В release CI/локальной сборке добавить `API_BASE_URL_RELEASE` (Android Gradle
   property/environment) и `API_BASE_URL_RELEASE` (iOS build setting).
2. Собрать release с реальным URL и проверить Info.plist/manifest.
3. Откат — вернуть прежние `DiConfig`/platform modules и main-manifest; БД и API не
   мигрируются.

## Open Questions

Нет. Фактический production URL — внешний release-параметр, а не решение внутри change.
