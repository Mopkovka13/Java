# Kafka, микросервисы и инфраструктура — вопросы собеседования (Сбер)

> 📝 Самопроверка: краткие ответы на 20 ключевых вопросов — в [ответы-kafka-микросервисы.md](ответы/ответы-kafka-микросервисы.md).

Микросервисы упоминаются в каждой Java-вакансии Сбера; Kafka — стандарт обмена сообщениями (входит в стек Platform V).

## Apache Kafka

- Архитектура: брокеры, топики, **партиции**, реплики, лидер/фолловеры, ISR.
- **Порядок сообщений — только внутри партиции**; partition key → hash → партиция. Сообщения одного счёта/клиента — с одним ключом (вопрос из топ-12).
- Consumer groups: ребалансировка, partition assignment; больше консьюмеров, чем партиций → простой.
- Гарантии доставки: at-most-once / at-least-once / exactly-once; acks (0/1/all), идемпотентный продюсер, транзакции Kafka.
- Offset management: автокоммит vs ручной, at-least-once → дубликаты → **идемпотентность консьюмера**.
- Retention, compacted topics, DLQ (dead letter queue) для «ядовитых» сообщений.
- Kafka vs RabbitMQ: pull vs push, перечитывание истории, масштабирование.
- Spring Kafka: @KafkaListener, error handling, retry topics.

## Паттерны микросервисов (готовь с примерами из опыта)

- Декомпозиция: по бизнес-доменам (DDD, bounded context); чем плох «распределённый монолит».
- Коммуникация: sync (REST, gRPC) vs async (Kafka) — когда что.
- **Saga** (оркестрация vs хореография) — распределённые транзакции без 2PC; компенсирующие действия.
- **Transactional Outbox** — как атомарно записать в БД и отправить в Kafka (вопрос на понимание dual-write problem).
- **Circuit Breaker** (Resilience4j): состояния closed/open/half-open; retry + backoff + jitter; bulkhead, rate limiter, timeout.
- Идемпотентность API: idempotency key, безопасные ретраи — критично в банке.
- API Gateway, service discovery, конфигурация (Spring Cloud Config / K8s ConfigMaps).
- Версионирование API, обратная совместимость контрактов.
- Консистентность: eventual consistency, CQRS, event sourcing (на словах).
- Distributed tracing (OpenTelemetry, trace id через сервисы), централизованные логи, метрики (Prometheus + Grafana), health checks.

## REST

- Идемпотентность методов (GET/PUT/DELETE — да, POST — нет), коды ответов (когда 400 vs 422, 401 vs 403, 409).
- Richardson maturity model, HATEOAS (на словах).
- Пагинация (offset vs cursor), фильтрация, ETag/кеширование.

## Docker / Kubernetes / OpenShift

OpenShift — корпоративный стандарт Сбера (дистрибутив Kubernetes от Red Hat).

- Docker: image vs container, слои, multi-stage build, dockerfile для Java-приложения (JRE-базовый образ, layered jars).
- Kubernetes: pod, deployment, service, ingress, configmap/secret, requests/limits.
- Liveness vs readiness vs startup probes (+ Spring Actuator).
- **CPU throttling** (вопрос из топ-12): CPU limit → CFS-квота → троттлинг; JVM в контейнере: UseContainerSupport, ActiveProcessorCount, Xmx vs memory limit (OOMKilled).
- Rolling update, HPA (автомасштабирование), graceful shutdown Spring-приложения (SIGTERM, preStop hook).
- Service mesh (Istio/Envoy) — основа Platform V Synapse: mTLS, traffic management, retries на уровне mesh.

## CI/CD

- Jenkins / GitLab CI: pipeline, стадии (build → test → quality gate → image → deploy).
- SonarQube, статический анализ; стратегии деплоя: rolling, blue-green, canary.
- Git flow vs trunk-based, code review процесс.
