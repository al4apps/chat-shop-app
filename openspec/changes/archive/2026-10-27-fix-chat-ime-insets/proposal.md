# Proposal

## Why

На Android клавиатура иногда оставляет ложный отступ сверху размером с собственную высоту, а на экране первого заказа поле ввода может оказаться под клавиатурой. Сейчас IME-отступы учитываются в разных местах по-разному: `Scaffold` в Home, `imePadding` в Dialog, `safeContentPadding + imePadding` в Auth и отсутствие IME-обработки в FirstOrder. Это ломает основной сценарий чата.

## What Changes

- Оставить системные insets Home на `Scaffold`; IME-отступ применять только к `MessageInputBar`.
- Убрать `imePadding()` с корневого Column в `DialogScreen` и добавить его на `MessageInputBar` в Dialog и FirstOrder.
- Убрать двойной `safeContentPadding + imePadding` в Auth; оставить один источник для root-экранов.
- Добавить `android:windowSoftInputMode="adjustResize"`, чтобы система не панорамировала окно.
- Добавить в `design-system` требование к поведению при открытой клавиатуре.
- Проверить Dialog, FirstOrder, Auth phone/code: поле ввода видно, TopBar не уезжает, нет ложного верхнего отступа, «Назад» сначала закрывает клавиатуру.
- Закрыть `CHAT-IME-1` в `docs/BACKLOG.md` после проверки.
- Не менять API, БД, навигацию и бизнес-логику.

## Capabilities

### New Capabilities

Нет.

### Modified Capabilities

- `design-system`: добавляется требование к IME/клавиатуре и единому учёту insets на экранах с вводом.

## Impact

- `androidApp/src/main/AndroidManifest.xml` — `windowSoftInputMode`.
- `DialogScreen`, `FirstOrderScreen` — IME-обработка панели ввода.
- `PhoneScreen`, `CodeScreen` — убрать двойной IME-padding.
- `openspec/specs/design-system/spec.md` — delta-спека.
- `docs/BACKLOG.md` — закрытие `CHAT-IME-1`.
- API, БД, зависимости и бизнес-логика не затрагиваются.
