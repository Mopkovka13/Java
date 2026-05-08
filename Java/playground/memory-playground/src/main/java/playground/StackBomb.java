package playground;

public class StackBomb {
    public static void main(String[] args) { run(); }

    static void run() {
        System.out.println("StackBomb: запускаю рекурсию...");
        try {
            recurse(0);
        } catch (StackOverflowError e) {
            // глубину получим из счётчика, ловим только для красивого вывода
        }
    }

    private static int counter = 0;

    private static void recurse(int depth) {
        counter = depth;
        try {
            recurse(depth + 1);
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError на глубине " + counter);
            throw e; // пробрасываем дальше — пусть всплывёт
        }
    }
}
