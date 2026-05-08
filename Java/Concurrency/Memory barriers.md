---
tags: [java, concurrency, jmm, memory-barriers]
---

> [[Index]] · рядом: [[volatile]] · [[happens-before]]

# Memory barriers

Инструкция CPU, запрещающая переставлять операции с памятью через себя. Нужна, потому что CPU агрессивно реордерит: store buffer, спекулятивные load'ы, etc.

## Четыре типа

| Барьер | Запрещает |
|---|---|
| `LoadLoad` | переставить чтение ДО с чтением ПОСЛЕ |
| `LoadStore` | чтение ДО с записью ПОСЛЕ |
| `StoreStore` | запись ДО с записью ПОСЛЕ |
| `StoreLoad` | запись ДО с чтением ПОСЛЕ — требует **сброса store buffer**, самый дорогой |

## Расстановка для `volatile`

| Доступ | Барьеры |
|---|---|
| volatile **write** | `StoreStore` ПЕРЕД (фундамент piggy-backing) + `StoreLoad` ПОСЛЕ |
| volatile **read** | `LoadLoad` + `LoadStore` ПОСЛЕ |

## Цена

- **x86** (TSO): `LoadLoad`/`LoadStore`/`StoreStore` почти бесплатны. `StoreLoad` дорогой → именно volatile **write** ощутимо стоит.
- **ARM/Power** (слабая модель): нужны явные `dmb`/`sync`, все барьеры реально что-то стоят.

## Правило

Думай в терминах [[happens-before]]-рёбер. Барьеры — деталь реализации JVM/CPU, как именно они обеспечивают hb.
