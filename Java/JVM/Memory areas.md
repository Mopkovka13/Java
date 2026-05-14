---
tags: [java, jvm, memory]
---

> [Index](../../Index.md) · рядом: [JDK vs JRE vs JVM](JDK%20vs%20JRE%20vs%20JVM.md) · [Memory playground](Memory%20playground.md) · [equals vs ==](../Language%20Core/equals%20vs%20==.md) · [GC fundamentals](GC/GC%20fundamentals.md)

# Области памяти JVM

```
JVM process
├── Per-thread (своё у каждого потока)
│   ├── Stack            — фреймы вызовов: локальные переменные, operand stack
│   ├── PC Register      — адрес текущей байткод-инструкции
│   └── Native Stack     — для JNI / нативных вызовов
└── Shared (общее для всех потоков)
    ├── Heap             — все объекты (new), массивы, java.lang.Class
    └── Metaspace        — метаданные классов (off-heap, нативная память)
```

## 1. Stack (поток-локальный)

- На каждый поток — свой стек.
- Состоит из **stack frames** — по одному на вызов метода. Фрейм содержит:
  - массив локальных (примитивы, ссылки),
  - operand stack (для байткод-инструкций),
  - ссылку на constant pool класса.
- При `return` — фрейм снимается; при вызове — кладётся.
- Размер: `-Xss` (default ~512KB–1MB).
- Примитивы **локальные** → в стеке. Примитивы **как поля объекта** → в heap внутри объекта.

## 2. Heap (общий)

- Все объекты, создаваемые через `new`, и массивы.
- Сюда же — `java.lang.Class<?>` со **статическими полями** класса (с Java 8+).
- Сюда же — String Pool (с Java 7+, раньше в PermGen).
- Управляется GC.
- Структура (для большинства GC):
  - **Young** = Eden + Survivor (S0/S1) — новые объекты,
  - **Old (Tenured)** — пережили несколько GC.
- Размер: `-Xms` (initial), `-Xmx` (max).

## 3. Metaspace (общий, off-heap)

- Появился в Java 8 вместо **PermGen**.
- Хранит **только метаданные**: `Klass`, дескрипторы методов, байткод методов, constant pool класса.
- **Статика тут НЕ хранится** — она в Heap, как поля объекта `Class<?>`.
- Растёт автоматически (нативная память ОС). Лимит: `-XX:MaxMetaspaceSize`.

## 4. PC Register / Native Stack

- **PC Register** — на поток, указатель на текущую инструкцию байткода.
- **Native Stack** — на поток, стек для нативных методов (JNI).

## 5. Что где живёт — таблица

| Данные | Где |
|---|---|
| Локальная переменная (примитив или ссылка) | Stack (фрейм метода) |
| Объект `new Foo()` | Heap |
| Массив `new int[10]` | Heap |
| Поле объекта (примитив) | Heap, внутри объекта |
| Поле объекта (ссылка) | Heap, внутри объекта; объект-цель тоже в Heap |
| **Статическое поле** | Heap (внутри `Class<?>`-объекта) |
| Метаданные класса, байткод методов | Metaspace |
| Строка-литерал `"abc"` | Heap (String Pool) |
| Direct ByteBuffer | Off-heap (нативная память) |

## 6. Ошибки памяти

| Где упёрлось | Что бросается |
|---|---|
| Stack потока (рекурсия) | **StackOverflowError** |
| Heap | `OutOfMemoryError: Java heap space` |
| Metaspace | `OutOfMemoryError: Metaspace` |
| GC >98% времени, <2% свободы | `OutOfMemoryError: GC overhead limit exceeded` |
| Off-heap (`DirectByteBuffer`) | `OutOfMemoryError: Direct buffer memory` |
| ОС-лимит на потоки | `OutOfMemoryError: unable to create native thread` |

## Гочи

- **Статика — в Heap**, не в Metaspace (миф из эпохи PermGen, до Java 8).
- В Metaspace — описание класса, не его данные.
- Stack у каждого потока **свой**, Heap и Metaspace — **общие**.
- JIT с **escape analysis** может разместить объект на стеке/в регистрах, если он не утекает за пределы метода (scalar replacement).
- String Pool с Java 7 переехал в Heap.
- PermGen в Java 8 убрали, заменили Metaspace.
