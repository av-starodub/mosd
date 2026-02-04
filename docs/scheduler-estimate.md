# Scheduler Estimate (без постановки в план)

## Metadata
- Status: current (MVP)
- Source of truth: `EstimateService` в `src/main/java/io/github/avstarodub/mosd/scheduler/app/EstimateService.java`
- Last updated: 2026-02-04

## Назначение
`POST /api/v1/scheduler/estimate` рассчитывает ожидаемое время выполнения для входного набора items.

Это виртуальный расчёт:
- не создаёт/не обновляет внешние сущности;
- не сохраняет результат в БД;
- не строит и не изменяет реальный план.

## Контракт входа
- `orderId` обязателен.
- `items` обязателен, минимум 1.
- `item.processSelections` обязателен, минимум 1, порядок процессов важен.
- `processKey` обязателен.
- `version` optional:
  - если передан -> используется конкретная версия;
  - если не передан -> используется latest.
- `batchVolumeOverride` optional:
  - если передан -> используется вместо `process.batchVolume`;
  - если не передан -> используется `process.batchVolume`.
- `volume > 0`, `volumeUnit` должен совпадать с unit процесса.
- `rateSelectionMode` обязателен (`OPTION | OVERRIDE`).
- XOR-контракт выбора скорости:
  - `OPTION` -> обязателен `selectedRateOptionKey`, `rateOverride` отсутствует;
  - `OVERRIDE` -> обязателен `rateOverride` (`units`, `minutes`, `reason`), `selectedRateOptionKey` отсутствует.
- `operationSelections` обязателен и должен ссылаться на операции/`timeOption` из процесса.
- `operationSelections` может быть подмножеством операций процесса (передавать все операции не требуется).

## Формула расчёта времени
Для item-process:
- `setupMinutes = sum(selected operation time option durations)` (ровно один раз).
- `rateMinutes_i = ceil(batchVolume_i / rate.units) * rate.minutes` (для каждой партии `i`).
- `productionMinutes = sum(rateMinutes_i)` (сумма изготовления всех партий).
- `estimatedMinutes = setupMinutes + productionMinutes`.

Для виртуального расписания очереди:
- первая партия в очереди процесса: `duration_1 = setupMinutes + rateMinutes_1`;
- остальные партии: `duration_n = rateMinutes_n`.

Важно:
- для последней (remainder) партии длительность считается отдельно по её фактическому объёму;
- `operation.quantity` в текущей версии используется как атрибут модели процесса и не участвует в формуле длительности.

## Алгоритм расчёта
Используется event-driven симуляция:
1. Для каждого item формируются партии первого процесса.
2. У каждого `processKey` есть своя очередь и момент освобождения ресурса.
3. Партия стартует в `max(processAvailableAt, readyAt)`.
4. По завершению партии она ставится в очередь следующего процесса item.
5. Расчёт заканчивается, когда очередь событий пуста.

## Политика очередей и детерминизм
- Очередь процесса: FIFO по времени поступления.
- Порядок событий фиксирован comparator'ом:
  - `endMinute`
  - `itemOrder`
  - `batchNumber`
  - `processIndex`
  - `processKey`
  - `itemExternalId`

Это обеспечивает стабильный результат при одинаковом входе.

## Ответ
Возвращается:
- `totalMinutes` по входному набору,
- оценка по каждому item и каждому процессу,
- виртуальные очереди по processKey с партиями (`startMinute`, `endMinute`, `durationMinutes`).

Для process в ответе:
- `setupMinutes` и `productionMinutes` разделены явно;
- `estimatedMinutes` всегда равен `setupMinutes + productionMinutes`.
- `rateSelectionUsed` показывает фактически использованный выбор скорости:
  - `OPTION` + `option` (`key`, `title`, `units`, `minutes`), либо
  - `OVERRIDE` + `override` (`units`, `minutes`, `reason`).
- `appliedBatchVolume` показывает фактически применённый размер партии.
- `lastBatchVolume` показывает объём последней партии (если меньше `appliedBatchVolume`, значит последняя партия неполная).

## Границы текущего решения
Estimate — отдельный режим модуля Scheduler для оценки времени.
Реальный план и архив — отдельные сценарии MVP, вне текущего endpoint.
