package playground;

import java.util.Scanner;

public class Menu {
    public static void main(String[] args) throws Exception {
        printMenu();
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!sc.hasNextLine()) return;
                String cmd = sc.nextLine().trim();
                switch (cmd) {
                    case "1" -> HeapBomb.run();
                    case "2" -> StackBomb.run();
                    case "3" -> MetaspaceBomb.run();
                    case "4" -> LeakyMap.run();
                    case "5" -> SlowAllocator.run();
                    case "p" -> printPid();
                    case "h", "?" -> printMenu();
                    case "q", "exit" -> { return; }
                    default -> System.out.println("неизвестная команда (h — справка)");
                }
            }
        }
    }

    private static void printMenu() {
        System.out.println("""
            Memory Playground
              1  HeapBomb       — наполнить heap до OutOfMemoryError
              2  StackBomb      — рекурсия до StackOverflowError
              3  MetaspaceBomb  — генерация классов до Metaspace OOM
              4  LeakyMap       — медленная утечка через static Map
              5  SlowAllocator  — равномерная аллокация (наблюдать GC)
              p  показать PID процесса
              h  справка
              q  выход
            """);
    }

    private static void printPid() {
        System.out.println("PID = " + ProcessHandle.current().pid());
    }
}
