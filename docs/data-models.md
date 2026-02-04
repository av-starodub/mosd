# Модели данных

## Metadata
- Status: current (MVP)
- Source of truth: domain records + Mongo documents в `src/main/java/io/github/avstarodub/mosd/space`
- Last updated: 2026-02-04

## Process
- `processKey` — уникальный неизменяемый ключ процесса.
- `version` — версия процесса (целое > 0).
- `type` — ссылка на `ProcessType.type`.
- `description` — описание (optional).
- `volumeUnit` — единица объёма.
- `batchVolume` — размер партии для передачи в следующий процесс (> 0, обязателен).
- `operations` — список операций процесса.
- `rateOptions` — варианты скорости процесса.
- `createdAt`, `updatedAt` — timestamps.

Ограничения:
- в БД уникальна пара `processKey + version` (индекс `process_key_version_unique`);
- `processKey` уникален как идентификатор процесса на сценарии создания новой сущности (version=1).
- `operations[].name` уникален внутри одного процесса.
- `operations[].timeOptions[].key` уникален внутри одной операции.
- `rateOptions[].key` уникален внутри одного процесса.
- `rateOptions` не пустой.
- `batchVolume` обязателен и > 0.

## Operation
- `operationType` — ссылка на `OperationType.type` (optional).
- `name` — отображаемое имя операции.
- `quantity` — количество однотипных операций в процессе (> 0).
- `timeOptions` — варианты длительности операции.

## TimeOption
- `key` — ключ варианта.
- `title` — название варианта.
- `duration` — длительность в минутах (> 0).

## RateOption
- `key` — ключ варианта скорости.
- `title` — название варианта.
- `rate.units` — количество единиц.
- `rate.minutes` — количество минут.

Формула скорости:
- минуты на объём считаются как `ceil(volume / units) * minutes`.

## ProcessType
- `type` — ключ типа процесса (уникальный).
- `title` — отображаемое название.
- `createdAt`, `updatedAt`.

## OperationType
- `type` — ключ типа операции.
- `title` — отображаемое название.
- `processType` — ссылка на `ProcessType.type`.
- `defaultTimeOptions` — типовые варианты длительности.
- `createdAt`, `updatedAt`.

MVP-правило:
- update/rename `OperationType` не поддерживается; только create/get/list.
