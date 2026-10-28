# Tasks

## 1. iOS image picker

- [x] 1.1 Добавить `IosImagePickerDelegate` и реализовать `IosImagePicker` через `PHPickerViewController`: один выбор, отмена → `null`, JPEG → `PickedImage`, защита от повторного вызова; проверить компиляцию через `./gradlew :shared:compileKotlinIosSimulatorArm64`.
- [x] 1.2 Добавить `attach(host: UIViewController)` в `IosImagePicker` и подключить `ComposeUIViewController` в `MainViewController`; проверить сборку `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`.
- [x] 1.3 Прогнать ручной сценарий на iOS Simulator: выбрать фото → появляется исходящее изображение; отменить выбор → сообщение не создаётся; повторить выбор после отмены; проверить через `make ios` и зафиксировать результат.

## 2. iOS audio recording and playback

- [x] 2.1 Добавить `IosAudioSession` с playback/record-категориями и подключить playback-настройку в `IosAudioPlayer`; проверить компиляцию iOS-таргета.
- [x] 2.2 Реализовать `IosAudioRecorder` через `AVAudioRecorder`: permission, AAC/M4A, старт/стоп/отмена, удаление временного файла, `durationSec` 1..120; проверить компиляцию iOS-таргета.
- [x] 2.3 Добавить `NSMicrophoneUsageDescription` в `iosApp/iosApp/Info.plist` и обновить doc-комментарий `AudioRecorder`; проверить сборку `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`.
- [x] 2.4 Проверить вручную на iOS Simulator: запись ~2 секунд и воспроизведение; отмена записи; отказ в доступе к микрофону → запись не начинается без падения; повторная запись после воспроизведения; если симулятор не даёт микрофон — повторить на устройстве и зафиксировать это в отчёте.

## 3. Documentation

- [x] 3.1 Обновить `docs/DEBT.md` и `docs/BACKLOG.md`: закрыть заглушки iOS-медиа и `IOS-VOICE-1`, зафиксировать native fallback и причину отказа от KMP-библиотек; проверить, что записи больше не называют iOS-медиа заглушками.

## 4. Integration checks

- [x] 4.1 Прогнать `./gradlew :shared:testAndroidHostTest` и `./gradlew :androidApp:assembleDebug`; убедиться, что Android-регрессий нет.
- [x] 4.2 Прогнать `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` и проверить change через `openspec validate --type change ios-media-parity --strict`; ошибок нет.

## Workflow follow-up

- Архив change-а — после приёмки и отдельного указания пользователя.
