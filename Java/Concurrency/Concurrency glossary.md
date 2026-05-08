---
tags: [java, concurrency, glossary, moc]
---

> [[Index]] · сокращения и термины из всех заметок раздела Concurrency.

# Concurrency — глоссарий

## Аббревиатуры

| Сокр. | Полное | Что |
|---|---|---|
| **hb** | happens-before | сокращение в коде/тексте: «A hb B» = «A happens-before B»; см. [[happens-before]] |
| **JMM** | Java Memory Model | формальная модель памяти Java; правила видимости и упорядоченности; см. [[happens-before]] |
| **JLS** | Java Language Specification | спецификация языка |
| **JVM** | Java Virtual Machine | виртуальная машина; см. [[JDK vs JRE vs JVM]] |
| **JIT** | Just-In-Time compiler | runtime-компилятор bytecode → native в JVM (C1/C2/Graal) |
| **GC** | Garbage Collector | сборщик мусора; см. [[GC fundamentals]] |
| **HotSpot** | — | основная open-source реализация JVM от Oracle/OpenJDK |
| **j.u.c.** | `java.util.concurrent` | пакет concurrent-примитивов и коллекций |
| **AQS** | `AbstractQueuedSynchronizer` | базовый класс под `ReentrantLock`/`Semaphore`/`CountDownLatch` — состояние + FIFO-очередь, всё на [[CAS]] |
| **CAS** | Compare-And-Swap | атомарная инструкция CPU «сравни и замени»; см. [[CAS]] |
| **LL/SC** | Load-Linked / Store-Conditional | пара инструкций ARM/Power, аналог CAS на слабых моделях |
| **ABA** | A → B → A | значение успело смениться и вернуться — CAS этого не заметит; см. [[CAS]] |
| **DCL** | Double-Checked Locking | паттерн ленивого singleton; см. [[Double-checked locking]] |
| **RMW** / **R-M-W** | Read-Modify-Write | трёхшаговая операция (`count++` = read+add+write); не атомарна без CAS/lock |
| **R/W** | Read / Write | чтение / запись |
| **CME** | `ConcurrentModificationException` | бросается fail-fast итераторами при модификации коллекции во время обхода |
| **FIFO** | First-In First-Out | очередь в порядке поступления |
| **TSO** | Total Store Ordering | сильная модель памяти x86: stores не реордерятся между собой, единственный реордер — load-after-store |
| **NIO** | New I/O (`java.nio`) | non-blocking I/O API: каналы, селекторы, буферы |
| **futex** | Fast Userspace muTEX | примитив ядра Linux для эффективной парковки потоков; на нём построен heavyweight lock в [[synchronized]] |
| **mark word** | — | первые 64 бита заголовка объекта в HotSpot; хранят lock state, identity hash, GC age |

## Термины

| Термин | Что |
|---|---|
| **happens-before** | hb — формальное правило JMM «A видно B»; см. [[happens-before]] |
| **piggy-backing** | публикация обычных данных через единственный volatile-флаг (см. [[volatile]]) |
| **lock-free** | хотя бы один поток прогрессирует; конкретный может ретраить |
| **wait-free** | каждый поток за конечное число шагов |
| **weakly consistent iterator** | не кидает `CME`, но не гарантирует видимость свежих изменений |
| **fail-fast iterator** | при модификации во время обхода кидает `CME` |
| **spurious wakeup** | пробуждение `wait()` без `notify` — JVM имеет право; см. [[wait-notify]] |
| **barging** | сторонний поток захватывает лок раньше notified; см. [[wait-notify]] |
| **biased / lightweight / heavyweight lock** | три уровня локов в HotSpot; см. [[synchronized]] |
| **inflation** | переход lightweight → heavyweight при contention |
| **store buffer** | очередь записей CPU перед коммитом в кеш |
| **memory barrier / fence** | инструкция CPU, запрещающая реордеринг; см. [[Memory barriers]] |
