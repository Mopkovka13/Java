---
tags: [java, core, object]
---

> [[Index]] · рядом: [[equals vs ==]], [[wait-notify]], [[synchronized]]

# Методы `java.lang.Object`

11 методов, наследуются всеми классами. Делятся на финальные (переопределить нельзя) и переопределяемые.

## Финальные

| Метод | Назначение |
|---|---|
| `getClass()` | Рантайм-класс объекта. Для рефлексии и `equals` через `getClass() ==`. |
| `notify()` / `notifyAll()` | Будят поток(и) на мониторе. Только из `synchronized`. См. [[wait-notify]]. |
| `wait()` / `wait(long)` / `wait(long,int)` | Освобождают монитор и ждут. Только из `synchronized`. |

## Переопределяемые

| Метод | Когда переопределять | По умолчанию |
|---|---|---|
| `equals(Object)` | Value-классы (DTO, Money, ключи Map) | `this == other` (тождество) |
| `hashCode()` | **Всегда вместе с `equals`**, по тем же полям | По идентичности (≈ адрес) |
| `toString()` | Почти всегда (логи, отладка, ошибки) | `ClassName@hexHash` |
| `clone()` | **Почти никогда** | `protected`, требует `Cloneable`, бросает checked exception |
| `finalize()` | **Никогда** (deprecated с Java 9, удалён в 18+) | Пустой |

## Правила «надо ли переопределять»

```
equals      → почти всегда для value-классов; никогда для сервисов
hashCode    → только и всегда вместе с equals (см. [[equals vs ==]])
toString    → почти всегда
clone       → нет, заменяем copy-constructor / static factory / record
finalize    → нет, заменяем AutoCloseable + try-with-resources / Cleaner
getClass    → нельзя
wait/notify → нельзя; в новом коде не вызываем (j.u.c.)
```

## Замены проблемных методов

| Старое | Замена |
|---|---|
| `clone()` | copy-constructor `new Foo(other)`, `static of(...)`, `record`, `List.copyOf` |
| `finalize()` | `AutoCloseable` + `try-with-resources`; `java.lang.ref.Cleaner` для критичных ресурсов |
| `wait/notify` | `j.u.c`: `ReentrantLock + Condition`, `BlockingQueue`, `CountDownLatch`, `Semaphore` |

## Для `record` и `enum`

- **`record`** — `equals`/`hashCode`/`toString` уже сгенерированы по компонентам. Переопределяй только при нестандартной логике.
- **`enum`** — `equals`/`hashCode` финальные в `Enum`, кастом запрещён. `==` достаточно.

## Гочи

- Дефолтный `equals` = `==`, дефолтный `hashCode` ≠ полевой → объект «теряется» в `HashMap`/`HashSet`, если переопределить только один.
- `clone()` объявлен в `Object`, но без `implements Cloneable` бросит `CloneNotSupportedException` — кривой контракт через маркерный интерфейс.
- `finalize()` мог воскрешать объект, тормозил GC, исполнялся не гарантировано — отсюда полный отказ.
- `wait()` без цикла `while(!condition)` ломается на spurious wakeup — см. [[wait-notify]].
