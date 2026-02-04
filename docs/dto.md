# DTO (JSON схемы)

## Metadata
- Status: current (MVP)
- Source of truth: DTO records в `src/main/java/io/github/avstarodub/mosd/*/api/dto`
- Last updated: 2026-02-04

Примечание:
- схемы ниже — примеры структуры;
- обязательность полей и ограничения смотри в аннотациях DTO (`@NotBlank`, `@Positive`, etc).

## Space

### ProcessCreateRequest
```json
{
  "processKey": "PROC-01",
  "type": "TYPE-A",
  "description": "Process v1",
  "volumeUnit": "unit",
  "batchVolume": 50,
  "operations": [
    {
      "operationType": "OP-TYPE-A",
      "name": "operation-1",
      "quantity": 4,
      "timeOptions": [
        {"key": "OPT-A", "title": "Option A", "duration": 6},
        {"key": "OPT-B", "title": "Option B", "duration": 9}
      ]
    }
  ],
  "rateOptions": [
    {"key": "STD", "title": "Standard", "rate": {"units": 3, "minutes": 2}}
  ]
}
```

### ProcessNewVersionRequest
```json
{
  "type": "TYPE-A",
  "description": "Process v2",
  "volumeUnit": "unit",
  "batchVolume": 40,
  "operations": [
    {
      "operationType": "OP-TYPE-A",
      "name": "operation-1",
      "quantity": 4,
      "timeOptions": [
        {"key": "OPT-A", "title": "Option A", "duration": 5},
        {"key": "OPT-B", "title": "Option B", "duration": 8}
      ]
    }
  ],
  "rateOptions": [
    {"key": "STD", "title": "Standard", "rate": {"units": 4, "minutes": 2}}
  ]
}
```

### ProcessResponse
```json
{
  "id": "65c0f0b1f0a8e20f3a2a1001",
  "processKey": "PROC-01",
  "version": 2,
  "type": "TYPE-A",
  "description": "Process v2",
  "volumeUnit": "unit",
  "batchVolume": 40,
  "operations": [
    {
      "operationType": "OP-TYPE-A",
      "name": "operation-1",
      "quantity": 4,
      "timeOptions": [
        {"key": "OPT-A", "title": "Option A", "duration": 5}
      ]
    }
  ],
  "rateOptions": [
    {"key": "STD", "title": "Standard", "rate": {"units": 4, "minutes": 2}}
  ],
  "createdAt": "2026-02-03T10:00:00Z",
  "updatedAt": "2026-02-03T10:05:00Z"
}
```

### Process list filter (query param)
- `type` (optional)

### ProcessTypeCreateRequest
```json
{
  "type": "TYPE-A",
  "title": "Type A"
}
```

### ProcessTypeResponse
```json
{
  "id": "65c0f0b1f0a8e20f3a2a2001",
  "type": "TYPE-A",
  "title": "Type A",
  "createdAt": "2026-02-03T10:00:00Z",
  "updatedAt": "2026-02-03T10:00:00Z"
}
```

### OperationTypeCreateRequest
```json
{
  "type": "OP-TYPE-A",
  "title": "Operation Type A",
  "processType": "TYPE-A",
  "defaultTimeOptions": [
    {"key": "OPT-A", "title": "Option A", "duration": 6},
    {"key": "OPT-B", "title": "Option B", "duration": 9}
  ]
}
```

### OperationTypeResponse
```json
{
  "id": "65c0f0b1f0a8e20f3a2a3001",
  "type": "OP-TYPE-A",
  "title": "Operation Type A",
  "processType": "TYPE-A",
  "defaultTimeOptions": [
    {"key": "OPT-A", "title": "Option A", "duration": 6},
    {"key": "OPT-B", "title": "Option B", "duration": 9}
  ],
  "createdAt": "2026-02-03T10:00:00Z",
  "updatedAt": "2026-02-03T10:00:00Z"
}
```

## Scheduler estimate

### EstimateRequest
```json
{
  "orderId": "ORD-001",
  "items": [
    {
      "externalId": "ITEM-1",
      "processSelections": [
        {
          "processKey": "PROC-01",
          "version": 2,
          "volume": 120,
          "volumeUnit": "unit",
          "batchVolumeOverride": 40,
          "rateSelectionMode": "OPTION",
          "selectedRateOptionKey": "STD",
          "operationSelections": [
            {"operationName": "operation-1", "selectedTimeOptionKey": "OPT-A"}
          ]
        }
      ]
    }
  ]
}
```

