package playground;

import java.util.Random;

public class SlowAllocator {
    public static void main(String[] args) { run(); }

    static void run() {
        System.out.println("SlowAllocator: создаю короткоживущие объекты — наблюдай GC в Young.");
        Random r = new Random();
        long count = 0;
        long sink = 0;
        try {
            while (true) {
                // короткоживущие объекты — большинство умрёт в Eden, до Old gen не доедут
                byte[] b = new byte[r.nextInt(50_000)];
                sink += b.length; // не даём JIT оптимизировать через escape analysis
                count++;
                if (count % 100_000 == 0) {
                    System.out.println("аллокаций: " + count + ", sink=" + sink);
                    Thread.sleep(50);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
