package playground;

import java.util.ArrayList;
import java.util.List;

public class HeapBomb {
    public static void main(String[] args) { run(); }

    static void run() {
        System.out.println("HeapBomb: накапливаю байтовые массивы...");
        List<byte[]> chunks = new ArrayList<>();
        long total = 0;
        try {
            while (true) {
                chunks.add(new byte[1024 * 1024]); // 1 MB за итерацию
                total += 1;
                if (total % 5 == 0) {
                    System.out.println("выделено ~" + total + " MB, объектов: " + chunks.size());
                }
            }
        } catch (OutOfMemoryError e) {
            System.out.println("\nOOM: " + e.getMessage());
            System.out.println("успели выделить ~" + total + " MB");
        }
    }
}
