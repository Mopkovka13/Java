# Spring — вопросы собеседования (Сбер)

> 📝 Самопроверка: краткие ответы на 25 ключевых вопросов — в [ответы-spring.md](ответы/ответы-spring.md).

Spring Boot — ядро стека Сбера. Самые «валящие» вопросы — про прокси и транзакции.

## ⚠️ Вопрос-убийца: self-invocation в @Transactional

**Вопрос:** «Метод A сервиса вызывает this.methodB(), на B стоит @Transactional REQUIRES_NEW. Что произойдёт?»
**Ответ:** Новая транзакция НЕ откроется. Spring оборачивает бин в прокси, а прокси перехватывает только внешние вызовы. `this.methodB()` — прямой вызов мимо прокси, аннотация игнорируется. То же касается @Cacheable, @Async и т.д.

**Решения (назови минимум два):**
1. Вынести methodB в отдельный сервис (предпочтительно — чище по дизайну).
2. Self-injection: заинжектить себя через `@Lazy` и вызывать `self.methodB()`.
3. `TransactionTemplate` — программное управление транзакцией.
4. AspectJ weaving вместо прокси (тяжёлое решение, редко).

## IoC и бины

- IoC/DI: что это, зачем; типы инжекции (конструктор — рекомендуемый, почему: иммутабельность, обязательность зависимостей, тестируемость).
- Жизненный цикл бина: инстанцирование → инжекция → BeanPostProcessor (before) → @PostConstruct/afterPropertiesSet → BeanPostProcessor (after) → использование → @PreDestroy.
- Scope'ы: singleton, prototype, request, session; ловушка prototype внутри singleton (ObjectProvider, @Lookup).
- Circular dependency: почему конструкторная инжекция падает, как Spring решает для сеттеров/полей (3 кеша), @Lazy.
- @Component vs @Bean vs @Configuration (proxyBeanMethods); @Qualifier, @Primary, @Conditional.
- BeanFactory vs ApplicationContext.

## Прокси и AOP

- JDK dynamic proxy (по интерфейсам) vs CGLIB (наследование) — что Spring Boot использует по умолчанию (CGLIB).
- Ограничения: final-методы/классы не проксируются CGLIB, private-методы не перехватываются.
- AOP: aspect, advice, pointcut, join point; где применяется (транзакции, кеш, security, логирование).

## Транзакции

- @Transactional: все 7 propagation (особенно REQUIRED vs REQUIRES_NEW vs NESTED — savepoint).
- isolation, readOnly (что реально делает), timeout.
- rollbackFor: по умолчанию откат только на unchecked! Checked-исключение НЕ откатывает транзакцию — классическая ловушка.
- На каких уровнях работает (public-методы; protected/private — нет, для прокси).
- TransactionSynchronization, @TransactionalEventListener (фазы AFTER_COMMIT).

## Spring Boot

- Автоконфигурация: @EnableAutoConfiguration, spring.factories / AutoConfiguration.imports, @ConditionalOnClass/OnMissingBean.
- Стартеры: что внутри, как написать свой.
- application.yml, профили (@Profile, spring.profiles.active), @ConfigurationProperties vs @Value.
- Actuator: health, metrics, эндпоинты для Kubernetes (liveness/readiness probes).
- Embedded Tomcat, настройка пула потоков.

## Spring MVC / REST

- DispatcherServlet: путь запроса от фильтра до контроллера.
- @RestController vs @Controller; @RequestBody/@ResponseBody, HttpMessageConverter.
- Обработка ошибок: @ControllerAdvice + @ExceptionHandler, ProblemDetail (RFC 7807).
- Валидация: @Valid, Bean Validation, кастомные валидаторы.
- Фильтры vs интерцепторы vs AOP.

## Spring Data JPA

- Репозитории: производные методы, @Query, проекции, Specification.
- Жизненный цикл entity: transient → managed → detached → removed; flush vs commit.
- LazyInitializationException: почему, Open Session in View (и почему его выключают).
- N+1 и решения (см. топ-12), кеш 1-го/2-го уровня Hibernate.
- @Version — оптимистичная блокировка; @Lock(PESSIMISTIC_WRITE).
- Отличие save/saveAndFlush, getReferenceById vs findById, dirty checking.

## Spring Security (базово)

- Цепочка фильтров, SecurityFilterChain.
- Аутентификация vs авторизация; JWT vs sessions; OAuth2/OIDC на словах.
- @PreAuthorize, method security.

## Где брать ответы

- «Spring в действии» (Крейг Уоллс) — рекомендован самим Сбером.
- [Документация Spring](https://docs.spring.io/spring-framework/reference/)
- [GitHub enhorse/java-interview, раздел Spring](https://github.com/enhorse/java-interview)
