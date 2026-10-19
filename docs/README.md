# docs/ — документация проекта

Индекс документов ChatShop. Точка входа для агента — [`../AGENTS.md`](../AGENTS.md);
README репозитория — [`../README.md`](../README.md).

| Файл | Назначение |
|---|---|
| [`PROJECT.md`](PROJECT.md) | Что за проект, домен, стек, структура модулей, текущий статус. |
| [`PLAN.md`](PLAN.md) | Архитектурный план: этапы, поток данных, схема БД, дерево компонентов, решения и риски. |
| [`CODING_RULES.md`](CODING_RULES.md) | Обязательные правила кода, компонентов и слоёв. |
| [`AGENT_ROLE.md`](AGENT_ROLE.md) | Роль, экспертиза и процесс работы агента. |
| [`CLIENT_GUIDE.md`](CLIENT_GUIDE.md) | Семантика backend-контракта: auth, лента, заказы, файлы, поллинг. |
| [`openapi.yaml`](openapi.yaml) | OpenAPI-контракт ChatShop API (несмотря на имя и расширение — это не конфиг приложения). |
| [`DEBT.md`](DEBT.md) | Реестр технического долга и осознанных компромиссов. |

## OpenSpec

- [`../openspec/specs`](../openspec/specs) — baseline-спеки по возможностям системы.
- [`../openspec/changes`](../openspec/changes) — активные изменения (proposal → specs → design → tasks).
- Проверка: `openspec validate --specs`, `openspec validate --changes`.

## Приоритет источников

1. Исполняемая конфигурация: `settings.gradle.kts`, `build.gradle.kts`,
   `gradle/libs.versions.toml`, `shared/build.gradle.kts`, код и задачи Gradle.
2. `../AGENTS.md`.
3. `PLAN.md` / `CLIENT_GUIDE.md` / `CODING_RULES.md`.
4. `PROJECT.md` и `AGENT_ROLE.md` — описание и процесс; при расхождении верить пунктам 1–3.

## Быстрые команды

```bash
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

Подробности, архитектура и правила — в [`../AGENTS.md`](../AGENTS.md).
