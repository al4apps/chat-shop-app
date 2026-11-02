# Design

## Context

См. `proposal.md` — Why. После `harden-release-config` URL и release-проверки уже вынесены
на край приложения. `testMode` пока остаётся производным от `BuildConfig.DEBUG` (Android)
и `Info.plist` (iOS), что не соответствует нужной модели.

Локальный backend доступен: `GET http://localhost:8080/health` и `/ready` — `200`;
iOS Simulator — `http://localhost:8080/api/v1`, Android-эмулятор —
`http://10.0.2.2:8080/api/v1`.

## Goals / Non-Goals

**Goals:**

- Один code-level переключатель `testMode`, независимый от debug/release.
- Debug/release используют один и тот же способ выбора источника данных.
- Сохранить build-настройки только для URL окружения.
- Убрать runtime-проверки из `AppConfig`.
- Проверить оба режима на локальном backend.

**Non-Goals:**

- Runtime-переключатель внутри приложения.
- Production URL и backend.
- Полный регресс всех функций MVP.
- Автоматическая защита от забытой константы `TEST_MODE = true` в release.

## Decisions

### 1. `DevConfig.TEST_MODE` — обычная константа в common-коде

```kotlin
object DevConfig {
    /**
     * true  — test mode/моки;
     * false — реальный backend.
     *
     * Перед release выставить false.
     */
    const val TEST_MODE = true
}
```

`ChatShopApp` (Android) и `MainViewController` (iOS) передают:

```kotlin
AppConfig(
    testMode = DevConfig.TEST_MODE,
    apiBaseUrl = ...,
)
```

Почему так: это ровно «переключатель в коде, как раньше», без зависимости от build
variant. Одна константа управляет обоими платформами.

Альтернативы:

- `BuildConfig.TEST_MODE`/Xcode setting — отклонено: пользователь не хочет test mode в
  build config.
- Runtime toggle в настройках — отклонено: нужен скрытый UI/перезапуск Koin и появляется
  риск включить моки в production.

### 2. `AppConfig` — данные без guards

Из `AppConfig` удаляются все `require` и закомментированные строки. `AppConfig` больше не
решает, валиден ли URL для режима.

- test mode не ходит в сеть, URL может быть локальным/неиспользуемым;
- debug с реальным backend может использовать `http://localhost` или LAN URL;
- release URL проверяется build-скриптами (`API_BASE_URL_RELEASE` / iOS Run Script).

`AppConfigTest` проверяет только передачу значений; тесты на runtime-исключения удаляются.

### 3. Убрать test mode из build-конфигурации

Android:

- `androidApp/build.gradle.kts` больше не содержит `TEST_MODE` field;
- `BuildConfig` используется только для `API_BASE_URL`;
- `ChatShopApp` больше не читает `BuildConfig.DEBUG` для данных.

iOS:

- из `Config.xcconfig` удаляются `TEST_MODE_DEBUG`/`TEST_MODE_RELEASE`;
- из `Info.plist` удаляется ключ `TestMode`;
- из build settings/pbxproj удаляются `TEST_MODE`;
- `CheckReleaseConfig.sh` больше не проверяет `TEST_MODE`, только URL;
- `MainViewController` читает только `ApiBaseUrl` и берёт `testMode` из `DevConfig`.

`API_BASE_URL_DEBUG`/`API_BASE_URL_RELEASE` остаются: это адрес окружения, а не тестовый
режим.

### 4. Матрица режимов

| Сценарий | `DevConfig.TEST_MODE` | `API_BASE_URL` |
| --- | --- | --- |
| Android-эмулятор, моки | `true` | `10.0.2.2` |
| iOS Simulator, моки | `true` | `localhost` |
| Физическое устройство без backend | `true` | локальный/неиспользуемый |
| Debug с локальным backend | `false` | доступный локальный URL |
| Release, реальный backend | `false` (вручную) | HTTPS production URL |

Для физического Android-устройства с локальным backend `API_BASE_URL_DEBUG` должен быть
LAN-адресом Mac, а не `10.0.2.2`. Для iOS Simulator `localhost` уже корректен.

### 5. Release — ручной чеклист

Так как кодовая константа не знает о build variant, release-процесс обязан:

1. выставить `DevConfig.TEST_MODE = false`;
2. собрать release с `API_BASE_URL_RELEASE=https://<host>/api/v1`;
3. проверить, что release-сборка не показывает мок-диалоги и выполняет реальный auth.

Это осознанный компромисс: автоматическая защита потребовала бы build-флаг или runtime
режим, которые отклонены.

### 6. Проверка на локальном backend

- `GET /health`, `/ready` — `200`.
- `DevConfig.TEST_MODE = false`.
- Android debug, `API_BASE_URL_DEBUG` default `10.0.2.2`.
- iOS Debug, `API_BASE_URL_DEBUG` default `localhost`.
- `POST /auth/request-code`, код из лога backend, `POST /auth/verify`.
- Список чатов, диалог, текст, изображение, голос, команда заказа.
- При переходе из test mode в real backend очистить данные приложения, чтобы мок-диалоги
  не смешались с серверными.
- После проверки вернуть `TEST_MODE = true` для обычной разработки (если это dev-сборка).

## Risks / Trade-offs

- **[Забыли выставить `TEST_MODE = false` перед release]** → release-чеклист и проверка
  отсутствия мок-данных; автоматической защиты нет по требованию.
- **[Один режим на оба варианта сборки]** → для debug с backend нужно менять константу и
  пересобирать; это ожидаемая цена code-level переключателя.
- **[Удаление guards скрывает пустой/неверный URL]** → release-проверки URL остаются;
  debug с test mode сеть не использует.
- **[Физическое устройство не видит localhost]** → использовать LAN URL или тестировать
  на эмуляторе; зафиксировать в отчёте.
- **[Smoke найдёт дефекты вне scope]** → записать в backlog, не расширять change.

## Migration Plan

- Переключение `DevConfig.TEST_MODE` — обычная правка + пересборка.
- Удалить старые `TEST_MODE` build settings/Info.plist после проверки, что release-скрипт
  больше на них не опирается.
- Откат — вернуть `BuildConfig.DEBUG`/plist-режим; данные/БД не мигрируются.

## Open Questions

Нет. Выбран code-level `DevConfig.TEST_MODE`; release переключается вручную.
