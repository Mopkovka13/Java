---
tags: [index, moc]
---

# Index — карта знаний

Главная точка входа. Все темы — отсюда.

## Java

### JVM
- [[JDK vs JRE vs JVM]] — путь от `.java` до исполнения, `javac`, JIT, `jlink`
- [[Memory areas]] — Stack / Heap / Metaspace, статика, OOM/StackOverflow
- [[Memory playground]] — как пощупать память: VisualVM + Gradle-проект

#### GC
- [[GC fundamentals]] — reachability, GC Roots, tracing vs reference counting

### Language Core
- [[equals vs ==]] — identity vs equality, контракт `equals`/`hashCode`, String pool, Integer cache

### Collections
- [[HashMap]] — структура, `put`/`get`, treeify, resize, многопоточка

### Concurrency

**JMM / память**
- [[happens-before]] — формальное правило видимости JMM, фундамент thread-safety
- [[volatile]] — гарантии, piggy-backing, когда применять
- [[Memory barriers]] — `LoadLoad`/`StoreStore`/`StoreLoad`/`LoadStore`, расстановка для volatile

**Координация / синхронизация**
- [[Thread coordination patterns]] — обзор: способы ожидания, j.u.c.-примитивы
- [[wait-notify]] — низкоуровневый шаблон + 4 классические ловушки
- [[Double-checked locking]] — ленивый singleton, зачем тут volatile

**Жизненный цикл потока**
- [[Thread cancellation]] — `interrupt()`, eaten interrupt, шаблон `safe*`
- [[Thread vs Runnable]] — почему `Runnable` + executor

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
- [[Red-black tree]] — самобалансируемое BST, фундамент TreeMap и treeify-бакетов

## Полигоны (практика)
- [[Memory playground]] — эксперименты с памятью JVM

---

> Открой граф (`Ctrl+G`) — увидишь связи между заметками.
