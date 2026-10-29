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

## [2026-07-20 → 2026-10-29] Release-конфигурация: test mode, dev-хосты и cleartext

- **Модуль/файл:** `shared` commonMain `di/AppConfig.kt`, `InitKoin.kt`, `AppModules.kt`;
  `androidApp/build.gradle.kts`, `androidApp/src/main/AndroidManifest.xml`,
  `androidApp/src/debug/AndroidManifest.xml`; `iosApp/Configuration/Config.xcconfig`,
  `iosApp/Configuration/CheckReleaseConfig.sh`, `iosApp/iosApp/Info.plist`,
  `iosApp/iosApp.xcodeproj/project.pbxproj`.
- **Суть:** конфигурация вынесена в `AppConfig` на entrypoint. Debug использует
  test mode и dev-хост; release получает `API_BASE_URL_RELEASE` (HTTPS), test mode
  выключен. Cleartext разрешён только debug-manifest; iOS Release проверяется
  Run Script, Android — Gradle-проверкой.
- **Риск:** release-сборка теперь требует явный `API_BASE_URL_RELEASE`; без него
  сборка падает. Это осознанно: лучше ошибка сборки, чем production-артефакт с
  dev-хостом или тестовым входом.
- **Команды:** Android —
  `./gradlew :androidApp:assembleRelease -PAPI_BASE_URL_RELEASE=https://<host>/api/v1`;
  iOS — `xcodebuild ... -configuration Release API_BASE_URL_RELEASE=https://<host>/api/v1`.
  Фактический production URL не хранится в репозитории; его задаёт release-владелец.
- **Проверка:** debug-сборка сохраняет test mode; release-сборки Android/iOS
  проверены с `https://api.example.invalid/api/v1`; release без URL падает;
  release manifest не содержит cleartext.
