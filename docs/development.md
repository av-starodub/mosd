# Разработка

## Metadata
- Status: current (MVP)
- Source of truth: `build.gradle.kts`, `docker-compose.yml`, профильные `application-*.yml`
- Last updated: 2026-02-04

## Запуск
- Локальная MongoDB через Docker Compose:
  - `./gradlew composeUp`
  - `./gradlew composeDown`
- Запуск приложения (dev‑профиль + compose):
  - `./gradlew bootDev`

## Профили
- `dev` включает Swagger и автосоздание индексов MongoDB.
- `test` используется в интеграционных тестах (`@IntegrationTest`).

## Swagger
- UI: `/swagger-ui.html`
- OpenAPI: `/v3/api-docs`

## Тесты
- Unit: `./gradlew test`
- Integration: `./gradlew integrationTest`
- Полная проверка: `./gradlew verify`

## DoD: Scheduler estimate контракт
- `estimatedMinutes = setupMinutes + productionMinutes` (setup учитывается ровно 1 раз на item-process).
- `ProcessSelectionRequest` использует XOR-контракт выбора скорости:
  - `OPTION` + `selectedRateOptionKey`;
  - `OVERRIDE` + `rateOverride { units, minutes, reason }`;
  - оба/ни одного -> `400`.
- `ProcessEstimate` возвращает:
  - `batchCount`, `appliedBatchVolume`, `lastBatchVolume`;
  - `rateSelectionUsed` (фактически применённый выбор скорости).
- Тестовые минимумы перед merge:
  - `./gradlew checkstyleMain checkstyleTest`
  - `./gradlew test --tests "*EstimateServiceTest"`
  - `./gradlew integrationTest --tests "*EstimateIntegrationTest"`
