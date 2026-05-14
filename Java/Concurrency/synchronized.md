---
tags: [java, concurrency, synchronized, jvm, locks]
---

> [Index](../../Index.md) · рядом: [wait-notify](wait-notify.md) · [happens-before](happens-before.md) · [volatile](volatile.md) · аббревиатуры → [Concurrency glossary](Concurrency%20glossary.md)

# `synchronized`

## Гарантии

| Гарантия | Через что |
|---|---|
| Mutual exclusion | Только один поток в критической секции на данный монитор |
| Reentrancy | Тот же поток входит повторно, счётчик глубины |
| Visibility | unlock *happens-before* последующего lock того же монитора |

Лок ассоциирован с **объектом**, не с блоком кода. Состояние лока живёт в **mark word** заголовка объекта.

## Эволюция локов в HotSpot

JVM не идёт сразу в OS-лок. Поднимается по уровням по мере появления contention.

| Уровень | Когда | Цена |
|---|---|---|
| **Biased** *(до Java 15, удалён в 18)* | один и тот же поток берёт многократно | почти бесплатно — проверка ID потока в mark word |
| **Lightweight (thin)** | несколько потоков, без одновременной конкуренции | CAS на mark word → указатель на displaced header в стеке |
| **Heavyweight (fat)** | реальный contention | нативный `ObjectMonitor`, OS-парковка через futex |

**Inflation** — переход lightweight → heavyweight при провале CAS. Обычно односторонний.

**Adaptive spinning** — перед уходом в OS-очередь поток спинит, JVM адаптирует длину по статистике монитора.

## Bytecode

- `synchronized (obj) { ... }` → `monitorenter` / `monitorexit` (два — happy path + exception path).
- `synchronized`-метод → флаг `ACC_SYNCHRONIZED` в дескрипторе. Лок: `this` (instance) или `Class` (static).

## Гочи

- `synchronized (this)` или `synchronized` на public объекте — **антипаттерн**: чужой код может взять твой лок. Канон — `private final Object lock = new Object();`.
- В Java 15+ biased удалена → остались два состояния.
- `synchronized` решает атомарность + видимость одновременно. Нужна **только** видимость — [volatile](volatile.md) дешевле.
- Не реагирует на [interrupt](Thread%20cancellation.md) — поток в ожидании монитора не прерывается. Альтернатива — `ReentrantLock.lockInterruptibly()`.
