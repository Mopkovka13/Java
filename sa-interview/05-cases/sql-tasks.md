# SQL-задачи с разбором

Тренируйся писать руками. На собесе дают схему и просят запрос. Проговаривай ход мыслей.

> Схема для большинства задач:
> ```
> clients(id, name, city, registered_at)
> orders(id, client_id, amount, status, created_at)
> products(id, title, price, category)
> order_items(order_id, product_id, qty)
> employees(id, name, manager_id, department, salary)
> ```

---

## Базовые (🟢 Junior)

### Задача 1. Все заказы клиента «Иван» дороже 1000.
```sql
SELECT o.*
FROM orders o
JOIN clients c ON c.id = o.client_id
WHERE c.name = 'Иван' AND o.amount > 1000;
```

### Задача 2. Количество клиентов по городам.
```sql
SELECT city, COUNT(*) AS cnt
FROM clients
GROUP BY city
ORDER BY cnt DESC;
```

### Задача 3. Клиенты, у которых НЕТ заказов.
```sql
SELECT c.*
FROM clients c
LEFT JOIN orders o ON o.client_id = c.id
WHERE o.id IS NULL;
```
**Разбор:** LEFT JOIN + `IS NULL` — классический приём «найти тех, у кого нет связанных записей». Альтернатива через `NOT EXISTS`.

---

## Группировка и агрегаты (🟡 Middle)

### Задача 4. Сумма заказов по каждому клиенту, только где сумма > 10000.
```sql
SELECT c.name, SUM(o.amount) AS total
FROM clients c
JOIN orders o ON o.client_id = c.id
GROUP BY c.id, c.name
HAVING SUM(o.amount) > 10000
ORDER BY total DESC;
```
**Разбор:** фильтр по агрегату → `HAVING`, не `WHERE`.

### Задача 5. Средний чек по месяцам за текущий год.
```sql
SELECT DATE_TRUNC('month', created_at) AS month,
       AVG(amount) AS avg_check,
       COUNT(*) AS orders_cnt
FROM orders
WHERE created_at >= DATE_TRUNC('year', CURRENT_DATE)
GROUP BY 1
ORDER BY 1;
```

### Задача 6. Топ-5 клиентов по сумме заказов.
```sql
SELECT c.name, SUM(o.amount) AS total
FROM clients c
JOIN orders o ON o.client_id = c.id
WHERE o.status = 'completed'
GROUP BY c.id, c.name
ORDER BY total DESC
LIMIT 5;
```

### Задача 7. Доля выполненных заказов (status='completed') от всех.
```sql
SELECT
  COUNT(*) FILTER (WHERE status = 'completed') * 100.0 / COUNT(*) AS completed_pct
FROM orders;
```
**Разбор:** `FILTER` (PostgreSQL) или `SUM(CASE WHEN status='completed' THEN 1 ELSE 0 END)` — условный подсчёт.

---

## Подзапросы (🟡 Middle)

### Задача 8. Клиенты, чья сумма заказов выше среднего по всем клиентам.
```sql
SELECT client_id, SUM(amount) AS total
FROM orders
GROUP BY client_id
HAVING SUM(amount) > (
  SELECT AVG(client_total)
  FROM (SELECT SUM(amount) AS client_total FROM orders GROUP BY client_id) t
);
```

### Задача 9. Последний заказ каждого клиента.
```sql
SELECT o.*
FROM orders o
WHERE o.created_at = (
  SELECT MAX(o2.created_at) FROM orders o2 WHERE o2.client_id = o.client_id
);
```
Или через оконную функцию (см. задачу 11).

---

## Оконные функции (🔴 Middle/Senior)

### Задача 10. Вторая по величине зарплата в каждом отделе.
```sql
SELECT department, name, salary
FROM (
  SELECT department, name, salary,
         DENSE_RANK() OVER (PARTITION BY department ORDER BY salary DESC) AS rnk
  FROM employees
) t
WHERE rnk = 2;
```
**Разбор:** `DENSE_RANK` не пропускает номера при равных значениях, в отличие от `RANK`.

### Задача 11. Последний заказ каждого клиента через окно.
```sql
SELECT *
FROM (
  SELECT o.*,
         ROW_NUMBER() OVER (PARTITION BY client_id ORDER BY created_at DESC) AS rn
  FROM orders o
) t
WHERE rn = 1;
```

### Задача 12. Нарастающий итог суммы заказов клиента по датам.
```sql
SELECT client_id, created_at, amount,
       SUM(amount) OVER (PARTITION BY client_id ORDER BY created_at) AS running_total
FROM orders;
```

### Задача 13. Разница суммы заказа с предыдущим (LAG).
```sql
SELECT client_id, created_at, amount,
       amount - LAG(amount) OVER (PARTITION BY client_id ORDER BY created_at) AS diff_prev
FROM orders;
```

---

## Иерархии и self-join (🔴 Senior)

### Задача 14. Сотрудники и имена их руководителей.
```sql
SELECT e.name AS employee, m.name AS manager
FROM employees e
LEFT JOIN employees m ON m.id = e.manager_id;
```

### Задача 15. Рекурсивно — вся цепочка подчинённых руководителя с id=1.
```sql
WITH RECURSIVE subordinates AS (
  SELECT id, name, manager_id FROM employees WHERE id = 1
  UNION ALL
  SELECT e.id, e.name, e.manager_id
  FROM employees e
  JOIN subordinates s ON e.manager_id = s.id
)
SELECT * FROM subordinates;
```

---

## Частые «подвохи» на собесе

### Задача 16. Найти дубликаты по email.
```sql
SELECT email, COUNT(*)
FROM clients
GROUP BY email
HAVING COUNT(*) > 1;
```

### Задача 17. Удалить дубликаты, оставив одну запись.
```sql
DELETE FROM clients a
USING clients b
WHERE a.id > b.id AND a.email = b.email;
```

### Задача 18. Почему `WHERE status != 'completed'` может не вернуть строки со status IS NULL?
**Ответ:** сравнение с NULL даёт `UNKNOWN`, такие строки не попадают в результат. Нужно `WHERE status IS DISTINCT FROM 'completed'` или добавить `OR status IS NULL`. Любимый каверзный вопрос про работу с NULL.

### Задача 19. Чем `COUNT(*)` отличается от `COUNT(column)`?
`COUNT(*)` — все строки; `COUNT(column)` — только не-NULL значения столбца.

### Задача 20. Заказы за последние 30 дней по дням, включая дни без заказов.
```sql
SELECT d::date AS day, COUNT(o.id) AS cnt
FROM generate_series(CURRENT_DATE - 29, CURRENT_DATE, '1 day') d
LEFT JOIN orders o ON o.created_at::date = d::date
GROUP BY d
ORDER BY d;
```
**Разбор:** генерация календаря + LEFT JOIN, чтобы не «проваливались» дни без данных.

---

## Как решать SQL на собесе (алгоритм)

1. **Уточни схему** — какие таблицы, ключи, что значит каждое поле, типы.
2. **Уточни задачу** — нужны ли NULL, дубликаты, какой период.
3. **Проговори план** — какие JOIN, нужна ли группировка/окно.
4. **Пиши по порядку выполнения:** FROM/JOIN → WHERE → GROUP BY → HAVING → SELECT → ORDER BY.
5. **Проверь на краях:** NULL, пустые группы, дубликаты, деление на ноль.
