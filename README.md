# ChatShop

Kotlin Multiplatform (Android + iOS) клиент для chat-commerce: переписка с оператором,
заказ как лента событий.

- Инструкции для агентов: [AGENTS.md](AGENTS.md)
- Документация проекта: [docs/README.md](docs/README.md)
- Контракт API: [docs/openapi.yaml](docs/openapi.yaml), семантика — [docs/CLIENT_GUIDE.md](docs/CLIENT_GUIDE.md)

```bash
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
```
