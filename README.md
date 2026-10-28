# ChatShop

Kotlin Multiplatform (Android + iOS) клиент для chat-commerce: переписка с оператором,
заказ как лента событий.

- Инструкции для агентов: [AGENTS.md](AGENTS.md)
- Документация проекта: [docs/README.md](docs/README.md)
- Спеки (OpenSpec): [openspec/specs](openspec/specs)
- Контракт API: [docs/openapi.yaml](docs/openapi.yaml), семантика — [docs/CLIENT_GUIDE.md](docs/CLIENT_GUIDE.md)

```bash
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
```

### iOS

```bash
make ios          # собрать, установить и запустить на симуляторе
make ios-build    # только сборка
make ios-run      # установить и запустить без пересборки
make ios-devices  # список доступных симуляторов
```

Конкретный симулятор:

```bash
make ios SIM_UDID=<udid>
make ios SIM_NAME="iPhone 17 Pro"
```
