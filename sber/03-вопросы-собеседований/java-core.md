# Java Core — вопросы собеседования (Сбер)

> 📝 Самопроверка: краткие ответы на 40 ключевых вопросов (блоки совпадают с этим файлом) — в [ответы-java-core.md](ответы/ответы-java-core.md).

Сбер любит «как это работает под капотом» и контракты языка. Глубина важнее ширины.

## ООП и язык

- Принципы ООП: инкапсуляция, наследование, полиморфизм, абстракция — с примерами из своего кода.
- SOLID — спрашивают почти всегда; готовь пример нарушения и исправления для каждого принципа.
- Отличие абстрактного класса от интерфейса (default-методы, множественная реализация).
- Модификаторы доступа, static, final, иммутабельность (как сделать класс immutable, зачем String immutable).
- String pool, intern(), почему String — ключ HashMap.
- Автоупаковка, кеш Integer от -128 до 127 (классическая ловушка `Integer == Integer`).
- Перегрузка vs переопределение, ковариантные возвращаемые типы.
- Generics: type erasure, wildcards (`? extends` / `? super`, PECS), почему нельзя `new T[]`.
- Записи (records), sealed-классы, switch-паттерны — фичи Java 17/21.

## Контракты (фирменная тема Сбера)

- **equals/hashCode**: контракт полностью, последствия нарушения в коллекциях.
- **Comparable/Comparator**: контракт compareTo, консистентность с equals (TreeMap!).
- **Serializable vs Externalizable**: serialVersionUID, transient, порядок полей в writeExternal/readExternal, обязательный public-конструктор без аргументов для Externalizable.
- **Cloneable/clone()**: поверхностное vs глубокое копирование, CloneNotSupportedException без Cloneable, почему clone считают анти-паттерном (копирующий конструктор лучше).

## Коллекции

- Иерархия Collection/Map; когда что выбирать.
- ArrayList vs LinkedList (реальная сложность операций, почему LinkedList почти всегда хуже).
- HashMap под капотом (см. топ-12), LinkedHashMap (access-order → LRU-кеш), TreeMap (красно-чёрное дерево).
- HashSet через HashMap.
- fail-fast vs fail-safe итераторы, ConcurrentModificationException.
- Comparable для TreeSet/TreeMap; что будет, если ключи не сравнимы.
- Сложность операций по всем структурам — O(1)/O(log n)/O(n) наизусть.

## JVM и память

- Структура памяти: heap (young/old gen), stack, metaspace.
- Garbage Collection: поколенческая гипотеза, minor/major GC; сборщики G1 (дефолт), ZGC, Shenandoah — когда какой.
- Ссылки: strong, soft, weak, phantom; WeakHashMap.
- StackOverflowError vs OutOfMemoryError; как диагностировать утечку памяти (heap dump, MAT, jmap/jcmd).
- JIT-компиляция, класслоадеры (иерархия, делегирование).
- Параметры: -Xmx, -Xms, -XX:MaxMetaspaceSize; JVM в контейнере (UseContainerSupport, лимиты).

## Исключения

- Иерархия Throwable: checked vs unchecked vs Error.
- try-with-resources, AutoCloseable, suppressed exceptions.
- finally и return (что вернётся), исключение в finally.
- Своя иерархия исключений в проекте: зачем и как.

## Stream API и функциональные интерфейсы

- Промежуточные vs терминальные операции, ленивость.
- map/flatMap, collect (groupingBy, toMap — ловушка с дубликатами ключей), reduce.
- parallelStream: когда нельзя (общий ForkJoinPool, side effects).
- Optional: правильное использование (не для полей/параметров).
- Функциональные интерфейсы: Function, Supplier, Consumer, Predicate, BiFunction; ссылки на методы.

## Где брать ответы

- [GitHub enhorse/java-interview — самый полный сборник вопросов-ответов на русском](https://github.com/enhorse/java-interview)
- [Хабр: 60 вопросов Java core для backend](https://habr.com/ru/articles/485678/)
- [Хабр: Собеседование Backend-Java ч.1](https://habr.com/ru/articles/529210/) и [ч.2](https://habr.com/ru/articles/529214/)
