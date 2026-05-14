---
tags: [java, collections, hashmap]
---

> [Index](../../Index.md) · рядом: [equals vs ==](../Language%20Core/equals%20vs%20==.md) · [Red-black tree](../../CS/Data%20Structures/Red-black%20tree.md) · [ConcurrentHashMap](ConcurrentHashMap.md)

# HashMap

## Структура

```
table: Node<K,V>[]   ← массив, длина = степень двойки
  [0] → Node → Node → Node                ← linked list
  [1] → null
  [2] → TreeNode (root) … …               ← красно-чёрное дерево
```

`Node` хранит: `hash`, `key`, `value`, `next`.

## Как считается бакет

```java
int hash = (h = key.hashCode()) ^ (h >>> 16);   // spreading
int bucket = (n - 1) & hash;                    // n — длина table
```

- XOR верхних и нижних бит — чтобы плохие `hashCode` не клались в одну корзину.
- `(n-1) & hash` ≡ `hash mod n`, но дешевле (n — степень двойки).

## `put` пошагово

1. Считаем `hash` и бакет.
2. Если бакет пуст → кладём `Node`.
3. Если есть — идём по списку/дереву.
4. Сравниваем по **`hash`** + **`equals`**:
   - совпало → **replace value**.
   - не совпало → **add** в конец списка / в дерево.
5. После вставки проверяем:
   - длина бакета ≥ `TREEIFY_THRESHOLD (8)` и `table.length ≥ 64` → **treeify**.
   - `size > capacity * loadFactor` → **resize**.

## `get` пошагово

1. Считаем `hash` → бакет.
2. По списку/дереву ищем по `hash` + `equals`.
3. Не нашли — `null`.

## Treeify / Untreeify

| Константа | Значение | Что означает |
|---|---|---|
| `TREEIFY_THRESHOLD` | 8 | бакет ≥ 8 → дерево |
| `MIN_TREEIFY_CAPACITY` | 64 | если `table.length < 64` → **resize** вместо treeify |
| `UNTREEIFY_THRESHOLD` | 6 | при удалении ≤ 6 → обратно в список |

> Treeify работает только если ключи `Comparable`. Иначе остаётся linked list.

## Resize

- Триггер: `size > capacity * loadFactor`.
- По умолчанию `initialCapacity = 16`, `loadFactor = 0.75` → первый resize после 12-го элемента.
- Размер удваивается.
- Java 8+: hash не пересчитывается — `hash & oldCap` определяет, остаться в текущей корзине или съехать в `oldIndex + oldCap`.
- O(n) на ресайз, но `put` амортизированно O(1).

## Сложности

| | Best | Average | Worst (list) | Worst (tree) |
|---|---|---|---|---|
| `put`/`get` | O(1) | O(1) | O(n) | O(log n) |

«Worst O(log n)» только если бакет уже стал [красно-чёрным деревом](../../CS/Data%20Structures/Red-black%20tree.md).

## Null

| | Можно? |
|---|---|
| `null` ключ | да, **один**, лежит в bucket 0 (hash = 0) |
| `null` значение | да, сколько угодно |

## Многопоточка

`HashMap` **не** thread-safe. До Java 8 конкурентный `resize` мог зациклить список — знаменитый «100% CPU» баг. С Java 8 цикла нет, но порча данных и `ConcurrentModificationException` остались.

| Класс | Когда |
|---|---|
| [ConcurrentHashMap](ConcurrentHashMap.md) | стандартный выбор |
| `Collections.synchronizedMap` | один глобальный лок, медленно |
| `Hashtable` | легаси, не используй |

## Fail-fast iterator

- `HashMap` iterator проверяет `modCount` → `ConcurrentModificationException` при модификации во время итерации (даже однопоточной).
- `ConcurrentHashMap` iterator — **weakly consistent**, не падает, но и не гарантирует увидеть новые элементы.

## Гочи

- `equals`/`hashCode` — фундамент. Нарушил [контракт](../Language%20Core/equals%20vs%20==.md) → `get` теряет элемент.
- **Мутабельные ключи запрещены**. Поменял поле, входящее в `hashCode`, после `put` → `get` уйдёт в другой бакет.
- Treeify нужен `Comparable`-ключи.
- Capacity всегда округляется вверх до степени двойки.
- Iteration order **не определён**. Нужен порядок — `LinkedHashMap` (insertion / access).
