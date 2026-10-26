# Proposal

## Why

В Pencil принята дизайн-система v2: более контрастная синяя палитра, Manrope и более собранная типографика. В коде приложения до сих пор используются старые цвета и системный шрифт, поэтому следующие UI-фичи (включая этап 11 — статусы заказа) пришлось бы собирать на устаревшей визуальной основе и затем переделывать.

## What Changes

- Обновить `AppColors`: палитра v2, включая `accentSoft` для мягких акцентов и статусных подложек.
- Перевести `AppTypography` на Manrope с весами 400/500/600/700; добавить `display` для крупных акцентов.
- Добавить шрифтовые ресурсы Manrope в `composeResources/font/` с лицензией OFL.
- Расширить `AppShapes` радиусами для карточек (20/24), не меняя существующие радиусы кнопок и пузырей.
- Подогнать базовые виджеты (`AppButton`, `AppTextField`, `AppTopBar`, `AppIconButton`, `MessageInputBar`) под v2: размеры, веса, цвета, фокус и отступы.
- Добавить `AppBadge` — компактный статусный бейдж для списка чатов и статусов заказа.
- Обновить маппинг Material 3 color scheme на новые токены.
- Обновить спецификацию `design-system` под принятые визуальные требования.
- Не менять навигацию, сетевой слой, БД, API и бизнес-логику.

## Capabilities

### New Capabilities

Нет.

### Modified Capabilities

- `design-system`: уточняются требования к палитре, типографике (Manrope), форме базовых виджетов и статусному бейджу.

## Impact

- `shared/src/commonMain/kotlin/com/al4apps/chatshop/core/design/theme/` — `AppColors`, `AppTypography`, `AppShapes`, `AppTheme`.
- `shared/src/commonMain/kotlin/com/al4apps/chatshop/core/design/widgets/` — базовые виджеты, новый `AppBadge`.
- `shared/src/commonMain/composeResources/font/` — TTF-файлы Manrope и лицензия.
- Все существующие экраны — визуально, без изменения поведения.
- `openspec/specs/design-system/spec.md` — delta-спека в этом change.
- Сборка Android и iOS; проверить, что шрифт и размеры не ломают текущие экраны.
