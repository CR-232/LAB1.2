import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static final int BUFFER_CAPACITY = 5;
    public static final int TOTAL_ITEMS = 180;
    public static final int NUM_PRODUCERS = 3;
    public static final int NUM_CONSUMERS = 4;

    private static GUI gui;

    public static class Logger {
        public static void setGUI(GUI guiInstance) {
            gui = guiInstance;
        }

        public static synchronized void log(String message) {
            if (gui != null) {
                gui.appendToConsole(message);
            } else {
                System.out.print(message);
            }
        }

        public static void updateProgress(int value) {
            if (gui != null) {
                gui.updateProgress(value);
            }
        }

        public static void updateStatus(String status) {
            if (gui != null) {
                gui.updateStatus(status);
            }
        }
    }

    public static class Depozit {
        final Deque<Integer> buffer = new ArrayDeque<>(BUFFER_CAPACITY);
        final Random rnd = new Random();
        final ReentrantLock lock = new ReentrantLock(true);

        int totalProduced = 0;
        int totalConsumed = 0;
        volatile boolean done = false;
        boolean fullAnnounced = false;
        boolean emptyAnnounced = false;

        public boolean tryProduce(String name) {
            lock.lock();
            try {
                if (done) return false;
                if (totalProduced >= TOTAL_ITEMS) return false;
                if (buffer.size() == BUFFER_CAPACITY) return false;

                int value = rnd.nextInt(1000);
                buffer.addLast(value);
                totalProduced++;

                Logger.log(String.format("[%s] Produced %d (buffer size=%d, totalProduced=%d)\n",
                        name, value, buffer.size(), totalProduced));

                Logger.updateProgress(totalProduced);

                if (buffer.size() == 1) {
                    emptyAnnounced = false;
                }

                return true;
            } finally {
                lock.unlock();
            }
        }

        public boolean tryConsume(String name) {
            lock.lock();
            try {
                if (done) return false;
                if (buffer.isEmpty()) return false;
                if (totalConsumed >= TOTAL_ITEMS) {
                    done = true;
                    return false;
                }

                int value = buffer.removeFirst();
                totalConsumed++;

                Logger.log(String.format("[%s] Consumed %d (buffer size=%d, totalConsumed=%d)\n",
                        name, value, buffer.size(), totalConsumed));

                if (buffer.size() == BUFFER_CAPACITY - 1) {
                    fullAnnounced = false;
                }

                if (totalConsumed >= TOTAL_ITEMS) {
                    done = true;
                    Logger.log("\n*** S-au consumat toate cele " + TOTAL_ITEMS + " obiecte ***\n");
                }

                return true;
            } finally {
                lock.unlock();
            }
        }

        public void checkAndAnnounceFull() {
            lock.lock();
            try {
                if (done) return;
                if (buffer.size() == BUFFER_CAPACITY && !fullAnnounced) {
                    Logger.log("\n*** DEPOZIT PLIN (size=" + BUFFER_CAPACITY + ") -> trecem la consum ***\n\n");
                    fullAnnounced = true;
                }
            } finally {
                lock.unlock();
            }
        }

        public void checkAndAnnounceEmpty() {
            lock.lock();
            try {
                if (done) return;
                if (buffer.isEmpty() && !emptyAnnounced) {
                    Logger.log("\n*** DEPOZIT GOL (size=0) -> trecem la producție ***\n\n");
                    emptyAnnounced = true;
                }
            } finally {
                lock.unlock();
            }
        }

        public boolean isFull() {
            lock.lock();
            try {
                return buffer.size() == BUFFER_CAPACITY;
            } finally {
                lock.unlock();
            }
        }

        public boolean isEmpty() {
            lock.lock();
            try {
                return buffer.isEmpty();
            } finally {
                lock.unlock();
            }
        }
    }

    public static class Producator extends Thread {
        private final Depozit depozit;
        private final Phaser phaser;
        private final int id;

        public Producator(Depozit depozit, Phaser phaser, int id) {
            super("Producer-" + id);
            this.depozit = depozit;
            this.phaser = phaser;
            this.id = id;
        }

        @Override
        public void run() {
            phaser.register();

            try {
                while (!depozit.done) {
                    int phase = phaser.getPhase();

                    if (phase % 2 == 0) {
                        while (!depozit.done && depozit.totalProduced < TOTAL_ITEMS) {
                            if (depozit.isFull()) break;

                            boolean produced = depozit.tryProduce(getName());
                            if (!produced) break;

                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }

                        depozit.checkAndAnnounceFull();
                    }

                    phaser.arriveAndAwaitAdvance();
                }
            } catch (Exception e) {
            } finally {
                phaser.arriveAndDeregister();
                Logger.log("[" + getName() + "] ieșire.\n");
            }
        }
    }

    public static class Consumator extends Thread {
        private final Depozit depozit;
        private final Phaser phaser;
        private final int id;

        public Consumator(Depozit depozit, Phaser phaser, int id) {
            super("Consumer-" + id);
            this.depozit = depozit;
            this.phaser = phaser;
            this.id = id;
        }

        @Override
        public void run() {
            phaser.register();

            try {
                while (!depozit.done) {
                    int phase = phaser.getPhase();

                    if (phase % 2 == 1) {
                        while (!depozit.done && depozit.totalConsumed < TOTAL_ITEMS) {
                            if (depozit.isEmpty()) break;

                            boolean consumed = depozit.tryConsume(getName());
                            if (!consumed) break;

                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }

                        depozit.checkAndAnnounceEmpty();
                    }

                    phaser.arriveAndAwaitAdvance();
                }
            } catch (Exception e) {
            } finally {
                phaser.arriveAndDeregister();
                Logger.log("[" + getName() + "] ieșire.\n");
            }
        }
    }

    public static void runSimulation(GUI guiInstance) throws InterruptedException {
        Logger.setGUI(guiInstance);
        Logger.updateStatus("Initializing simulation...");

        Logger.log("=== PORNIRE PROGRAM ===\n");
        Logger.log("Obiective: " + TOTAL_ITEMS + " obiecte totale (Z=" + TOTAL_ITEMS + ")\n");
        Logger.log("Capacitate buffer: " + BUFFER_CAPACITY + "\n");
        Logger.log("Producători: " + NUM_PRODUCERS + "\n");
        Logger.log("Consumatori: " + NUM_CONSUMERS + "\n");
        Logger.log("Sincronizare: ReentrantLock + Phaser\n\n");

        Depozit depozit = new Depozit();
        Phaser phaser = new Phaser(1);

        Thread[] producers = new Thread[NUM_PRODUCERS];
        for (int i = 0; i < NUM_PRODUCERS; i++) {
            producers[i] = new Producator(depozit, phaser, i + 1);
            producers[i].start();
        }

        Thread[] consumers = new Thread[NUM_CONSUMERS];
        for (int i = 0; i < NUM_CONSUMERS; i++) {
            consumers[i] = new Consumator(depozit, phaser, i + 1);
            consumers[i].start();
        }

        Logger.updateStatus("Simulation running...");

        try {
            while (!depozit.done && depozit.totalConsumed < TOTAL_ITEMS) {
                phaser.arriveAndAwaitAdvance();
                Thread.sleep(50);

                if (depozit.totalConsumed >= TOTAL_ITEMS && depozit.buffer.isEmpty()) {
                    depozit.done = true;
                }
            }
        } finally {
            phaser.forceTermination();
            Thread.sleep(100);
        }

        for (Thread p : producers) {
            p.join(100);
            if (p.isAlive()) p.interrupt();
        }
        for (Thread c : consumers) {
            c.join(100);
            if (c.isAlive()) c.interrupt();
        }

        Logger.log("\n=== PROGRAM TERMINAT ===\n");
        Logger.log("Total obiecte produse: " + depozit.totalProduced + "\n");
        Logger.log("Total obiecte consumate: " + depozit.totalConsumed + "\n");
        Logger.log("Obiecte necesare (Z): " + TOTAL_ITEMS + "\n");

        if (depozit.totalConsumed == TOTAL_ITEMS) {
            Logger.log("✓ Toate obiectele au fost produse și consumate cu succes!\n");
        } else {
            Logger.log("✗ Programul nu a atins obiectivul complet!\n");
        }

        Logger.updateStatus("Simulation completed successfully!");
        Logger.updateProgress(TOTAL_ITEMS);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}