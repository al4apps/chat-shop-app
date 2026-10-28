# Design

## Context

См. `proposal.md` — Why. Общий UI уже умеет и фото, и голос: `DialogComponent` вызывает
`ImagePicker.pickImage()`, `VoiceRecordingController` управляет таймером и лимитом 120 секунд,
`MessagesInteractor` сохраняет файл и ставит `IMAGE`/`VOICE` в outbox. На iOS отсутствуют
только платформенные реализации, разрешение и презентация пикера.

Текущие ограничения:

- `iosApp` имеет deployment target 18.2, поэтому доступны `PHPickerViewController` и
  современные API `AVAudioSession`;
- `MainViewController` возвращает `ComposeUIViewController`, но не хранит ссылку на него;
- `Info.plist` не содержит описания доступа к микрофону;
- `IosAudioPlayer` работает, но не настраивает `AVAudioSession`;
- UIKit-контроллеры нельзя покрыть обычными host-тестами; проверка iOS-медиа — ручная.

## Goals / Non-Goals

**Goals:**

- Дать iOS паритет с Android по выбору одного фото и записи голосового.
- Сохранить существующие контракты `ImagePicker`/`AudioRecorder` и весь common-UI.
- Корректно обрабатывать отмену, отказ в микрофоне, короткую запись и лимит 120 секунд.
- Не добавлять новую библиотеку и не менять Android.
- Оставить воспроизведение голосовых рабочим после записи.

**Non-Goals:**

- Камера, мультивыбор, кроп, редактирование изображений.
- Новый экран/диалог объяснения отказа в разрешении.
- Автоматические UI-тесты `PHPickerViewController`/`AVAudioRecorder`.
- Изменение API, БД, outbox или общих текстов.

## Decisions

### 1. Native fallback: `PHPickerViewController` + `AVAudioRecorder`

Рассмотренные KMP-кандидаты:

| Кандидат | Что выяснено | Почему не выбран |
| --- | --- | --- |
| `ImagePickerKMP` 1.1.7 | 2026, MIT, Compose 1.11.1, iOS 16+ | Публичный вход — `@Composable rememberImagePickerKMP`; тянет камеру, кроп, Coil, material icons. Для одного gallery picker требует перестроить вызов из компонента в Compose-state. |
| `Peekaboo` 0.5.2 | последний релиз 2024, 44 открытых issue | Риск несовместимости с Kotlin 2.4 / Compose 1.11; проект не развивается. |
| `kmp-record` 0.5.0 | README: early API, **на симуляторе падает** | Не подходит для обязательной проверки на iOS Simulator. |
| `KRecorder` 0.1.0 | 2026, ранняя версия, малое сообщество | Стабильность не подтверждена; объём проверки больше, чем у нативного AVAudioRecorder. |
| `Klarinet` 0.5.0 | Kotlin 2.4.20, iOS simulator, AAC/M4A | Low-level audio engine для музыки/эффектов; избыточен по размеру и сложности для голосового сообщения. |
| `kmp-audio-recorder-player` | alpha04, последняя активность 2025 | Alpha-версия, не production-кандидат. |

`PHPicker` и `AVAudioRecorder` — системные API, уже используются в проекте (`IosAudioPlayer`,
`ImageCompressor`). Они дают нужный минимум без новой зависимости. Решение фиксируется в
`docs/DEBT.md` как осознанный native fallback.

### 2. Явный host для пикера, без поиска key window

`IosImagePicker` получает метод `attach(host: UIViewController)`. `MainViewController`
создаёт `ComposeUIViewController`, передаёт его в пикер и возвращает наружу:

```text
MainViewController
    |
    +--> ComposeUIViewController (root)
    |          ^
    |          | attach
    +--> IosImagePicker ---present---> PHPickerViewController
                                      |
                                      v delegate
                               PickedImage(JPEG)
```

Почему не `UIApplication.keyWindow`: ключевое окно deprecated и может вернуть не тот
контроллер при появлении модальных окон. Явный host делает зависимость видимой и
тестируемой на уровне запуска приложения.

Если `pickImage()` вызван без host или при уже открытом пикере, возвращается `null`.
Делегат хранится в `IosImagePicker` до завершения, чтобы Kotlin/Native не собрал его раньше
колбэка.

### 3. Фото: PHPicker, один выбор, JPEG на выходе

- Конфигурация: `PHPickerConfiguration`, `filter = images`, `selectionLimit = 1`.
- Отмена или пустой результат → `null`.
- Выбранный `PHPickerResult` загружается через `NSItemProvider.loadObjectOfClass(UIImage)`.
- `UIImageJPEGRepresentation(image, 0.95)` → `ByteArray`; `PickedImage.mime = "image/jpeg"`.
- `PHPicker` не требует доступа к photo library, поэтому `NSPhotoLibraryUsageDescription`
  не обязателен. Добавляем только `NSMicrophoneUsageDescription`.
