---
tags: [java, concurrency, patterns, dcl, singleton]
---

> [Index](../../Index.md) · рядом: [volatile](volatile.md) · [happens-before](happens-before.md) · аббревиатуры → [Concurrency glossary](Concurrency%20glossary.md)

# Double-checked locking (DCL)

Ленивая инициализация singleton без блокировки на каждый `get()`.

```java
class Singleton {
    private static volatile Singleton instance;   // ← volatile обязателен

    static Singleton get() {
        Singleton local = instance;
        if (local == null) {
            synchronized (Singleton.class) {
                local = instance;
                if (local == null) {
                    instance = local = new Singleton();
                }
            }
        }
        return local;
    }
}
```

## Почему `volatile` обязателен

`new Singleton()` — это **три шага**:
1. Выделить память.
2. Запустить конструктор.
3. Присвоить ссылку в `instance`.

Без `volatile` JIT/CPU имеют право переставить (3) перед (2). Тогда другой поток в первом `if (instance == null)` без лока увидит **наполовину построенный** объект.

`volatile` ставит барьеры → запись в `instance` происходит только после конструктора и со всеми внутренними записями объекта, видными другим потокам.

## Альтернативы

- **Holder idiom** — проще и без volatile:
  ```java
  static Singleton get() { return Holder.INSTANCE; }
  private static class Holder { static final Singleton INSTANCE = new Singleton(); }
  ```
  Гарантия инициализации — JLS class initialization, лениво и thread-safe.
- `enum Singleton { INSTANCE; }` — самое простое, по Effective Java канон.

DCL обоснован, если нужна параметризованная ленивая инициализация (передать аргумент в конструктор по требованию).

## Гочи

- `local`-переменная — оптимизация: одно volatile-чтение на happy-path вместо двух.
- Без `volatile` тот же код работал бы «случайно» на x86, но падал бы на ARM/Power.
- До Java 5 DCL был **сломан** на любом железе — старая JMM не давала достаточных гарантий.
