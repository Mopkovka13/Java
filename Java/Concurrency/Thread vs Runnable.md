---
tags: [java, concurrency, thread, runnable]
---

> [[Index]] · рядом: [[Thread coordination patterns]] · [[Thread cancellation]] · аббревиатуры → [[Concurrency glossary]]

# `extends Thread` vs `Runnable`

`extends Thread` — антипаттерн почти всегда. Канон — `Runnable`/`Callable` + `ExecutorService`.

| `extends Thread` | `Runnable` / `Callable` |
|---|---|
| Жёстко привязан к Thread API | Кладётся в любой `ExecutorService` |
| Один объект = один запуск | Можно запускать многократно |
| Занят слот наследования | Свободен для бизнес-иерархии |
| Смешивает «что делать» и «как исполнять» | Разделяет логику и runtime |

## Канон

```java
ExecutorService pool = Executors.newSingleThreadExecutor();
Future<?> task = pool.submit(() -> {
    try {
        while (!Thread.currentThread().isInterrupted()) {
            Thread.sleep(200);
            work();
        }
    } catch (InterruptedException ignored) { }
});

task.cancel(true);   // под капотом — interrupt(), см. [[Thread cancellation]]
pool.shutdown();
```

## Когда `extends Thread` оправдан

Почти никогда. Исключения — нужно переопределить что-то у самого `Thread` (что почти не нужно).