Примечание:
- `version` optional. Если не передан — берётся latest версия процесса.
- `batchVolumeOverride` optional. Если передан — имеет приоритет над `process.batchVolume`.
- `operationSelections` может содержать подмножество операций процесса.
- Выбор скорости задаётся через XOR-контракт:
  - `rateSelectionMode = OPTION` -> нужен `selectedRateOptionKey`, `rateOverride` должен быть пуст;
  - `rateSelectionMode = OVERRIDE` -> нужен `rateOverride { units, minutes, reason }`, `selectedRateOptionKey` должен быть пуст.

### EstimateResponse
```json
{
  "orderId": "ORD-001",
  "totalMinutes": 70,
  "items": [
    {
      "externalId": "ITEM-1",
      "estimatedMinutes": 40,
      "processes": [
        {
          "processKey": "PROC-01",
          "version": 2,
          "estimatedMinutes": 40,
          "batchCount": 3,
          "appliedBatchVolume": 40,
          "lastBatchVolume": 40,
          "setupMinutes": 6,
          "productionMinutes": 34,
          "rateSelectionUsed": {
            "mode": "OPTION",
            "option": {"key": "STD", "title": "Standard", "units": 10, "minutes": 8},
            "override": null
          },
          "operations": [
            {"operationName": "operation-1", "selectedTimeOptionKey": "OPT-A", "durationMinutes": 6}
          ]
        }
      ]
    }
  ],
  "queues": [
    {
      "processKey": "PROC-01",
      "totalMinutes": 70,
      "batches": [
        {"itemExternalId": "ITEM-1", "batchNumber": 1, "startMinute": 0, "endMinute": 14, "durationMinutes": 14}
      ]
    }
  ]
}
```

Примечание:
- `rateSelectionUsed` в response самодостаточен:
  - `OPTION` -> заполнен `option { key, title, units, minutes }`;
  - `OVERRIDE` -> заполнен `override { units, minutes, reason }`.

## ErrorResponse
```json
{
  "timestamp": "2026-02-03T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/scheduler/estimate",
  "fieldErrors": [
    {"field": "items[0].processSelections[0].volume", "message": "must be greater than 0"}
  ]
}
```

## Archive (планируемый контракт)

### CalculationArchiveDocument (self-contained)
Один документ, достаточный для повторного запуска/аудита без отдельного хранения исходного request.

```json
{
  "calculationId": "CALC-2026-02-04-0001",
  "orderId": "ORD-001",
  "calculatedAt": "2026-02-04T17:30:00Z",
  "algorithmVersion": "estimate-v1",
  "input": {
    "items": [
      {
        "externalId": "ITEM-1",
        "processSelections": [
          {
            "processKey": "PROC-01",
            "versionUsed": 2,
            "volume": 120,
            "volumeUnit": "unit",
            "batchVolumeOverride": 40,
            "rateSelectionUsed": {
              "mode": "OVERRIDE",
              "option": null,
              "override": {"units": 5, "minutes": 3, "reason": "historical actual throughput"}
            },
            "operationSelectionsUsed": [
              {"operationName": "operation-1", "selectedTimeOptionKey": "OPT-A"}
            ]
          }
        ]
      }
    ]
  },
  "result": {
    "totalMinutes": 70,
    "items": [
      {
        "externalId": "ITEM-1",
        "estimatedMinutes": 40,
        "processes": [
          {
            "processKey": "PROC-01",
            "version": 2,
            "estimatedMinutes": 40,
            "batchCount": 3,
            "appliedBatchVolume": 40,
            "lastBatchVolume": 40,
            "setupMinutes": 6,
            "productionMinutes": 34,
            "rateSelectionUsed": {
              "mode": "OVERRIDE",
              "option": null,
              "override": {"units": 5, "minutes": 3, "reason": "historical actual throughput"}
            }
          }
        ]
      }
    ],
    "queues": [
      {
        "processKey": "PROC-01",
        "totalMinutes": 70,
        "batches": [
          {"itemExternalId": "ITEM-1", "batchNumber": 1, "startMinute": 0, "endMinute": 14, "durationMinutes": 14}
        ]
      }
    ]
  }
}
```

Примечания:
- `input` и `result` хранятся в одном документе.
- `rateSelectionUsed` обязателен в `input` и `result`, чтобы исключить потерю контекста скорости.
- `versionUsed` фиксирует конкретную версию процесса, применённую в расчёте.
