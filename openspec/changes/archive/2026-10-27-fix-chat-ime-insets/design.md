# Design

## Context

См. `proposal.md` — Why. `MainActivity` включает edge-to-edge. `HomeContent` оборачивает Home в `Scaffold` с системными insets. `DialogScreen` применяет `imePadding()` ко всему Column, `FirstOrderScreen` не применяет IME вообще, Auth-экраны совмещают `safeContentPadding()` и `imePadding()`. В манифесте не задан `windowSoftInputMode`.

## Goals / Non-Goals

**Goals:**

- Убрать ложный верхний отступ при открытой клавиатуре.
- Держать поле ввода видимым в Dialog и FirstOrder.
- Убрать двойной IME-отступ в Auth.
- Сохранить полноэкранный просмотр изображения без лишних inset-полей.

**Non-Goals:**

- Не менять структуру экранов, навигацию и бизнес-логику.
- Не переводить все экраны на новый общий Scaffold.
- Не менять MessageInputBar как компонент дизайн-системы.
- Не добавлять новые зависимости.

## Decisions

### 1. IME-отступ применяется к панели ввода, а не ко всему экрану

В `DialogScreen` убрать `imePadding()` с корневого Column. Вместо этого передать `Modifier.imePadding()` в `MessageInputBar`. В `FirstOrderScreen` сделать то же.

Почему:

- TopBar и лента не должны сдвигаться вверх/вниз при появлении клавиатуры;
- ложный верхний отступ исчезает, потому что IME больше не применяется к контейнеру, который уже учитывает системные insets;
- список (`weight(1f)`) сам уменьшается на высоту панели ввода с IME-padding;
- полноэкранный просмотр изображения остаётся вне IME-отступа.

Отклонённая альтернатива — `Scaffold(contentWindowInsets = WindowInsets.safeDrawing)` для всего Home: она меняет отступы всех Home-экранов и затрагивает полноэкранный overlay; для точечного бага это шире необходимого.

### 2. Auth использует только `safeContentPadding`

`PhoneScreen` и `CodeScreen` уже применяют `safeContentPadding()`, который включает IME. Дополнительный `imePadding()` удаляется, чтобы не удваивать отступ.

### 3. `windowSoftInputMode="adjustResize"`

В `AndroidManifest.xml` для `MainActivity` задать `android:windowSoftInputMode="adjustResize"`, чтобы система не панорамировала окно (`adjustPan`) и не создавала визуальный сдвиг контента. IME-инсеты остаются источником истины в Compose.

Если на конкретном API/OEM `adjustResize` вместе с `imePadding` даст двойной нижний отступ, приоритет — Compose insets: убрать `adjustResize` и оставить IME-padding на панели ввода.

### 4. Тестовая матрица

- Dialog: открыть/закрыть клавиатуру, отправить сообщение, проверить TopBar и ленту.
- FirstOrder: то же для поля ввода.
- Auth phone/code: клавиатура не должна добавлять двойной отступ.
- «Назад»: первое нажатие закрывает клавиатуру, второе — экран.
- Android API 36/37 и хотя бы один старый API; проверить gesture/nav bar.

## Risks / Trade-offs

- **`adjustResize` конфликтует с edge-to-edge на части устройств** → проверить матрицу; при двойном отступе оставить только Compose `imePadding`.
- **IME-анимация может дёргать список** → `LazyColumn` с `reverseLayout` и `weight` должен компенсировать; проверить визуально.
- **Auth-поля могут оказаться под клавиатурой без `imePadding`** → `safeContentPadding` включает IME; проверить на устройстве.
- **Разные клавиатуры/жесты** → проверить стандартную и gesture navigation; не полагаться только на эмулятор.

## Migration Plan

1. Изменить манифест, Dialog, FirstOrder, Auth.
2. Проверить сборку и ручную матрицу.
3. Обновить delta-спеку `design-system` и закрыть `CHAT-IME-1`.
4. Откат — revert изменений UI; данные и API не затрагиваются.

## Open Questions

Нет. Если `adjustResize` даст двойной отступ, решение уже описано в рисках.
