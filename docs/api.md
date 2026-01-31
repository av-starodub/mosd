# API

## Общая информация
- Базовый путь: `/api/v1`
- Формат: JSON
- Аутентификация: отсутствует (MVP)
- Swagger (dev‑профиль): `/swagger-ui.html`
- OpenAPI JSON (dev‑профиль): `/v3/api-docs`

## Формат ошибок
```json
{
  "timestamp": "2026-01-31T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/space/processes",
  "fieldErrors": [
    {
      "field": "operations[0].name",
      "message": "must not be blank"
    }
  ]
}
```

## Space

### Процессы
#### Получить актуальные (latest) процессы
`GET /api/v1/space/processes`

Параметры:
- `type` — фильтр по типу процесса (process type key)
- `page` — номер страницы (с 0, по умолчанию 0)
- `size` — размер страницы (по умолчанию 50)
- `sortBy` — поле сортировки: `processKey`, `type`, `version`, `createdAt`, `updatedAt`
- `sortDir` — направление сортировки: `asc` или `desc`

Пример:
```
GET /api/v1/space/processes?type=CUT&sortBy=processKey&sortDir=asc&page=0&size=20
```

Пример ответа:
```json
[
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
]
```

#### Получить latest‑версию процесса
`GET /api/v1/space/processes/{processKey}`

Пример ответа:
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

#### Получить все версии процесса
`GET /api/v1/space/processes/{processKey}/versions`

Пример ответа:
```json
[
  {
    "id": "65c0f0b1f0a8e20f3a2a1001",
    "processKey": "CUT-01",
    "version": 1,
    "type": "CUT",
    "description": "Cutting process v1",
    "volumeUnit": "m",
    "handoffMode": "BATCHED",
    "batchVolume": 50,
    "operations": [
      {"name": "op-1", "executionTime": 5}
    ],
    "rateOptions": [
      {"key": "STD", "title": "Standard", "rate": {"units": 3, "minutes": 2}}
    ],
    "createdAt": "2026-01-31T09:00:00Z",
    "updatedAt": "2026-01-31T09:00:00Z"
  },
  {
    "id": "65c0f0b1f0a8e20f3a2a1002",
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
]
```

#### Получить конкретную версию процесса
`GET /api/v1/space/processes/{processKey}/versions/{version}`

Пример ответа:
```json
{
  "id": "65c0f0b1f0a8e20f3a2a1002",
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

#### Получить все версии всех процессов
`GET /api/v1/space/processes/versions`

Пример ответа:
```json
[
  {
    "id": "65c0f0b1f0a8e20f3a2a1001",
    "processKey": "CUT-01",
    "version": 1,
    "type": "CUT",
    "description": "Cutting process v1",
    "volumeUnit": "m",
    "handoffMode": "BATCHED",
    "batchVolume": 50,
    "operations": [
      {"name": "op-1", "executionTime": 5}
    ],
    "rateOptions": [
      {"key": "STD", "title": "Standard", "rate": {"units": 3, "minutes": 2}}
    ],
    "createdAt": "2026-01-31T09:00:00Z",
    "updatedAt": "2026-01-31T09:00:00Z"
  },
  {
    "id": "65c0f0b1f0a8e20f3a2a2001",
    "processKey": "WELD-01",
    "version": 1,
    "type": "WELD",
    "description": "Welding process v1",
    "volumeUnit": "pcs",
    "handoffMode": "FULL_ONLY",
    "batchVolume": null,
    "operations": [
      {"name": "op-1", "executionTime": 8}
    ],
    "rateOptions": [
      {"key": "STD", "title": "Standard", "rate": {"units": 1, "minutes": 2}}
    ]
  }
]
```

#### Создать процесс (версия 1)
`POST /api/v1/space/processes`

Пример запроса:
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

Пример ответа:
```json
{
  "id": "65c0f0b1f0a8e20f3a2a1001",
  "processKey": "CUT-01",
  "version": 1,
  "type": "CUT",
  "description": "Cutting process v1",
  "volumeUnit": "m",
  "handoffMode": "BATCHED",
  "batchVolume": 50,
  "operations": [
    {"name": "op-1", "executionTime": 5}
  ],
  "rateOptions": [
    {"key": "STD", "title": "Standard", "rate": {"units": 3, "minutes": 2}}
  ],
  "createdAt": "2026-01-31T09:00:00Z",
  "updatedAt": "2026-01-31T09:00:00Z"
}
```

#### Создать новую версию (v+1)
`POST /api/v1/space/processes/{processKey}/versions`

Пример запроса:
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

Пример ответа:
```json
{
  "id": "65c0f0b1f0a8e20f3a2a1002",
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

#### Удалить процесс (все версии)
`DELETE /api/v1/space/processes/{processKey}`

### Статусы ответа (Space)
- `200 OK` — успешное получение данных
- `201 Created` — ресурс создан
- `204 No Content` — ресурс удалён
- `400 Bad Request` — ошибка валидации
- `404 Not Found` — ресурс не найден
- `409 Conflict` — нарушение уникальности или ресурс занят

### Ограничения и валидация
- `processKey` уникален и неизменяем.
- `type` должен существовать в справочнике типов (`ProcessType.type`).
- `volumeUnit` не пустой.
- `operations[].name` не пустой и уникален внутри процесса.
- `rateOptions[].key` уникальны внутри процесса.
- `rateOptions` должен содержать минимум один вариант.
- `executionTime`, `rateOptions[].rate.units`, `rateOptions[].rate.minutes` должны быть > 0.
- если `handoffMode = BATCHED` → `batchVolume` обязателен и > 0.
- если `handoffMode = FULL_ONLY` → `batchVolume` должен быть `null`.

Примечание:
- `handoffMode = FULL_ONLY` означает, что следующий процесс стартует только после полного завершения текущего.
- `handoffMode = BATCHED` означает, что результат можно передавать порциями по `batchVolume`.

### Типы процессов
#### Получить список типов
`GET /api/v1/space/process-types`

Пример ответа:
```json
[
  {
    "id": "65c0f0b1f0a8e20f3a2a2001",
    "type": "CUT",
    "title": "Cutting",
    "createdAt": "2026-01-31T09:00:00Z",
    "updatedAt": "2026-01-31T09:00:00Z"
  }
]
```

#### Получить тип по ключу
`GET /api/v1/space/process-types/{type}`

Пример ответа:
```json
{
  "id": "65c0f0b1f0a8e20f3a2a2001",
  "type": "CUT",
  "title": "Cutting",
  "createdAt": "2026-01-31T09:00:00Z",
  "updatedAt": "2026-01-31T09:00:00Z"
}
```

#### Создать тип
`POST /api/v1/space/process-types`

Пример запроса:
```json
{
  "type": "CUT",
  "title": "Cutting"
}
```

Пример ответа:
```json
{
  "id": "65c0f0b1f0a8e20f3a2a2001",
  "type": "CUT",
  "title": "Cutting",
  "createdAt": "2026-01-31T09:00:00Z",
  "updatedAt": "2026-01-31T09:00:00Z"
}
```

#### Удалить тип (если нет процессов)
`DELETE /api/v1/space/process-types/{type}`
