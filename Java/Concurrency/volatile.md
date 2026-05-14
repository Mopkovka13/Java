---
tags: [java, concurrency, volatile, jmm]
---

> [Index](../../Index.md) · рядом: [happens-before](happens-before.md) · [Memory barriers](Memory%20barriers.md) · [Double-checked locking](Double-checked%20locking.md) · [wait-notify](wait-notify.md) · аббревиатуры → [Concurrency glossary](Concurrency%20glossary.md)

# `volatile`

## Гарантирует

| Гарантия | Что значит |
|---|---|
| Видимость | Запись из одного потока сразу видна другим |
| [happens-before](happens-before.md) | volatile write hb последующего volatile read той же переменной |
| Запрет реордеринга | JIT/CPU не переставят инструкции через volatile-доступ ([Memory barriers](Memory%20barriers.md)) |
| Атомарность одного R/W | В т.ч. для `long`/`double` (без volatile они могут «порваться») |

## НЕ гарантирует

- **Атомарность read-modify-write.** `count++` = read → +1 → write. Между read и write влезет другой поток → lost update. Чинить: `AtomicInteger` (CAS), `synchronized`, `LongAdder`.
- Не заменяет `synchronized` для критических секций.

## Piggy-backing

```java
Object data;
volatile boolean ready;

// writer
data = build();      // (1) обычная запись
ready = true;        // (2) volatile write

// reader
if (ready)           // (3) volatile read
    use(data);       // (4) видит свежий data
```

Запись в volatile тащит за собой все предшествующие обычные записи (через `StoreStore` барьер). Чтение видит их все. → можно публиковать сложное состояние через один volatile-флаг.

## Когда применять

- ✅ Флаги (`shutdown`, `ready`, `initialized`).
- ✅ Публикация неизменяемого объекта (`volatile Config config`).
- ✅ [Double-checked locking](Double-checked%20locking.md) — обязателен.
- ❌ Счётчики, аккумуляторы → `AtomicInteger` / `LongAdder`.
- ❌ Группа полей, меняемая атомарно → `synchronized` / `Lock`.

## Гочи

- `volatile` массив — volatile только **ссылка**, не элементы. Нужны volatile элементы → `AtomicReferenceArray`.
- `volatile` поле в объекте не защищает другие поля этого же объекта.
- `count++` на `volatile int` всё равно небезопасен.
- Не блокирует — потоки не ждут друг друга, просто видят правду.
