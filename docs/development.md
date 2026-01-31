# Разработка

## Запуск
- Локальная MongoDB через Docker Compose:
  - `./gradlew composeUp`
  - `./gradlew composeDown`
- Запуск приложения (dev‑профиль + compose):
  - `./gradlew bootDev`

## Профили
- `dev` включает Swagger и автосоздание индексов MongoDB.

## Swagger
- UI: `/swagger-ui.html`
- OpenAPI: `/v3/api-docs`

## Тесты
- `./gradlew test`
- `./gradlew verify`
