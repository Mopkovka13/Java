package playground;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LeakyMap {
    public static void main(String[] args) { run(); }

    // static = поле объекта Class<LeakyMap>, который живёт в Heap
    // ссылка из GC roots → ничего не освобождается
    private static final Map<String, byte[]> CACHE = new HashMap<>();

    static void run() {
        System.out.println("LeakyMap: медленная утечка. Открой VisualVM, посмотри на Old gen / Heap.");
        long iter = 0;
        try {
            while (true) {
                CACHE.put(UUID.randomUUID().toString(), new byte[10 * 1024]); // 10 KB
                iter++;
                if (iter % 1000 == 0) {
                    System.out.println("записей в кэше: " + CACHE.size());
                    Thread.sleep(100); // не убиваем CPU, чтобы успел понаблюдать
                }
            }
        } catch (OutOfMemoryError e) {
            System.out.println("\nOOM: " + e.getMessage());
            System.out.println("в кэше: " + CACHE.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
