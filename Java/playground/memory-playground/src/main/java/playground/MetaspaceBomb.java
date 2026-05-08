package playground;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import java.util.ArrayList;
import java.util.List;

public class MetaspaceBomb {
    public static void main(String[] args) { run(); }

    static void run() {
        System.out.println("MetaspaceBomb: генерирую классы динамически...");
        List<Class<?>> generated = new ArrayList<>(); // держим ссылки, чтобы классы не выгружались
        long n = 0;
        try {
            while (true) {
                Class<?> cls = new ByteBuddy()
                        .subclass(Object.class)
                        .name("playground.Generated$" + n)
                        .make()
                        .load(MetaspaceBomb.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                        .getLoaded();
                generated.add(cls);
                n++;
                if (n % 1000 == 0) {
                    System.out.println("создано классов: " + n);
                }
            }
        } catch (OutOfMemoryError e) {
            System.out.println("\nOOM: " + e.getMessage());
            System.out.println("успели создать классов: " + n);
        }
    }
}
