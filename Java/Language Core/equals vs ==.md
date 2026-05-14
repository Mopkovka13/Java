---
tags: [java, core, equals, hashcode]
---

> [Index](../../Index.md) · рядом: [Memory areas](../JVM/Memory%20areas.md) (heap & String pool)

# `==` vs `equals` и контракт `equals`/`hashCode`

## Что сравнивает `==`

| Тип операндов | Сравнивает |
|---|---|
| Примитивы (`int`, `boolean`, …) | значения |
| Ссылочные типы | **ссылки** (тождество объектов) |

Поэтому для объектов `==` отвечает на «это **один и тот же** объект?», а `equals` — «они **равны по смыслу**?».

## `equals` по умолчанию

В `Object.equals(Object)` — это `this == other`. То есть пока класс не переопределил, `equals` ничем не лучше `==`.

## String pool

```java
"abc" == "abc"                  // true  — оба из пула
new String("abc") == "abc"      // false — new всегда новый объект в heap
new String("abc").intern() == "abc"  // true
"a" + "bc" == "abc"             // true  — компилятор свернул в литерал
```

- В пул попадают **литералы** и результаты `intern()`.
- С Java 7 пул в Heap (раньше — в PermGen).
- Динамическая конкатенация (`"a" + var`) — новый объект, **не** из пула.

## Integer cache

```java
Integer a = 127, b = 127;   // a == b → true   (кэш -128..127)
Integer c = 128, d = 128;   // c == d → false  (новые)
```

Кэш `IntegerCache`: `-128..127`, верх через `-XX:AutoBoxCacheMax`. Так же кэшируются `Boolean`, `Byte`, `Short`, маленькие `Long`, `Character (0..127)`.

> Никогда не сравнивай обёртки через `==`. Только `equals` или `Objects.equals`.

## Контракт `equals` (5 свойств)

1. **Рефлексивность:** `x.equals(x) == true`.
2. **Симметрия:** `x.equals(y) == y.equals(x)`.
3. **Транзитивность:** `x.equals(y) && y.equals(z) ⇒ x.equals(z)`.
4. **Консистентность:** при неизменных объектах — стабильный результат.
5. **Non-null:** `x.equals(null) == false`.

Классическое нарушение симметрии: `java.sql.Timestamp extends Date` — `Date.equals(Timestamp)` ≠ `Timestamp.equals(Date)`. Лекарство — `getClass() == other.getClass()` для расширяемых типов.

## Контракт `hashCode`

- `a.equals(b) == true` ⇒ `a.hashCode() == b.hashCode()`. **Обязательно.**
- Совпадение `hashCode` не обязывает `equals` быть `true` — это **коллизия**, нормально.
- В пределах одного запуска JVM `hashCode` стабилен.

## Что ломается при нарушении

- `HashMap.put(k,v)` → `get(k)` возвращает `null`. Объект ушёл в другую корзину.
- `HashSet` хранит «дубликаты» (с т. з. `equals`).
- Самый коварный случай: `equals == true`, `hashCode` разный — баги вылезают через месяцы (rehash, смена реализации).

## Идиомы

- Переопределил `equals` — **обязательно** переопредели `hashCode`.
- `Objects.equals(a, b)` — null-safe.
- `Objects.hash(f1, f2, …)` — простой и корректный hashCode.
- **Не используй мутабельные поля** в `equals`/`hashCode`, если объект — ключ `HashMap`/`HashSet`. Изменил поле после `put` — `get` не найдёт.
- **Records** (Java 14+) — `equals`/`hashCode`/`toString` сгенерированы автоматически по компонентам.
- **Enum** — `==` достаточно, кастом не нужен.

## Канонический шаблон `equals` (для классической иерархии)

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MyClass m)) return false;   // pattern matching, Java 16+
    return Objects.equals(field1, m.field1)
        && field2 == m.field2;
}

@Override
public int hashCode() {
    return Objects.hash(field1, field2);
}
```

## Гочи

- Дефолтный `equals` = `==` (тождество ссылок).
- `new String(...)` всегда **не** из пула.
- Кэш обёрток `-128..127` ломает интуицию `==` для `Integer`.
- Нарушение `equals/hashCode` → молчаливо разрушает `HashMap`/`HashSet`.
- Симметрия чаще всего ломается в наследовании — отсюда совет `getClass()` вместо `instanceof`.
