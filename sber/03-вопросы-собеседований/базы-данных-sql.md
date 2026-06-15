# Базы данных и SQL — вопросы собеседования (Сбер)

> 📝 Самопроверка: краткие ответы на 20 ключевых вопросов — в [ответы-базы-данных-sql.md](ответы/ответы-базы-данных-sql.md).

«Первый вопрос — почти всегда SQL и типы JOIN» (наблюдение с банковских собесов). Целевая СУБД Сбера — **PostgreSQL** (своя сборка Platform V Pangolin; банк мигрировал с Oracle).

## SQL

- **JOIN'ы**: INNER, LEFT/RIGHT, FULL OUTER, CROSS — нарисуй диаграммы Венна в голове; LEFT JOIN + WHERE по правой таблице = скрытый INNER (ловушка).
- GROUP BY, HAVING vs WHERE, агрегаты с NULL.
- Подзапросы vs JOIN vs CTE (WITH); рекурсивные CTE.
- **Оконные функции**: ROW_NUMBER, RANK, DENSE_RANK, LAG/LEAD, агрегаты OVER (PARTITION BY) — классика лайвкодинга: «вторая по величине зарплата в каждом отделе».
- UNION vs UNION ALL, EXISTS vs IN (и NULL-ловушка NOT IN).
- Удаление дубликатов, top-N по группе — типовые SQL-задачи.

## Индексы и производительность

- B-tree: как устроен, почему ускоряет; когда индекс НЕ работает (функция над колонкой, LIKE '%...', низкая селективность, неявное приведение типов).
- Составные индексы: порядок колонок, покрывающие индексы (INDEX ONLY SCAN).
- PostgreSQL-специфика: GIN (jsonb, полнотекст), GiST, BRIN, partial/functional indexes.
- **EXPLAIN (ANALYZE)**: seq scan vs index scan vs bitmap scan, nested loop vs hash join vs merge join — уметь читать план.
- Цена индексов: замедление записи, bloat.

## Транзакции (любимая тема, см. также топ-12)

- ACID — расшифровать с примерами.
- Уровни изоляции + аномалии (dirty/non-repeatable/phantom read, serialization anomaly).
- **MVCC в PostgreSQL**: версии строк, xmin/xmax, READ UNCOMMITTED = READ COMMITTED, vacuum и зачем он нужен (мёртвые версии, transaction id wraparound).
- Lost update и решения: SELECT FOR UPDATE (пессимистично), @Version/optimistic locking, advisory locks.
- Deadlock в БД: как возникает, как PostgreSQL его разрешает, как избегать.

## Проектирование

- Нормальные формы (1НФ–3НФ, BCNF) и когда денормализация оправдана.
- Первичные/внешние ключи, surrogate vs natural keys, UUID vs sequence (плюсы/минусы для индексов).
- Партиционирование таблиц (range/list/hash), шардирование — отличие.
- Миграции: Liquibase/Flyway, обратная совместимость схемы при rolling-деплое (expand-contract).

## NoSQL (на уровне концепций)

- Redis: структуры данных, кеширование (cache-aside, TTL, проблемы инвалидации, cache stampede).
- Когда NoSQL vs реляционка; CAP-теорема.

## Типовые SQL-задачи лайвкодинга

```sql
-- 1. Сотрудники с зарплатой выше средней по своему отделу
SELECT e.* FROM employee e
JOIN (SELECT dept_id, AVG(salary) avg_s FROM employee GROUP BY dept_id) d
  ON e.dept_id = d.dept_id
WHERE e.salary > d.avg_s;

-- 2. Вторая по величине зарплата
SELECT MAX(salary) FROM employee
WHERE salary < (SELECT MAX(salary) FROM employee);

-- 3. Дубликаты email
SELECT email, COUNT(*) FROM users GROUP BY email HAVING COUNT(*) > 1;

-- 4. Top-3 зарплаты в каждом отделе (оконные функции)
SELECT * FROM (
  SELECT e.*, DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) rnk
  FROM employee e
) t WHERE rnk <= 3;
```
