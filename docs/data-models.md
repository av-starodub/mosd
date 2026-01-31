# Модели данных

## Process
- `processKey` — уникальный, неизменяемый ключ процесса
- `version` — версия процесса
- `type` — ссылка на `ProcessType`
- `description` — описание (опционально)
- `volumeUnit` — единица объёма работ/продукции
- `handoffMode` — режим передачи результата (`FULL_ONLY` | `BATCHED`)
- `batchVolume` — объём порции, после которой возможна передача в следующий процесс (только для `BATCHED`)
- `operations` — список операций
- `rateOptions` — набор вариантов скорости (выбор на клиенте)
- `createdAt`, `updatedAt` — метки времени

Ограничения:
- `processKey` уникален в базе.
- `operations[].name` уникальны внутри процесса.
- `rateOptions[].key` уникальны внутри процесса.
- `rateOptions` содержит минимум один вариант.
- `executionTime` и значения `rateOptions[].rate` положительные.
- если `handoffMode = BATCHED` → `batchVolume` обязателен и > 0.
- если `handoffMode = FULL_ONLY` → `batchVolume` должен быть `null`.

Примечание:
- `volumeUnit` должен быть одинаковым для объёма заказа, `batchVolume` и скоростей `rateOptions`.

## ProcessType
- `type` — ключ типа процесса
- `title` — отображаемое название
Ограничения:
- `type` уникален в базе.

## Operation
- `name` — название операции
- `executionTime` — время выполнения операции в минутах

## Rate
- `units` — количество единиц
- `minutes` — время в минутах

## RateOption
- `key` — ключ варианта скорости
- `title` — отображаемое название
- `rate` — скорость (units/min)
