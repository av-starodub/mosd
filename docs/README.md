# Документация MOSD

## Metadata
- Status: current (MVP)
- Source of truth: код в `src/main/java` и `src/test/java`
- Last updated: 2026-02-04

Этот каталог содержит рабочую документацию по проекту на этапе MVP.

## Реализовано сейчас
- Space:
  - процессы с версионированием
  - справочник типов процессов (`ProcessType`)
  - справочник типов операций (`OperationType`)
- Scheduler:
  - расчёт ожидаемого времени выполнения без постановки в реальный план (`POST /api/v1/scheduler/estimate`)

## MVP scope (частично реализовано)
- Space — модель предметных процессов (реализовано)
- Scheduler:
  - виртуальный расчёт (реализовано)
  - реальный план выполнения (в работе)
- Archive:
  - хранение/получение расчётов (в работе)
  - хранение/получение результатов выполнения (в работе)
  - контракт расчёта: self-contained документ (input + result + metadata)

## После MVP
- безопасность и авторизация API
- расширенный аудит изменений

## Содержание
- [API](api.md)
- [Архитектура](architecture.md)
- [Модели данных](data-models.md)
- [DTO (JSON схемы)](dto.md)
- [Логика расчёта estimate](scheduler-estimate.md)
- [Требования](requirements.md)
- [Решения (ADR)](decisions.md)
- [Разработка](development.md)
