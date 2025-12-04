import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final int D = 5;
    private static final int Y = 4;
    private static final int Z = 2;
    private static final int TOTAL_NEEDED = Y * Z;

    private static final AtomicInteger totalProduced = new AtomicInteger(0);
    private static final AtomicInteger totalConsumed = new AtomicInteger(0);
    private static volatile boolean productionComplete = false;

    private static final List<Integer> buffer = new ArrayList<>(D);
    private static final Object lock = new Object();

    public static void main(String[] args) {
        ProducerConsumerGUI gui = new ProducerConsumerGUI();

        gui.log("=== LABORATOR NR.5 ===");
        gui.log("Capacitate depozit (D): " + D);
        gui.log("Numar consumatori (Y): " + Y);
        gui.log("Produse/consumator (Z): " + Z);
        gui.log("Total necesar: " + TOTAL_NEEDED);
        gui.log("Producatori: 3");
        gui.log("Fiecare producator pune 2 numere ODATA");
        gui.log("Sincronizare distribuită în fiecare clasă");
        gui.log("=================================");

        ExecutorService executor = Executors.newFixedThreadPool(7);

        for (int i = 1; i <= 3; i++) {
            executor.execute(new Producer(i, gui));
        }

        for (int i = 1; i <= Y; i++) {
            executor.execute(new Consumer(i, gui));
        }

        executor.shutdown();

        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        gui.log("\n========== RAPORT FINAL ==========");
        gui.log("Total produse generate: " + totalProduced.get());
        gui.log("Total produse consumate: " + totalConsumed.get());
        gui.log("Produse ramase in buffer: " + buffer.size());
        gui.log("==================================");
    }

    // CLASA PRODUCER - Vadim
    static class Producer implements Runnable {
        private final int id;
        private final ProducerConsumerGUI gui;
        private final Random random = new Random();

        Producer(int id, ProducerConsumerGUI gui) {
            this.id = id;
            this.gui = gui;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (checkProductionComplete()) {
                        break;
                    }

                    int nr1 = generateOddNumber();
                    int nr2 = generateOddNumber();

                    gui.log("Producator " + id + " a generat: " + nr1 + " si " + nr2);

                    produceBothNumbers(nr1, nr2);

                    Thread.sleep(random.nextInt(300) + 100);
                }

                gui.log("Producator " + id + " a terminat!");
            } catch (InterruptedException e) {
                gui.log("Producator " + id + " a fost intrerupt");
            }
        }

        private boolean checkProductionComplete() {
            synchronized (lock) {
                if (totalProduced.get() >= TOTAL_NEEDED) {
                    productionComplete = true;
                    lock.notifyAll();
                    return true;
                }
                return false;
            }
        }

        private void produceBothNumbers(int nr1, int nr2) throws InterruptedException {
            synchronized (lock) {
                while (buffer.size() > D - 2) {
                    gui.log("Producator " + id + " asteapta - nu are loc pentru 2 numere (" +
                            buffer.size() + "/" + D + ")");
                    lock.wait();
                }

                buffer.add(nr1);
                buffer.add(nr2);

                totalProduced.addAndGet(2);

                gui.log("Producator " + id + " a adaugat ODATA: " + nr1 + " si " + nr2 +
                        " | Total: " + totalProduced.get() + "/" + TOTAL_NEEDED +
                        " | Buffer: " + buffer.size() + "/" + D);

                lock.notifyAll();
            }
        }

        private int generateOddNumber() {
            int nr = random.nextInt(50) + 1;
            return (nr % 2 == 0) ? nr + 1 : nr;
        }
    }

    // CLASA CONSUMER - Maxim
    static class Consumer implements Runnable {
        private final int id;
        private final ProducerConsumerGUI gui;
        private final Random random = new Random();
        private int consumedCount = 0;

        Consumer(int id, ProducerConsumerGUI gui) {
            this.id = id;
            this.gui = gui;
        }

        @Override
        public void run() {
            try {
                while (consumedCount < Z) {
                    int item = consumeNumber();

                    if (item == -1) {
                        gui.log("Consumator " + id + " - productie completa");
                        break;
                    }

                    if (item != -2) {
                        consumedCount++;
                        totalConsumed.incrementAndGet();

                        gui.log("Consumator " + id + " a consumat: " + item +
                                " (" + consumedCount + "/" + Z + ")");

                        if (consumedCount >= Z) {
                            gui.log("Consumator " + id + " a fost indestulat!");
                            break;
                        }
                    }

                    Thread.sleep(random.nextInt(300) + 100);
                }

                gui.log("Consumator " + id + " terminat cu " +
                        consumedCount + " produse");
            } catch (InterruptedException e) {
                gui.log("Consumator " + id + " a fost intrerupt");
            }
        }

        private int consumeNumber() throws InterruptedException {
            synchronized (lock) {
                while (buffer.isEmpty()) {
                    if (productionComplete) {
                        return -1;
                    }
                    gui.log("Consumator " + id + " asteapta - buffer gol");
                    lock.wait();
                }

                int item = buffer.remove(0);
                gui.log("Consumator " + id + " a luat: " + item +
                        " | Buffer: " + buffer.size() + "/" + D);

                lock.notifyAll();

                return item;
            }
        }
    }
}