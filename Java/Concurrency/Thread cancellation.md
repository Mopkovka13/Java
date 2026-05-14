---
tags: [java, concurrency, interrupt, cancellation]
---

> [Index](../../Index.md) · рядом: [Thread coordination patterns](Thread%20coordination%20patterns.md) · [Thread vs Runnable](Thread%20vs%20Runnable.md) · [wait-notify](wait-notify.md) · аббревиатуры → [Concurrency glossary](Concurrency%20glossary.md)

# Отмена потока

Канон — **`Thread.interrupt()`**, не `volatile boolean stop`. Interrupt прерывает блокирующие вызовы (`sleep`, `wait`, `BlockingQueue.take`, NIO) → `InterruptedException`. Голый флаг — нет.

## Канонический шаблон воркера

```java
public void run() {
    try {
        while (!Thread.currentThread().isInterrupted()) {
            Thread.sleep(200);   // пусть кидает
            work();
        }
    } catch (InterruptedException e) {
        // вышли из цикла, поток умирает
    }
}
```

`sleep` сам становится точкой отмены — не нужен внешний флаг.

## `isInterrupted()` vs `interrupted()`

| Метод | Что делает |
|---|---|
| `thread.isInterrupted()` (instance) | читает флаг, **не сбрасывает** |
| `Thread.interrupted()` (static) | читает флаг и **сбрасывает** |
| Бросок `InterruptedException` | **сбрасывает** флаг автоматически |

В цикле опроса всегда **instance**.

## Eaten interrupt — главная ловушка

```java
try { Thread.sleep(1000); }
catch (InterruptedException e) { /* проглотили */ }
// флаг сброшен, верхний код не узнает → поток не остановится
```

**Правильно:**
```java
catch (InterruptedException e) {
    Thread.currentThread().interrupt();   // восстановить флаг
    // или пробросить выше
}
```

## Шаблон `safe*`-обёрток

Корректная обёртка sleep/join восстанавливает флаг **на текущем потоке**:

```java
static void safeSleep(long ms) {
    try { Thread.sleep(ms); }
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
}
```

### Типичный баг — `safeJoin`

```java
static void safeJoin(Thread t) {
    try { t.join(); }
    catch (InterruptedException e) {
        t.interrupt();   // ❌ НЕ caller, а joined-поток
    }
}
```

`join()` блокирует **caller'а**. Interrupt пришёл caller'у. В catch надо восстановить флаг **caller'у**, а не передавать joined-потоку.

**Правило:** в шаблоне «catch → восстановить флаг → выйти» **всегда** `Thread.currentThread().interrupt()`. Звать `interrupt()` на другом потоке — это уже передача отмены вперёд, отдельная семантика.

## Подозрительные имена

`safeSleep`, `quietSleep`, **`Uninterruptibles.sleepUninterruptibly` (Guava)** — проверять глазами. Часто глотают `InterruptedException` без восстановления флага → ломают отмену во всём, что их использует.

## Гочи

- `Future.cancel(true)` под капотом — `interrupt()`. См. [Thread vs Runnable](Thread%20vs%20Runnable.md).
- `volatile boolean stop` оправдан **только** в чисто CPU-bound цикле без блокировок.
- Поток в `synchronized (lock)` блок **не реагирует** на interrupt. Используй `lock.lockInterruptibly()`.
