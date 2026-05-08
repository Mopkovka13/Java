---
tags: [java, concurrency, wait, notify, synchronized]
---

> [[Index]] · рядом: [[Thread coordination patterns]] · [[Thread cancellation]] · [[happens-before]]

# `wait` / `notify`

Низкоуровневый примитив. На прикладе обычно прячется за `Lock`+`Condition` или `j.u.c.`-примитивами ([[Thread coordination patterns]]).

## Канонический шаблон

```java
// Consumer
synchronized (lock) {
    while (!condition) lock.wait();   // while, не if!
    consume();
}

// Producer
synchronized (lock) {
    condition = true;
    lock.notifyAll();
}
```

**Закрывает 4 из 5 ловушек ниже одной строкой `while + synchronized`.**

## Ловушки

### 1. `notify()` будит случайного — lost notification

Несколько ждущих с разными условиями на одном мониторе. `notify()` выбирает любого. Разбуженный «не тот» проверяет своё условие — false → обратно в wait. Реально нужный поток не просыпается.

**Лечение:** `notifyAll()` или `Lock` + раздельные `Condition` (`condA.signal()` будит только своих).

### 2. Barging — посторонний влезает в очередь

`synchronized` и не-fair `ReentrantLock` (default) не строят FIFO. После `notify` notified-поток ещё не получил лок — а сторонний C, только что подошедший, захватывает раньше. C съедает данные, notified приходит к пустому состоянию.

**Лечение:** `while` re-check. Нужна честная очередь — `new ReentrantLock(true)`.

### 3. Lost wakeup — `notify` пришёл раньше `wait`

Если check и wait не под одним локом, producer успевает выполнить `notify` между ними → consumer уходит в `wait` после нотификации, ждёт вечно.

**Лечение:** check **и** wait под одним `synchronized (lock)`. Producer тоже под `synchronized (lock)`.

### 4. Spurious wakeup

JVM имеет право разбудить поток без `notify`. → всегда `while`, никогда `if`.

## Сводка

| Проблема | Симптом | Лечение |
|---|---|---|
| `notify()` будит не того | висит в wait | `notifyAll()` / раздельные `Condition` |
| Barging | проснулся, условие уже не то | `while` re-check, fair lock |
| Lost wakeup | wait после прошедшего notify | check+wait под одним локом |
| Spurious wakeup | проснулся без notify | `while` |

## Гочи

- `wait()` **отдаёт** монитор на время сна. `Thread.sleep()` — **не отдаёт**.
- `notify()` vs `notifyAll()` — если не уверен, `notifyAll()`.
- `wait`/`notify` вызываются **только под `synchronized`** на этом же объекте. Иначе `IllegalMonitorStateException`.
- `wait(timeout)` реагирует на [[Thread cancellation|interrupt]] — кидает `InterruptedException`.
