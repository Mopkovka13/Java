---
tags: [java, jvm, memory, playground]
---

> [Index](../../Index.md) · рядом: [Memory areas](Memory%20areas.md) · [JDK vs JRE vs JVM](JDK%20vs%20JRE%20vs%20JVM.md)

# Memory Playground — как пощупать память JVM

Проект: `Java/playground/memory-playground/`

## Запуск

```bash
cd Java/playground/memory-playground

# меню со всеми сценариями
./gradlew run -q --console=plain

# конкретный сценарий
./gradlew run -PmainClass=playground.HeapBomb
./gradlew run -PmainClass=playground.StackBomb
./gradlew run -PmainClass=playground.MetaspaceBomb
./gradlew run -PmainClass=playground.LeakyMap
./gradlew run -PmainClass=playground.SlowAllocator
```

## JVM-флаги (в build.gradle.kts)

```
-Xmx64m -Xms64m       # маленький heap → OOM быстрее
-Xss256k              # маленький стек → SOF на меньшей глубине
-XX:MaxMetaspaceSize=64m
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=build/heap.hprof
-Xlog:gc*:file=build/gc.log:time,uptime,level,tags
```

## Сценарии

| Класс | Что делает | Ожидаемый результат |
|---|---|---|
| `HeapBomb` | Кладёт 1 МБ массивы в `ArrayList` | `OOM: Java heap space`, дамп в `build/heap.hprof` |
| `StackBomb` | Бесконечная рекурсия | `StackOverflowError` на ~глубине ≈ Xss / размер фрейма |
| `MetaspaceBomb` | Через ByteBuddy генерит классы и держит ссылки | `OOM: Metaspace` |
| `LeakyMap` | Static `HashMap`, ключи через UUID | Медленный рост Old gen → `OOM: Java heap space` |
| `SlowAllocator` | Короткоживущие объекты | Видишь GC в Young, Old почти не растёт |

## Наблюдение в VisualVM

```bash
sudo apt install visualvm   # один раз
visualvm                    # запуск
```

1. В левой панели — список запущенных Java-процессов (PID можно посмотреть в меню playground командой `p`).
2. Двойной клик по процессу → вкладки:
   - **Monitor** — графики Heap, Threads, Classes loaded.
   - **Sampler / Profiler → Memory** — топ классов, аллокации по типам.
   - **Heap Dump** (правый клик по процессу или кнопка) — снимок памяти, открывается тут же.
3. Плагины (`Tools → Plugins`):
   - **Visual GC** — отрисовка Eden/S0/S1/Old в реальном времени. Очень нагляден на `SlowAllocator` и `LeakyMap`.
   - **Threads Inspector** — детали потоков.

## Альтернативы наблюдения (CLI)

```bash
jps                                  # PID процессов
jstat -gc <pid> 1000                 # статы GC раз в секунду
jmap -histo <pid> | head -30         # топ классов по числу инстансов
jstack <pid>                         # стеки всех потоков
jcmd <pid> GC.heap_info
jcmd <pid> GC.class_histogram
jcmd <pid> JFR.start duration=30s filename=rec.jfr   # Flight Recorder, открыть в JMC
```

## Как читать heap dump

- VisualVM: вкладка **Classes** — топ классов; **Instances** — конкретные объекты, их поля и **References → GC Root** (что держит).
- **Eclipse MAT** (мощнее VisualVM):
  - **Dominator Tree** — кто реально удерживает память.
  - **Leak Suspects** — автодиагностика утечек.

## План экспериментов

1. **`SlowAllocator`** + Visual GC → увидеть, как Eden наполняется и схлопывается, объекты не доезжают до Old.
2. **`LeakyMap`** + Visual GC → Old gen медленно растёт, GC чаще, eventually OOM.
3. **`HeapBomb`** → быстрый OOM, дамп → открыть в VisualVM/MAT, найти `byte[]` как доминатор.
4. **`StackBomb`** → посмотреть `StackOverflowError`, варьируй `-Xss` и сравни глубину.
5. **`MetaspaceBomb`** → в Monitor видно, что **Metaspace** забивается, а **Heap** — нет.

## Гочи

- `./gradlew run` форкает отдельный JVM-процесс — VisualVM увидит его как `GradleWorkerMain` или по mainClass.
- При OOM Gradle Daemon тоже может упасть — норм, перезапустится.
- Если играешь часто, отключи daemon: `./gradlew run --no-daemon`.
