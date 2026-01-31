[![Build](https://github.com/av-starodub/mosd/actions/workflows/build.yml/badge.svg)](https://github.com/av-starodub/mosd/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/av-starodub/mosd)](https://github.com/av-starodub/mosd/blob/master/LICENSE)
# MOSD — Manufacturing Order Scheduling & Dispatching

### Система планирования и диспетчеризации производственных заказов.

## Назначение
MOSD может использоваться автономно или в составе систем управления производством (MES) по стандартам ISA‑95/MESA.

## ✨ Основные возможности
- **Моделирование производства** — описание производства через процессы, операции и их временные характеристики
- **Расчёт ожидаемого времени выполнения** — оценка времени выполнения заказа без учёта текущей загрузки (lead time estimation)
- **Детальное планирование** — расчёт ожидаемого срока готовности заказа с учётом загрузки производства (finite capacity scheduling)
- **Управление очередями** — диспетчеризация очередей процессов в рамках выполнения заказов
- **Отслеживание выполнения** — мониторинг статуса заказов и их архивирование после завершения

## 🚀 Статус проекта
- **Текущая стадия**: Реализация MVP версии
- **Ветка разработки**: [mvp](https://github.com/av-starodub/mosd/tree/mvp)

## 🛠️ Технологии
- **Java 21**
- **Spring Boot 3**
- **MongoDB** (хранение данных и архива MVP)
- **PostgreSQL** (хранение архива после MVP)
- **Testcontainers**
- **Docker**
- **OpenAPI/Swagger**

## 🔒 Безопасность
- Проект использует Dependabot для автоматического мониторинга уязвимостей в зависимостях

## 📚 API (Space)
- `GET /api/v1/space/processes` — список актуальных (latest) процессов  
  Фильтры и параметры:
  - `type` — фильтр по типу процесса (process type key)
  - `page` — номер страницы (с 0, по умолчанию 0)
  - `size` — размер страницы (по умолчанию 50)
  - `sortBy` — поле сортировки: `processKey`, `type`, `version`, `createdAt`, `updatedAt`
  - `sortDir` — направление сортировки: `asc` или `desc`
