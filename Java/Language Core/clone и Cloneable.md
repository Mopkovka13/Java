---
tags: [java, core, clone, cloneable]
---

> [Index](../../Index.md) · рядом: [Object methods](Object%20methods.md), [equals vs ==](equals%20vs%20==.md)

# `clone()` и `Cloneable`

## Что делает `Object.clone()`

`protected native Object clone()`. Создаёт новый объект того же класса, **побайтово** копирует поля (shallow). **Конструктор не вызывает.**

Без `implements Cloneable` → `CloneNotSupportedException`.

## `Cloneable` — пустой маркер

```java
public interface Cloneable { }
```

Не объявляет `clone()`. Просто разрешает `Object.clone()` не бросать исключение. **Главная кривость дизайна.**

## Shallow vs Deep

| Тип поля | Shallow (default) | Deep |
|---|---|---|
| Примитив | копия значения | копия значения |
| Immutable ссылка (`String`, `Integer`…) | та же ссылка — ОК | та же ссылка |
| Mutable ссылка (`List`, `Date`, массив) | **та же ссылка → общий объект!** | новая копия (рекурсивно) |

```java
Order b = a.clone();        // shallow
b.items.add(...)            // меняется и a.items!
```

## Проблемы дизайна

1. Маркерный интерфейс вместо нормального метода.
2. `protected` — приходится переопределять как `public`.
3. Checked `CloneNotSupportedException` — try/catch ради ничего.
4. **Не вызывает конструктор** → инварианты, проверки, final-инициализация в обход.
5. С `final` mutable-полями глубокая копия невозможна (нечем переприсвоить).
6. До Java 5 возвращал `Object` (нужен каст). Сейчас covariant return спасает.
7. Хрупкое наследование: предок «сломал» — потомок не починит.

## Правильная реализация (если уж надо)

```java
public final class Foo implements Cloneable {
    private final int x;
    private int[] arr;
    private Date date;             // mutable

    @Override
    public Foo clone() {
        try {
            Foo c = (Foo) super.clone();   // обязательно super.clone(), не new!
            c.arr = arr.clone();           // массив — отдельно
            c.date = (Date) date.clone();  // mutable поле — отдельно
            return c;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);   // мы Cloneable, не случится
        }
    }
}
```

Правила:
- `public`, covariant return (конкретный тип).
- `super.clone()` обязательно — иначе у потомков будет неверный класс.
- Каждое mutable ссылочное поле копировать вручную.
- `final` + mutable = корректно не сделать.

## Альтернативы (предпочтительно)

| Подход | Когда |
|---|---|
| **Copy constructor** `new Foo(other)` | Дефолт. Явно, типобезопасно, дружит с `final`. |
| **Static factory** `Foo.copyOf(other)` | Когда хочется именованного смысла. |
| **`record`** + `with`-методы | Иммутабельные данные — копия не нужна. |
| `List.copyOf`, `new ArrayList<>(src)`, `Map.copyOf` | Коллекции. |
| `arr.clone()`, `Arrays.copyOf`, `System.arraycopy` | Массивы — здесь `clone()` идиоматичен. |
| `SerializationUtils.clone` (Apache), Jackson/Kryo | Тяжёлый deep copy большого графа. Медленно, но универсально. |

## Используется ли сейчас?

В новом коде — **почти нет**. Bloch (Effective Java, item 13) против. Реальные кейсы:
- `array.clone()` — да, это нормально.
- Поддержка legacy `Cloneable`-классов.
- Некоторые JDK-классы (`Calendar.clone()`, `Date.clone()`).

Для своих DTO/value-классов: `record` или copy-constructor.

## Гочи

- `super.clone()` нельзя заменить на `new Foo(...)` — у потомков будет неправильный рантайм-класс.
- Shallow-копия mutable полей — самый частый источник «магических» багов.
- `clone()` не вызывает конструктор → инициализация в конструкторе обходится. Если в нём была валидация — её не будет.
- `Cloneable` без переопределения `clone()` бессмыслен: метод останется `protected` от `Object`.
