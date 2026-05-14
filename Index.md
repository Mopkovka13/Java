---
tags: [index, moc]
---

# Index — карта знаний

Главная точка входа. Все темы — отсюда.

## Java

### JVM
- [JDK vs JRE vs JVM](Java/JVM/JDK%20vs%20JRE%20vs%20JVM.md) — путь от `.java` до исполнения, `javac`, JIT, `jlink`
- [Memory areas](Java/JVM/Memory%20areas.md) — Stack / Heap / Metaspace, статика, OOM/StackOverflow
- [Memory playground](Java/JVM/Memory%20playground.md) — как пощупать память: VisualVM + Gradle-проект

#### GC
- [GC fundamentals](Java/JVM/GC/GC%20fundamentals.md) — reachability, GC Roots, tracing vs reference counting

### Language Core
- [Object methods](Java/Language%20Core/Object%20methods.md) — что наследуем от `Object` и что стоит переопределять
- [equals vs ==](Java/Language%20Core/equals%20vs%20==.md) — identity vs equality, контракт `equals`/`hashCode`, String pool, Integer cache
- [clone и Cloneable](Java/Language%20Core/clone%20и%20Cloneable.md) — shallow/deep копии, почему дизайн сломан, чем заменять

### Collections
- [HashMap](Java/Collections/HashMap.md) — структура, `put`/`get`, treeify, resize, многопоточка

### Concurrency

- [Concurrency cheatsheet](Java/Concurrency/Concurrency%20cheatsheet.md) — сводная таблица всех concurrent-сущностей
- [Concurrency glossary](Java/Concurrency/Concurrency%20glossary.md) — расшифровки аббревиатур и терминов раздела

**JMM / память**
- [happens-before](Java/Concurrency/happens-before.md) — формальное правило видимости JMM, фундамент thread-safety
- [volatile](Java/Concurrency/volatile.md) — гарантии, piggy-backing, когда применять
- [Memory barriers](Java/Concurrency/Memory%20barriers.md) — `LoadLoad`/`StoreStore`/`StoreLoad`/`LoadStore`, расстановка для volatile
- [CAS](Java/Concurrency/CAS.md) — compare-and-swap, ABA, lock-free базис атомиков и AQS

**Координация / синхронизация**
- [Thread coordination patterns](Java/Concurrency/Thread%20coordination%20patterns.md) — обзор: способы ожидания, j.u.c.-примитивы
- [synchronized](Java/Concurrency/synchronized.md) — гарантии, эволюция локов в HotSpot, bytecode
- [wait-notify](Java/Concurrency/wait-notify.md) — низкоуровневый шаблон + 4 классические ловушки
- [Double-checked locking](Java/Concurrency/Double-checked%20locking.md) — ленивый singleton, зачем тут volatile

**Жизненный цикл потока**
- [Thread cancellation](Java/Concurrency/Thread%20cancellation.md) — `interrupt()`, eaten interrupt, шаблон `safe*`
- [Thread vs Runnable](Java/Concurrency/Thread%20vs%20Runnable.md) — почему `Runnable` + executor

### Generics
*(пусто)*

### IO / NIO
*(пусто)*

## Spring
*(пусто)*

## Hibernate
*(пусто)*

## jOOQ
*(пусто)*

## Redis
*(пусто)*

## Kafka
*(пусто)*

## GraphQL
*(пусто)*

## CS / Data Structures
- [Red-black tree](CS/Data%20Structures/Red-black%20tree.md) — самобалансируемое BST, фундамент TreeMap и treeify-бакетов

## Полигоны (практика)
- [Memory playground](Java/JVM/Memory%20playground.md) — эксперименты с памятью JVM

---

> Открой граф (`Ctrl+G`) — увидишь связи между заметками.