- Ошибка загрузки объекта или отсутствие данных → `null` без падения.

Двойное JPEG-кодирование (пикер + `ImageCompressor`) допустимо: качество 0.95, итоговый
размер всё равно ограничивается 5 МБ. Альтернатива — загружать исходные данные по UTI и
отдавать их компрессору; отложена, чтобы не зависеть от конкретного UTI-бриджа.

### 4. Голос: AVAudioRecorder + явная аудиосессия

`IosAudioRecorder`:

1. Проверяет `AVAudioSession.recordPermission`.
2. При `undetermined` запрашивает разрешение через `requestRecordPermission` и ждёт callback.
3. При отказе возвращает `false` из `start()`.
4. Настраивает `AVAudioSession` категорией `playAndRecord` с `defaultToSpeaker` и
   `allowBluetooth`, активирует сессию.
5. Создаёт `AVAudioRecorder` с AAC/M4A: 44.1 kHz, mono, bitrate 96 kbps, quality high.
6. Пишет в `NSTemporaryDirectory()/voice-<uuid>.m4a`.

`stop()`:

- запоминает `currentTime` до остановки;
- останавливает и освобождает рекордер;
- читает `NSData`, удаляет временный файл;
- если запись короче 1 секунды — возвращает `null`;
- иначе `durationSec = currentTime.toInt().coerceIn(1, 120)`, `mime = "audio/mp4"`.

`cancel()` останавливает/освобождает рекордер и удаляет файл. После `stop()`/`cancel()`
сессия переводится в playback-категорию, чтобы `IosAudioPlayer` мог играть.

### 5. Аудиосессия общая для записи и плеера

Добавляется небольшой iOS-хелпер `IosAudioSession`:

- `configurePlayback()` — `.playback`, active;
- `configureRecording()` — `.playAndRecord` + `defaultToSpeaker`, active;
- `deactivate()` — при необходимости.

`IosAudioPlayer.startNew()` вызывает `configurePlayback()` перед созданием плеера.
`IosAudioRecorder` вызывает `configureRecording()` на старте и `configurePlayback()` после
остановки. Это устраняет типичную проблему, когда после записи звук уходит в разговорный
динамик или плеер не запускается.

### 6. Проверка — ручная матрица, автоматические тесты не имитируют UIKit

Unit-тестами нельзя надёжно проверить системный пикер/микрофон. В change входит чек-лист:

- iOS Simulator: выбор фото, отмена, запись 2 секунд, отмена записи, отказ в микрофоне,
  воспроизведение после записи;
- iOS Simulator: повторный выбор фото после отмены;
- Android: регрессия выбора фото и записи голосового;
- сборка `linkDebugFrameworkIosSimulatorArm64` и приложения через Xcode/`make ios`.

Если симулятор не даёт вход микрофона, запись проверяется на физическом устройстве;
это фиксируется в результате change-а.

## Risks / Trade-offs

- **[Отсутствует `NSMicrophoneUsageDescription` → система завершит приложение при запросе]** →
  ключ добавляется до первого запроса; проверить на чистой установке.
- **[`PHPicker` не презентуется, если host не привязан]** → host передаётся в
  `MainViewController`; при отсутствии host `pickImage()` возвращает `null`, а не падает.
- **[Делегат пикера собран до колбэка]** → сильная ссылка на делегат в `IosImagePicker`
  до завершения `CompletableDeferred`.
- **[`AVAudioSession` остаётся в record-категории и ломает воспроизведение]** → общий
  хелпер и возврат в playback после stop/cancel; проверить воспроизведение сразу после записи.
- **[Симулятор не видит микрофон]** → проверка на устройстве; в отчёте указать, что именно
  проверено на симуляторе, а что на железе.
- **[Двойное JPEG-кодирование ухудшает качество]** → quality 0.95 и лимит 5 МБ; при
  необходимости заменить на передачу исходных байтов без изменения контракта.
- **[Native-код сложнее сопровождать, чем библиотеку]** → код небольшой, использует
  системные API и уже существующий `IosAudioPlayer`; решение и причина записаны в `DEBT.md`.

## Migration Plan

Миграция данных не требуется. Откат — вернуть заглушки `IosImagePicker`/`IosAudioRecorder`,
убрать `NSMicrophoneUsageDescription` и host-attach; API, БД и common-UI не затрагиваются.

## Open Questions

Нет. Выбор native fallback подтверждён; камера и мультивыбор в scope не входят.
