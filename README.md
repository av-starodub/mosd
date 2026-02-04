[![Build](https://github.com/av-starodub/mosd/actions/workflows/build.yml/badge.svg)](https://github.com/av-starodub/mosd/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/av-starodub/mosd)](https://github.com/av-starodub/mosd/blob/master/LICENSE)
# MOSD — Process Scheduling & Dispatching

### Система моделирования процессов, расчёта и планирования выполнения.

## Назначение
MOSD может использоваться как автономный сервис или как компонент в более крупной системе.

## ✨ Основные возможности
- **Space** — каталог процессов, типов процессов и типов операций
- **Estimate** — виртуальный расчёт ожидаемого времени для входного набора данных
- **Scheduler plan** — построение и управление реальным планом выполнения (MVP: в работе)
- **Archive** — хранение и получение расчётов и результатов выполнения (MVP: в работе)
- **Queue view** — детализированное представление очередей по процессам

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
- [Логика расчёта estimate](docs/scheduler-estimate.md)
- [Архитектура](docs/architecture.md)
- [Модели данных](docs/data-models.md)
- [DTO (JSON схемы)](docs/dto.md)
- [Требования](docs/requirements.md)
- [Решения (ADR)](docs/decisions.md)
- [Разработка](docs/development.md)
