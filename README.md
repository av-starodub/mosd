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

## 📚 Документация
- [Документация](docs/README.md)
- [API](docs/api.md)
- [Архитектура](docs/architecture.md)
- [Модели данных](docs/data-models.md)
- [DTO (JSON схемы)](docs/dto.md)
- [Требования](docs/requirements.md)
- [Решения (ADR)](docs/decisions.md)
- [Разработка](docs/development.md)
