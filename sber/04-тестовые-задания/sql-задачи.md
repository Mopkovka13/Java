# SQL-задачи: условие → решение

SQL-задачу дают почти на каждой технической секции (и разработчикам, и аналитикам). Здесь — самые частые с реальных собесов, в формате **Задача → Решение → Разбор → Подвохи**.

> Схема для большинства задач:
> ```
> employees(id, name, dept_id, salary, manager_id)
> departments(id, name)
> orders(id, client_id, amount, status, created_at)
> clients(id, name, city, registered_at)
> ```

---

## A. Базовые (JOIN, агрегаты)

### 1. Сотрудники и их отделы (INNER vs LEFT)

**Задача.** Вывести имя сотрудника и название его отдела. Затем — включить сотрудников **без** отдела.

**Решение.**
```sql
-- только с отделом:
SELECT e.name, d.name AS dept
FROM employees e
JOIN departments d ON d.id = e.dept_id;

-- включая тех, у кого dept_id = NULL:
SELECT e.name, d.name AS dept
FROM employees e
LEFT JOIN departments d ON d.id = e.dept_id;
```
**Разбор.** `JOIN` (INNER) отбросит сотрудников без отдела; `LEFT JOIN` сохранит их (отдел будет NULL).
**Подвохи.** Если потом добавить `WHERE d.name = 'IT'` — LEFT снова превратится в INNER (NULL не пройдёт фильтр). Условие по правой таблице клади в `ON`.

---

### 2. Количество сотрудников по отделам

**Задача.** Сколько сотрудников в каждом отделе, по убыванию.

**Решение.**
```sql
SELECT d.name, COUNT(e.id) AS cnt
FROM departments d
LEFT JOIN employees e ON e.dept_id = d.id
GROUP BY d.id, d.name
ORDER BY cnt DESC;
```
**Разбор.** `LEFT JOIN` + `COUNT(e.id)` (а не `COUNT(*)`!) даёт 0 для пустых отделов: `COUNT` по конкретной колонке не считает NULL.
**Подвохи.** `COUNT(*)` посчитал бы пустому отделу 1 (из-за строки самого отдела). Группируй и по `id`, и по `name`.

---

### 3. Отделы со средней зарплатой выше 100к (HAVING)

**Задача.** Найти отделы, где средняя зарплата > 100000.

**Решение.**
```sql
SELECT dept_id, AVG(salary) AS avg_salary
FROM employees
GROUP BY dept_id
HAVING AVG(salary) > 100000;
```
**Разбор.** Фильтр по агрегату → `HAVING` (после группировки), не `WHERE`.
**Подвохи.** В `WHERE` агрегат недоступен. Если нужно ещё и предварительно отсечь, например, уволенных — это `WHERE status='active'` ДО группировки.

---

## B. Хиты собеседований

### 4. Вторая по величине зарплата

**Задача.** Найти вторую по величине зарплату (не второго сотрудника, а именно второе **значение**).

**Решение (надёжное, через DISTINCT + оконку).**
```sql
SELECT salary FROM (
  SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk
  FROM employees
) t
WHERE rnk = 2;
```
**Альтернатива (без оконок):**
```sql
SELECT MAX(salary) FROM employees
WHERE salary < (SELECT MAX(salary) FROM employees);
```
**Разбор.** `DENSE_RANK` не пропускает номера при одинаковых зарплатах — поэтому «вторая по величине» корректна даже при дубликатах максимума.
**Подвохи.** Через `RANK` при двух одинаковых максимумах второе значение «съедется». Через `LIMIT 1 OFFSET 1` сломается на дубликатах.

---

### 5. Топ-N по группе (вторая зарплата в каждом отделе)

**Задача.** Для каждого отдела вывести 2 самые высокие зарплаты.

**Решение.**
```sql
SELECT * FROM (
  SELECT e.*, DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rnk
  FROM employees e
) t
WHERE rnk <= 2;
```
**Разбор.** `PARTITION BY dept_id` = «считать ранг отдельно в каждом отделе». Это шаблон **top-N по группе** — запомни его как «золотой».
**Подвохи.** Выбор между `RANK`/`DENSE_RANK`/`ROW_NUMBER` зависит от того, как обрабатывать одинаковые значения (нужны ли дубликаты в топе).

---

### 6. Последняя транзакция каждого клиента

**Задача.** Для каждого клиента — дата его последнего заказа и сам заказ.

**Решение (оконка, предпочтительно).**
```sql
SELECT * FROM (
  SELECT o.*, ROW_NUMBER() OVER (PARTITION BY client_id ORDER BY created_at DESC) AS rn
  FROM orders o
) t
WHERE rn = 1;
```
**Разбор.** `ROW_NUMBER` нумерует заказы клиента от свежего к старому; берём первый.
**Подвохи.** При одинаковом `created_at` добавь tie-breaker в `ORDER BY` (например, `, id DESC`), иначе результат недетерминирован.

---

### 7. Сумма трёх последних транзакций клиента

**Задача.** Для каждого клиента — сумма его 3 последних по времени заказов.

