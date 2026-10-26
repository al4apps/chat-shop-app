# Tasks

## 1. Font Assets

- [x] 1.1 Добавить в `shared/src/commonMain/composeResources/font/` статические Manrope Regular, Medium, SemiBold, Bold и файл лицензии OFL; проверить, что все четыре TTF и лицензия присутствуют.
- [x] 1.2 Проверить генерацию ресурсов командой `./gradlew :shared:generateComposeResClass`; убедиться, что `Res.font.*` для Manrope создан без ошибок.

## 2. Theme Tokens

- [x] 2.1 Обновить `AppColors` значениями v2 и добавить `accentSoft`; проверить компиляцию и отсутствие старых hex-значений в теме.
- [x] 2.2 Перевести `AppTypography` на `FontFamily` Manrope с весами 400/500/600/700 и шкалой Display/H1/H2/Title/Body/Caption/Button; проверить, что `AppTheme` передаёт Manrope, а `AppTypography()` без аргумента использует безопасный fallback.
- [x] 2.3 Добавить в `AppShapes` радиусы `extraLarge = 20` и `extraExtraLarge = 24`, не меняя `small`, `medium`, `large`; проверить, что существующие вызовы `AppTheme.shapes` не изменили значения.
- [x] 2.4 Обновить `AppTheme`: создать Manrope `FontFamily` из ресурсов, передать в `AppTypography` и замапить `accentSoft` в `secondaryContainer`/`onSecondaryContainer`; проверить, что приложение собирается и тема открывается.

## 3. Base Widgets

- [x] 3.1 Привести `AppButton` к v2: Manrope SemiBold 16/20, высота 52, радиус 12, новые accent/disabled-цвета; проверить primary, secondary, text и disabled на витрине.
- [x] 3.2 Привести `AppTextField` к v2: высота 56, радиус 12, Manrope, accent-фокус 2 px, error-рамка и supporting text; проверить на экранах авторизации и в диалоге.
- [x] 3.3 Привести `AppTopBar` и `AppIconButton` к v2: высота 56, заголовок 17/22 SemiBold, визуальные действия 40×40 при touch target не меньше 48; проверить заголовок с длинным текстом и кнопку «Назад».
- [x] 3.4 Обновить `MessageInputBar` только токенами и типографикой, не меняя структуру «скрепка + поле + действие»; проверить в диалоге и на первом заказе.
- [x] 3.5 Добавить `AppBadge` в `core/design/widgets` и показать его в `features/example`; проверить компактную ширину, accentSoft-фон и отсутствие растягивания.

## 4. Documentation

- [x] 4.1 Обновить раздел дизайн-системы в `docs/PLAN.md` §6: палитра v2, Manrope, шкала типографики, новые радиусы и `AppBadge`; проверить, что значения совпадают с кодом.
- [x] 4.2 Проверить, что `openspec validate "adopt-design-system-v2" --type change` проходит после правок спеки.

## 5. Verification

- [x] 5.1 Собрать Android и прогнать тесты: `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug`; проверить успешный exit code.
- [x] 5.2 Собрать iOS: `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`; проверить, что шрифты попали в framework и сборка успешна.
- [x] 5.3 Пройти визуальный чек-лист на Android-эмуляторе и iOS-симуляторе: Onboarding, Auth (телефон/код), ChatList, FirstOrder, Dialog; проверить отсутствие обрезки текста, достаточный контраст и корректные размеры кнопок/полей.
- [x] 5.4 Проверить, что LICENSE/OFL для Manrope включён в поставку и указан в документации.

## Workflow follow-up

- После приёмки change выполнить `/opsx-archive` или `openspec archive adopt-design-system-v2`, чтобы перенести delta-спеку `design-system` в main specs.
- Следующим change-ом запланировать `implement-order-statuses` (этап 11) уже на новой дизайн-системе.
