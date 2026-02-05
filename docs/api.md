# API

## Metadata
- Status: current (MVP)
- Source of truth: controllers в `src/main/java/io/github/avstarodub/mosd/*/api/controller`
- Last updated: 2026-02-05

## Общая информация
- Base path: `/api/v1`
- Формат: JSON
- Аутентификация: отсутствует (MVP)
- Swagger UI (dev): `/swagger-ui.html`
- OpenAPI (dev): `/v3/api-docs`

Подробные JSON-схемы запросов/ответов: [dto.md](dto.md).

## Ошибки
Единый формат ошибки: `ErrorResponse` (см. [dto.md](dto.md)).

Коды:
- `200` — успешно
- `201` — создано
- `400` — ошибка валидации/аргументов
- `404` — не найдено
- `409` — конфликт (дубликат/запрещённый update через create)
- `500` — непредвиденная ошибка

## Space

### Processes
- `POST /api/v1/space/processes`
  - Request: `ProcessCreateRequest`
  - Response: `ProcessResponse`
- `POST /api/v1/space/processes/{processKey}/versions`
  - Request: `ProcessNewVersionRequest`
  - Response: `ProcessResponse`
- `GET /api/v1/space/processes/{processKey}`
  - Response: `ProcessResponse` (latest)
- `GET /api/v1/space/processes?type={type}`
  - Query:
    - `type` (optional) — фильтр latest по типу
  - Response: `List<ProcessResponse>` (latest по `processKey`)
- `GET /api/v1/space/processes/{processKey}/versions`
  - Response: `List<ProcessResponse>` (все версии по ключу, по возрастанию версии)
- `GET /api/v1/space/processes/{processKey}/versions/{version}`
  - Path:
    - `version` > 0
  - Response: `ProcessResponse`

### Process types
- `POST /api/v1/space/process-types`
  - Request: `ProcessTypeCreateRequest`
  - Response: `ProcessTypeResponse`
- `GET /api/v1/space/process-types/{type}`
  - Response: `ProcessTypeResponse`
- `GET /api/v1/space/process-types`
  - Response: `List<ProcessTypeResponse>`

### Operation types
- `POST /api/v1/space/operation-types`
  - Request: `OperationTypeCreateRequest`
  - Response: `OperationTypeResponse`
- `GET /api/v1/space/operation-types/{type}?processType={processType}`
  - Response: `OperationTypeResponse`
- `GET /api/v1/space/operation-types?processType={processType}`
  - Response: `List<OperationTypeResponse>`

MVP-ограничение:
- update/rename для `OperationType` не поддерживается.

## Scheduler

### Estimate
- `POST /api/v1/scheduler/estimate`
  - Request: `EstimateRequest`
  - Response: `EstimateResponse`

Назначение:
- рассчитывает ожидаемое время выполнения для входного набора items;
- строит виртуальные очереди;
- не сохраняет результат и не изменяет реальный план.

Правило `batchVolumeOverride`:
- если `items[].processSelections[].batchVolumeOverride` передан, он имеет приоритет над `process.batchVolume`;
- если не передан, используется `process.batchVolume`.

Правило выбора скорости (`rateSelection`):
- `rateSelectionMode = OPTION` -> используется `selectedRateOptionKey`;
- `rateSelectionMode = OVERRIDE` -> используется `rateOverride { units, minutes, reason }`;
- оба/ни одного не допускаются (валидация `400`).

Алгоритм и ограничения: [scheduler-estimate.md](scheduler-estimate.md).

Примечание:
- в MVP модуль Scheduler также включает сценарий реального плана;
- текущий документ описывает только уже реализованный endpoint estimate.

### Plan (в работе, принципы контракта)
- write-команды (`CHANGE`, `APPEND`) обрабатываются через приоритетные FIFO-очереди;
- read (`GET`) обслуживается отдельно от write-очередей;
- для каждого `planId` действует single-writer;
- write-события требуют `eventId` для дедупликации.

## Archive (планируется)
- Контракт хранения расчёта: `CalculationArchiveDocument` (self-contained, один документ).
- Подробная JSON-схема: [dto.md](dto.md) (раздел Archive).
