# ChatShop — реестр технического долга

> Фиксировать сюда осознанные компромиссы (что и почему упрощено/отложено), а не список
> личных TODO по текущей фиче. Каждая запись — при её закрытии переносится в конец файла
> в раздел «Закрыто» или удаляется.

Формат записи:

```
## [YYYY-MM-DD] Краткое название
- Модуль/файл: ...
- Суть: что сделано не идеально и почему (срок/ограничение/неопределённость).
- Риск: к чему может привести, если не исправить.
- Как исправить: ...
```

---

## Открыто

## [2026-07-20] Happy-path авторизации не прогнан с реальным бэкендом
- Модуль/файл: `features/auth` (экран кода, verify, refresh-цепочка).
- Суть: локальный бэкенд не был поднят — проверены UI-флоу, маска, валидация и обработка сетевой ошибки; экран кода и verify/refresh проверены только компиляцией и логикой.
- Риск: расхождение с контрактом (формат полей, коды ошибок) обнаружится позже.
- Как исправить: при первом поднятии бэкенда прогнать полный вход (код из лога сервера), проверить 401→refresh→повтор.


---

## Закрыто

## [2026-07-20 → 2026-07-20] Временный вход в витрину дизайн-системы с онбординга
- Закрыто на этапе 3: кнопка и колбэк удалены с реального онбординга. Экран `features/example`
  и `Config.Example` остаются в коде как dev-витрина без входа из прод-UI.

## [2026-07-22 → 2026-10-28] iOS-медиа: native fallback вместо KMP-библиотеки

- **Модуль/файл:** `shared` iosMain `IosImagePicker.kt`, `IosImagePickerDelegate.kt`,
  `IosAudioRecorder.kt`, `IosAudioSession.kt`; `MainViewController.kt`;
  `iosApp/iosApp/Info.plist`.
- **Суть:** выбор фото и запись голосовых реализованы нативно: `PHPickerViewController`
  для одного изображения и `AVAudioRecorder` (AAC/M4A) с `AVAudioSession`. Пикер
  презентуется от корневого `ComposeUIViewController`, переданного через `attach`.
- **Почему native:** рассмотрены KMP-кандидаты. `ImagePickerKMP` требует
  Compose-state и тянет камеру/кроп/Coil; `Peekaboo` не развивается с 2024;
  `kmp-record` не работает на iOS Simulator; `KRecorder` слишком ранний;
  `Klarinet` — тяжёлый low-level audio engine. Для одного фото и голосового
  системные API дают меньшую зависимость и полный контроль.
- **Риск:** native-код нужно сопровождать отдельно для iOS; UIKit-сценарии не
  покрываются host-тестами.
- **Проверка:** iOS Simulator — выбор фото, отмена, повторный выбор, запись,
  воспроизведение, отмена записи и отказ в микрофоне пройдены; Android-регрессия
  (`testAndroidHostTest` + `assembleDebug`) пройдена. На физическом iOS-устройстве
  не проверялось.
- **Как исправить при необходимости:** заменить реализацию на KMP-библиотеку,
  сохранив интерфейсы `ImagePicker`/`AudioRecorder`.

## [2026-07-20 → 2026-11-02] Release-конфигурация: test mode, dev-хосты и cleartext

- **Модуль/файл:** `shared` commonMain `di/AppConfig.kt`, `DevConfig.kt`, `InitKoin.kt`,
  `AppModules.kt`; `androidApp/build.gradle.kts`, `androidApp/src/main/AndroidManifest.xml`,
  `androidApp/src/debug/AndroidManifest.xml`; `iosApp/Configuration/Config.xcconfig`,
  `iosApp/Configuration/CheckReleaseConfig.sh`, `iosApp/iosApp/Info.plist`.
- **Суть:** `testMode` — константа `DevConfig.TEST_MODE` в common-коде, независимая от
  debug/release. Debug-сборки по умолчанию используют test mode; для локального backend
  константа выставляется в `false`. Release получает `API_BASE_URL_RELEASE` (HTTPS);
  cleartext разрешён только debug-manifest.
- **Риск:** release-сборка требует не только URL, но и **ручного** `DevConfig.TEST_MODE =
  false`. Автоматической проверки константы нет — это осознанный компромисс ради
  code-level переключателя. Перед релизом проверить реальный auth и отсутствие мок-диалогов.
- **Команды:** Android —
  `./gradlew :androidApp:assembleRelease -PAPI_BASE_URL_RELEASE=https://<host>/api/v1`;
  iOS — `xcodebuild ... -configuration Release API_BASE_URL_RELEASE=https://<host>/api/v1`.
  Фактический production URL не хранится в репозитории; его задаёт release-владелец.
- **Проверка:** Android/iOS debug проверены с `DevConfig.TEST_MODE=true` и локальным
  backend при `false`; release-сборки с тестовым HTTPS URL проходят, release без URL
  падает, release manifest не содержит cleartext.
