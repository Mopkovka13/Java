---
tags: [java, concurrency, patterns, signaling, moc]
---

> [Index](../../Index.md) · подтемы: [wait-notify](wait-notify.md) · [Thread cancellation](Thread%20cancellation.md) · [Thread vs Runnable](Thread%20vs%20Runnable.md) · аббревиатуры → [Concurrency glossary](Concurrency%20glossary.md)

# Координация потоков — обзор

Как один поток ждёт сигнал от другого. От худшего к лучшему.

## Способы ожидания

| Способ | Когда |
|---|---|
| `while (!flag) {}` busy-wait | почти никогда — 100% CPU |
| `while (!flag) Thread.onSpinWait()` | lock-free hot path, наносекунды |
| `while (!flag) Thread.sleep(ms)` polling | редкие проверки, не критична латентность |
| `wait`/`notify` ([wait-notify](wait-notify.md)) | низкоуровневая база, обычно скрыта |
| `j.u.c.`-примитивы (см. ниже) | **по умолчанию это** |

## `j.u.c.`-примитивы

| Задача | Инструмент |
|---|---|
| Дождаться однократного «готово» | `CountDownLatch` |
| Передать **данные** между потоками | `BlockingQueue` |
| Producer ↔ consumer одного значения | `Exchanger`, `SynchronousQueue` |
| Пропустить N потоков | `Semaphore` |
| Групповой барьер | `CyclicBarrier`, `Phaser` |
| Своё условие ожидания | `ReentrantLock` + `Condition` |

```java
CountDownLatch ready = new CountDownLatch(1);
ready.await();             // consumer блокируется
ready.countDown();         // producer сигналит
```

## Правило

> Пишешь `while (!volatileFlag)` — спроси, не сделает ли `CountDownLatch`/`BlockingQueue` это лучше. В 90% случаев — да.

## Связанные темы

- [wait-notify](wait-notify.md) — низкоуровневые примитивы и их ловушки.
- [Thread cancellation](Thread%20cancellation.md) — отмена через `interrupt()`.
- [Thread vs Runnable](Thread%20vs%20Runnable.md) — почему `Runnable` + executor.
- [volatile](volatile.md) — для простых флагов и публикации.
