---
tags: [java, concurrency, cheatsheet, moc]
---

> [Index](../../Index.md) · аббревиатуры — [Concurrency glossary](Concurrency%20glossary.md) · обзор всех concurrent-сущностей, детали в отдельных заметках.

# Concurrency — сводная таблица

## 1. JMM — фундамент

| Сущность | Даёт | Не даёт |
|---|---|---|
| [happens-before](happens-before.md) | формальное правило видимости JMM | — |
| [volatile](volatile.md) | видимость, hb, запрет реордера, атомарность одного R/W | атомарность RMW (`++`) |
| `final` | hb на завершение конструктора (если `this` не утёк) | — |
| [Memory barriers](Memory%20barriers.md) | LoadLoad/StoreStore/StoreLoad/LoadStore | — |
| [CAS](CAS.md) | атомарный compare-and-set + видимость | защиту от ABA |

## 2. Atomics (lock-free, на CAS)

Все: видимость + атомарность одного R-M-W. Не блокируют. Под высокой конкуренцией — retry storm.

| Класс | Назначение |
|---|---|
| `AtomicInteger` / `Long` / `Boolean` | RMW на примитивах |
| `AtomicReference<T>` | RMW на ссылках |
| `AtomicStampedReference<T>` | (value, stamp) — защита от ABA |
| `AtomicMarkableReference<T>` | (value, mark) — для lazy-delete |
| `AtomicIntegerArray` / `LongArray` / `ReferenceArray` | per-element CAS |
| `Atomic*FieldUpdater` | CAS на чужом `volatile`-поле без отдельного объекта |
| `LongAdder` / `DoubleAdder` | счётчик per-thread слоты — быстрее `AtomicLong` при contention |
| `LongAccumulator` / `DoubleAccumulator` | произвольная ассоциативная функция (`max`, `sum`, ...) |

## 3. Локи

| Класс | Гарантии | Особенности |
|---|---|---|
| [synchronized](synchronized.md) | mutex + reentrancy + visibility | biased→thin→fat, **не** interruptible, нет `tryLock` |
| `ReentrantLock` | то же | `lockInterruptibly`, `tryLock(timeout)`, fair, несколько `Condition` |
| `ReentrantReadWriteLock` | разделение R/W | много readers ИЛИ один writer |
| `StampedLock` (Java 8+) | + **optimistic read** | **не reentrant**, нет `Condition` |
| `Lock` + `Condition` | замена `wait`/`notify` | несколько Condition на один Lock |

## 4. Synchronizers (на AQS поверх CAS)

| Класс | Назначение | Заметка |
|---|---|---|
| `CountDownLatch` | дождаться N событий, один раз | нельзя сбросить |
| `CyclicBarrier` | свести N потоков многократно | + `barrierAction` |
| `Phaser` | гибкий barrier с фазами | participants на лету |
| `Semaphore` | пропустить N одновременно | fair/non-fair |
| `Exchanger<T>` | hand-off между двумя потоками | оба ждут |

## 5. Thread-safe коллекции

| Класс | Стратегия | Iterator |
|---|---|---|
| `ConcurrentHashMap` | CAS на пустой бакет, `synchronized` на голову бакета | weakly consistent |
| `ConcurrentSkipListMap` / `Set` | lock-free skip list | weakly consistent, sorted |
| `CopyOnWriteArrayList` / `Set` | полная копия на каждую запись | snapshot |
| `ConcurrentLinkedQueue` / `Deque` | lock-free Michael-Scott | weakly consistent, unbounded |

> Weakly consistent — не падает с `CME`, но и не гарантирует увидеть свежие изменения.

## 6. Blocking queues

| Класс | Bounded? | Особенности |
|---|---|---|
| `ArrayBlockingQueue` | да | один лок, fair-режим |
| `LinkedBlockingQueue` | опц., default `MAX_VALUE` | два лока (head/tail), выше throughput |
| `LinkedBlockingDeque` | опц. | двусторонняя |
| `PriorityBlockingQueue` | unbounded | heap по `Comparable` |
| `DelayQueue` | unbounded | элемент виден после `getDelay()` |
| `SynchronousQueue` | **capacity = 0** | rendezvous, прямая передача |
| `LinkedTransferQueue` | unbounded | `transfer()` ждёт consumer'а |

## 7. Executors / async

| Класс | Назначение |
|---|---|
| `Executor` / `ExecutorService` | абстракция запуска задач |
| `ThreadPoolExecutor` | основной impl: corePool, maxPool, queue, RejectedExecutionHandler |
| `ScheduledThreadPoolExecutor` | по расписанию |
| `ForkJoinPool` | work-stealing, за ним `parallelStream` |
| `Future<T>` | результат + `cancel(true)` |
| `CompletableFuture<T>` | composable async (`thenApply`, `allOf`, `orTimeout`) |
| `Executors.newXxx()` | фабрики; `newCachedThreadPool` опасен на проде |

## 8. Thread-локали

| Класс | Заметка |
|---|---|
| `ThreadLocal<T>` | per-thread; в пулах — утечки, `remove()` обязателен |
| `InheritableThreadLocal<T>` | копируется в child thread, но не в задачи ExecutorService |
| `ScopedValue` (Java 21+) | immutable замена для structured concurrency |

## 9. Поток как объект

| Класс | Что даёт |
|---|---|
| `Thread` | низкоуровневый поток; см. [Thread cancellation](Thread%20cancellation.md) |
| `Runnable` / `Callable<V>` | задача; см. [Thread vs Runnable](Thread%20vs%20Runnable.md) |
| `Thread.UncaughtExceptionHandler` | глобальный handler |

## Что выбирать

| Нужно | Бери |
|---|---|
| Видимость одного флага | `volatile` |
| Атомарный счётчик | `AtomicLong` (мало contention) / `LongAdder` (много) |
| Защитить группу полей | `synchronized` / `ReentrantLock` |
| Много читателей, мало писателей | `ReentrantReadWriteLock` / `StampedLock` |
| Дождаться готовности | `CountDownLatch` |
| Свести группу потоков | `CyclicBarrier` / `Phaser` |
| Очередь между потоками | `ArrayBlockingQueue` / `LinkedBlockingQueue` |
| Hand-off один-в-один | `SynchronousQueue` |
| Async-композиция | `CompletableFuture` |
| Параллельная декомпозиция | `ForkJoinPool` / `parallelStream` |
| Map в многопотоке | `ConcurrentHashMap` |
| Список почти-только-на-чтение | `CopyOnWriteArrayList` |
| Per-thread state | `ThreadLocal` (с `remove()`) или `ScopedValue` |
