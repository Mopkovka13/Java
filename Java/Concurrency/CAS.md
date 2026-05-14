---
tags: [java, concurrency, cas, atomic, lock-free]
---

> [Index](../../Index.md) · рядом: [volatile](volatile.md) · [synchronized](synchronized.md) · [happens-before](happens-before.md) · аббревиатуры → [Concurrency glossary](Concurrency%20glossary.md)

# CAS (compare-and-swap)

Атомарная операция CPU: «если в адресе всё ещё `expected` — записать `new`, иначе ничего не делать; вернуть успех/неуспех».

## Реализация на железе

| Архитектура | Инструкция                                       |
| ----------- | ------------------------------------------------ |
| x86 / x64   | `LOCK CMPXCHG` — блокирует кеш-линию             |
| ARM / Power | пара **LL/SC** (Load-Linked / Store-Conditional) |
|             |                                                  |
|             |                                                  |

Атомарность гарантирует CPU, не библиотека.

## Retry loop

```java
int prev, next;
do {
    prev = value.get();
    next = prev + 1;
} while (!value.compareAndSet(prev, next));
```

Так устроен `AtomicInteger.incrementAndGet()`. Под высокой конкуренцией — **retry storm**, потоки крутятся вхолостую → `LongAdder` для счётчиков (per-thread слоты).

## ABA — главная ловушка

CAS проверяет **значение**, не историю. Если значение успело смениться `A → B → A`, CAS это не заметит.

Реальная боль — в lock-free структурах с переиспользованием узлов (стек, очередь):

```
T1: читает head = A, next = B; готовится pop'нуть
T2: pop A; pop B; push X; push A   ← тот же узел вернули
T1: CAS(head, A, B) ✅ успех — но stack = X, next у A больше не B → коррапт
```

## Решения ABA

| Подход | Идея |
|---|---|
| `AtomicStampedReference<T>` | пара (value, stamp); CAS сравнивает обе, stamp инкрементится при каждом swap |
| `AtomicMarkableReference<T>` | пара (value, boolean); для lazy-delete |
| Immutable nodes | каждый push — новый объект |
| Hazard pointers | вне Java; поток «резервирует» указатель |
| Опора на GC | пока ссылка жива, узел не переиспользуется (часто спасает в Java) |

## Где CAS в Java

- `AtomicInteger`/`Long`/`Reference`/`LongArray`/...
- `LongAdder`, `DoubleAdder` — CAS на per-thread слотах.
- **AQS** (`AbstractQueuedSynchronizer`) — фундамент `ReentrantLock`, `Semaphore`, `CountDownLatch`.
- `ConcurrentHashMap` — CAS при вставке в пустой бакет.
- `j.u.c.`-очереди (`ConcurrentLinkedQueue`, ...).
- `VarHandle` (Java 9+) — CAS на произвольных полях.

## Lock-free vs wait-free

| Свойство | Гарантия |
|---|---|
| Lock-free | хоть один поток прогрессирует; конкретный может ретраить долго |
| Wait-free | **каждый** поток за конечное число шагов |

`j.u.c.` — обычно lock-free, не wait-free.

## Гочи

- CAS — **видимость** даёт автоматически (как [volatile](volatile.md)).
- Под низкой конкуренцией CAS быстрее [synchronized](synchronized.md). Под высокой — может проиграть из-за retry storm.
- `compareAndSet` возвращает `boolean` — забыл проверить = молча потерял запись.
