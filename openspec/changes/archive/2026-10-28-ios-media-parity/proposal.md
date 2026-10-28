# Proposal

## Why

На iOS выбор фото и запись голосовых остаются заглушками: `IosImagePicker` возвращает `null`, `IosAudioRecorder.isAvailable = false`. Android-пользователь может отправлять оба типа вложений, iOS — нет. Это функциональный разрыв в основном сценарии чата и блокер паритета перед стабилизацией MVP.

Спека `messaging` уже требует выбор изображения и запись голосового на обеих платформах. Проблема не в контракте, а в отсутствующей iOS-реализации.

## What Changes

- Реализовать `IosImagePicker` через нативный `PHPickerViewController`:
  - выбор одного изображения из галереи;
  - отмена → `null`;
  - результат — JPEG-байты для существующего `ImageCompressor`;
  - презентация от корневого `UIViewController` через явный `attach`, без поиска key window.
- Реализовать `IosAudioRecorder` через нативный `AVAudioRecorder`:
  - запрос разрешения на микрофон и корректный отказ;
  - AAC/M4A, mono, 44.1 kHz;
  - старт, стоп, отмена, удаление временного файла;
  - реальная длительность 1–120 секунд и `mime = "audio/mp4"`;
  - настройка `AVAudioSession` и возврат playback-категории после записи.
- Добавить `NSMicrophoneUsageDescription` в `iosApp/iosApp/Info.plist`.
- Подключить корневой контроллер в `MainViewController` для презентации пикера.
- Проверить/настроить `AVAudioSession` в `IosAudioPlayer`, чтобы запись и воспроизведение не конфликтовали.
- Добавить ручной чек-лист iOS Simulator/устройство и регрессию Android.
- Обновить `docs/DEBT.md` и `docs/BACKLOG.md`: закрыть заглушки и `IOS-VOICE-1`, зафиксировать выбор native fallback.

**Вне scope:**

- изменения Android-реализаций и общего UI;
- камера, мультивыбор и кроп изображений;
- отдельный UX-экран для отказа в разрешении (сохраняется текущая деградация: запись просто не начинается);
- автоматические UI-тесты UIKit-контроллеров;
- новые KMP-библиотеки: по результатам оценки кандидаты не подходят по зрелости или архитектуре.

## Capabilities

### New Capabilities

Нет.

### Modified Capabilities

Нет. `messaging` уже описывает изображения и голосовые; change закрывает разрыв реализации. Установлен `skip_specs: true`.

## Impact

- `shared/src/iosMain/kotlin/.../core/media/IosImagePicker.kt`
- `shared/src/iosMain/kotlin/.../core/media/IosAudioRecorder.kt`
- `shared/src/iosMain/kotlin/.../core/media/IosAudioPlayer.kt`
- `shared/src/iosMain/kotlin/.../MainViewController.kt`
- `shared/src/iosMain/kotlin/.../di/PlatformModule.ios.kt` — при необходимости проводка host/picker.
- `iosApp/iosApp/Info.plist`
- `docs/DEBT.md`, `docs/BACKLOG.md`
- Публичные интерфейсы, API, БД, Android и пользовательские тексты не меняются.
