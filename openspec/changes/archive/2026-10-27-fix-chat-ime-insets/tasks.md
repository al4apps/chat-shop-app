# Tasks

## 1. Insets Implementation

- [x] 1.1 Добавить `android:windowSoftInputMode="adjustResize"` для `MainActivity` в `AndroidManifest.xml`; проверить, что манифест собирается и окно не панорамируется.
- [x] 1.2 Убрать `imePadding()` с корневого Column в `DialogScreen` и передать `Modifier.imePadding()` в `MessageInputBar`; проверить, что TopBar и лента не сдвигаются, а поле ввода поднимается над клавиатурой.
- [x] 1.3 Добавить `Modifier.imePadding()` к `MessageInputBar` на `FirstOrderScreen`; проверить, что поле ввода не перекрывается клавиатурой.
- [x] 1.4 Убрать `imePadding()` из `PhoneScreen` и `CodeScreen`, оставив `safeContentPadding()`; проверить, что поля не получают двойной отступ и остаются видимыми.

## 2. Documentation

- [x] 2.1 Проверить delta-спеку `design-system` в change: требование «Клавиатура и IME-инсеты» и сценарии Dialog/FirstOrder/Auth/back; `openspec validate "fix-chat-ime-insets" --type change` — valid.
- [x] 2.2 После проверки закрыть `CHAT-IME-1` в `docs/BACKLOG.md`: указать change, тестовую матрицу и результат.

## 3. Verification

- [x] 3.1 Собрать Android и прогнать тесты: `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug`; проверить успешный exit code.
- [x] 3.2 Вручную проверить на Android-эмуляторе/устройстве: Dialog — открыть/закрыть клавиатуру, отправить сообщение, TopBar и лента не прыгают; FirstOrder — поле ввода видно; Auth phone/code — нет двойного отступа.
- [x] 3.3 Проверить «Назад» при открытой клавиатуре: первое нажатие закрывает клавиатуру, второе — экран; после закрытия нет остаточного отступа.
- [x] 3.4 Проверить сборку iOS: `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`; убедиться, что изменения Compose не ломают iOS-таргет.
- [x] 3.5 Проверить `git diff`: изменения только в манифесте, Home/Dialog/FirstOrder/Auth UI, спеке и backlog; API, БД и бизнес-логика не затронуты.
- [x] 3.6 Запустить `openspec validate --specs` и `openspec validate "fix-chat-ime-insets" --type change`; оба должны пройти.

## Workflow follow-up

- После приёмки архивировать change: `/opsx-archive`.
- Следующий шаг — `implement-order-statuses` (этап 11).
