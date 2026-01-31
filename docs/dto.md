# DTO (JSON схемы)

## Process
### ProcessCreateRequest
```json
{
  "processKey": "CUT-01",
  "type": "CUT",
  "description": "Cutting process v1",
  "volumeUnit": "m",
  "handoffMode": "BATCHED",
  "batchVolume": 50,
  "operations": [
    {"name": "op-1", "executionTime": 5},
    {"name": "op-2", "executionTime": 10}
  ],
  "rateOptions": [
    {"key": "STD", "title": "Standard", "rate": {"units": 3, "minutes": 2}}
  ]
}
```

### ProcessNewVersionRequest
```json
{
  "type": "CUT",
  "description": "Cutting process v2",
  "volumeUnit": "m",
  "handoffMode": "BATCHED",
  "batchVolume": 50,
  "operations": [
    {"name": "op-1", "executionTime": 6}
  ],
  "rateOptions": [
    {"key": "STD", "title": "Standard", "rate": {"units": 2, "minutes": 1}}
  ]
}
```

### ProcessResponse
```json
{
  "id": "65c0f0b1f0a8e20f3a2a1001",
  "processKey": "CUT-01",
  "version": 2,
  "type": "CUT",
  "description": "Cutting process v2",
  "volumeUnit": "m",
  "handoffMode": "BATCHED",
  "batchVolume": 50,
  "operations": [
    {"name": "op-1", "executionTime": 6}
  ],
  "rateOptions": [
    {"key": "STD", "title": "Standard", "rate": {"units": 2, "minutes": 1}}
  ],
  "createdAt": "2026-01-31T10:00:00Z",
  "updatedAt": "2026-01-31T10:10:00Z"
}
```

### ProcessFilterRequest (query params)
- `type` — фильтр по типу процесса
- `page` — номер страницы (с 0)
- `size` — размер страницы
- `sortBy` — поле сортировки: `processKey`, `type`, `version`, `createdAt`, `updatedAt`
- `sortDir` — направление сортировки: `asc` или `desc`

## ProcessType
### ProcessTypeCreateRequest
```json
{
  "type": "CUT",
  "title": "Cutting"
}
```

### ProcessTypeResponse
```json
{
  "id": "65c0f0b1f0a8e20f3a2a2001",
  "type": "CUT",
  "title": "Cutting",
  "createdAt": "2026-01-31T10:00:00Z",
  "updatedAt": "2026-01-31T10:10:00Z"
}
```

## Общие DTO
### OperationRequest / OperationResponse
```json
{"name": "op-1", "executionTime": 5}
```

### RateRequest / RateResponse
```json
{"units": 3, "minutes": 2}
```

### RateOptionRequest / RateOptionResponse
```json
{"key": "STD", "title": "Standard", "rate": {"units": 3, "minutes": 2}}
```

### HandoffMode
Допустимые значения:
```
FULL_ONLY
BATCHED
```

### ErrorResponse
```json
{
  "timestamp": "2026-01-31T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/space/processes",
  "fieldErrors": [
    {"field": "operations[0].name", "message": "must not be blank"}
  ]
}
```