**Решение.**
```sql
SELECT client_id, SUM(amount) AS last3_sum
FROM (
  SELECT o.*, ROW_NUMBER() OVER (PARTITION BY client_id ORDER BY created_at DESC) AS rn
  FROM orders o
) t
WHERE rn <= 3
GROUP BY client_id;
```
**Разбор.** Сначала пронумеровали по свежести, оставили топ-3, потом сгруппировали и сложили.
**Подвохи.** Любимая формулировка Сбера/банков (последние операции по счёту). Не путай с оконным `SUM` — тут нужна именно фильтрация топ-3.

---

### 8. Клиенты без заказов

**Задача.** Найти клиентов, у которых нет ни одного заказа.

**Решение (два способа).**
```sql
-- LEFT JOIN + IS NULL:
SELECT c.* FROM clients c
LEFT JOIN orders o ON o.client_id = c.id
WHERE o.id IS NULL;

-- NOT EXISTS (предпочтительно):
SELECT c.* FROM clients c
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.client_id = c.id);
```
**Разбор.** «Найти тех, у кого нет связанных строк» — классика. `NOT EXISTS` читается яснее и не страдает от NULL.
**Подвохи.** `NOT IN (SELECT client_id ...)` вернёт **пусто**, если в подзапросе есть хоть один NULL — частая ошибка. Используй `NOT EXISTS`.

---

### 9. Найти и удалить дубликаты

**Задача.** Найти клиентов с одинаковым email; затем оставить по одному.

**Решение.**
```sql
-- найти дубликаты:
SELECT email, COUNT(*) FROM clients
GROUP BY email
HAVING COUNT(*) > 1;

-- удалить, оставив запись с минимальным id:
DELETE FROM clients a
USING clients b
WHERE a.id > b.id AND a.email = b.email;
```
**Разбор.** Группировка + `HAVING COUNT(*) > 1` находит дубли; self-join по равенству email и `a.id > b.id` удаляет все, кроме «первого».
**Подвохи.** Перед `DELETE` всегда сначала сделай `SELECT` тех же строк — убедиться, что удаляешь именно то.

---

### 10. Нарастающий итог и сравнение с предыдущим (оконки)

**Задача.** По заказам клиента вывести нарастающую сумму и разницу с предыдущим заказом.

**Решение.**
```sql
SELECT client_id, created_at, amount,
       SUM(amount) OVER (PARTITION BY client_id ORDER BY created_at) AS running_total,
       amount - LAG(amount) OVER (PARTITION BY client_id ORDER BY created_at) AS diff_prev
FROM orders;
```
**Разбор.** `SUM() OVER (ORDER BY ...)` — нарастающий итог (не схлопывает строки). `LAG` берёт значение предыдущей строки.
**Подвохи.** У самого первого заказа `LAG` вернёт NULL — это нормально, при необходимости заверни в `COALESCE`.

---

## C. Каверзные (на понимание)

### 11. Доля выполненных заказов

**Задача.** Посчитать процент заказов со статусом `completed` от всех.

**Решение.**
```sql
SELECT
  COUNT(*) FILTER (WHERE status = 'completed') * 100.0 / COUNT(*) AS completed_pct
FROM orders;
```
**Разбор.** `FILTER (WHERE ...)` (PostgreSQL) — условный подсчёт. Универсальный аналог: `SUM(CASE WHEN status='completed' THEN 1 ELSE 0 END)`.
**Подвохи.** `* 100.0`, а не `* 100` — иначе целочисленное деление даст 0.

---

### 12. Почему `status != 'X'` теряет строки со status IS NULL?

**Задача (теория с примером).** Объяснить, почему `WHERE status != 'completed'` не вернёт строки, где `status IS NULL`.

**Ответ.** Любое сравнение с `NULL` даёт не `true/false`, а `UNKNOWN`, и такие строки не проходят `WHERE`. Чтобы включить NULL:
```sql
WHERE status IS DISTINCT FROM 'completed'   -- считает NULL отличным от 'completed'
-- или: WHERE status != 'completed' OR status IS NULL
```
**Подвохи.** Это любимый «теоретический» вопрос про трёхзначную логику SQL (true/false/unknown).

---

## Как решать SQL-задачу на собесе (алгоритм)

1. **Уточни схему:** какие таблицы, ключи, что значит каждое поле, какие типы, бывают ли NULL.
2. **Уточни задачу:** нужны ли дубликаты, как обрабатывать NULL, какой период.
3. **Пиши по порядку выполнения:** `FROM/JOIN → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT`.
4. **Проверь на краях:** NULL, пустые группы, дубликаты ключей, деление на ноль, tie-breaker в `ORDER BY`.
5. **Знай три «золотых шаблона»:** top-N по группе (`DENSE_RANK + PARTITION BY`), «нет связанных строк» (`NOT EXISTS`), последние записи (`ROW_NUMBER`).

---

## Источники

- [Вопросы по SQL на собеседовании: 27 задач с ответами — tproger](https://tproger.ru/articles/sql-interview-questions)
- [5 заданий по SQL с реальных собеседований — tproger](https://tproger.ru/articles/5-zadanij-po-sql-s-realnyh-sobesedovanij)
- [SQL-собеседование: вопросы, кейсы и типичные ошибки — Stepik](https://welcome.stepik.org/blog/sql-sobesedovanie-voprosy-i-podgotovka)
