import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.concurrent.Phaser;

public class ProducerConsumerSimulation {

    // Parametri configurabili
    public static final int BUFFER_CAPACITY = 5;
    public static final int TOTAL_ITEMS = 180;
    public static final int NUM_PRODUCERS = 3;
    public static final int NUM_CONSUMERS = 4;

    // Referință către GUI
    private static SimpleSimulationGUI gui;

    // Logger pentru afișare în GUI și scriere în fișier
    public static class Logger {
        private static PrintWriter fileWriter;

        static {
            try {
                fileWriter = new PrintWriter(new FileWriter("producer_consumer_log.txt"));
            } catch (Exception e) {
                e.printStackTrace();
                fileWriter = null;
            }
        }

        public static void setGUI(SimpleSimulationGUI guiInstance) {
            gui = guiInstance;
        }

        public static synchronized void log(String message) {
            // Afișează în GUI
            if (gui != null) {
                gui.appendToConsole(message);
            } else {
                // Dacă nu există GUI, afișează în consolă
                System.out.print(message);
            }

            // Scrie în fișier (dar nu afișăm în GUI)
            if (fileWriter != null) {
                fileWriter.print(message);
                fileWriter.flush();
            }
        }

        public static synchronized void close() {
            if (fileWriter != null) {
                fileWriter.close();
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

    // Clasa Depot
    public static class Depot {
        final Deque<Integer> buffer = new ArrayDeque<>(BUFFER_CAPACITY);
        final Random rnd = new Random();

        int totalProduced = 0;
        int totalConsumed = 0;
        volatile boolean done = false;
        boolean fullAnnounced = false;
        boolean emptyAnnounced = false;

        public boolean tryProduce(String name) {
            synchronized (this) {
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
            }
        }

        public boolean tryConsume(String name) {
            synchronized (this) {
                if (buffer.isEmpty()) return false;

                int value = buffer.removeFirst();
                totalConsumed++;

                Logger.log(String.format("[%s] Consumed %d (buffer size=%d, totalConsumed=%d)\n",
                        name, value, buffer.size(), totalConsumed));

                if (buffer.size() == BUFFER_CAPACITY - 1) {
                    fullAnnounced = false;
                }

                if (totalConsumed >= TOTAL_ITEMS && buffer.isEmpty()) {
                    done = true;
                }

                return true;
            }
        }

        public void checkAndAnnounceFull() {
            synchronized (this) {
                if (buffer.size() == BUFFER_CAPACITY && !fullAnnounced) {
                    Logger.log("\n*** DEPOZIT PLIN (size=" + BUFFER_CAPACITY + ") -> trecem la consum ***\n\n");
                    fullAnnounced = true;
                }
            }
        }

        public void checkAndAnnounceEmpty() {
            synchronized (this) {
                if (buffer.isEmpty() && !emptyAnnounced) {
                    Logger.log("\n*** DEPOZIT GOL (size=0) -> trecem la producție ***\n\n");
                    emptyAnnounced = true;
                }
            }
        }

        public boolean isFull() {
            synchronized (this) {
                return buffer.size() == BUFFER_CAPACITY;
            }
        }

        public boolean isEmpty() {
            synchronized (this) {
                return buffer.isEmpty();
            }
        }
    }

    // Clasa Producer
    public static class Producer extends Thread {
        private final Depot depot;
        private final Phaser phaser;
        private final int id;

        public Producer(Depot depot, Phaser phaser, int id) {
            super("Producer-" + id);
            this.depot = depot;
            this.phaser = phaser;
            this.id = id;
        }

        @Override
        public void run() {
            phaser.register();

            try {
                while (!depot.done && depot.totalProduced < TOTAL_ITEMS) {
                    int phase = phaser.getPhase();

                    if (phase % 2 == 0) {
                        while (!depot.done &&
                                depot.totalProduced < TOTAL_ITEMS &&
                                depot.buffer.size() < BUFFER_CAPACITY) {

                            boolean produced = depot.tryProduce(getName());
                            if (!produced) break;

                            try { Thread.sleep(20); } catch (InterruptedException e) { break; }
                        }

                        depot.checkAndAnnounceFull();

                    }

                    phaser.arriveAndAwaitAdvance();
                }
            } catch (Exception e) {
                // Phaser terminat
            } finally {
                phaser.arriveAndDeregister();
                Logger.log("[" + getName() + "] ieșire.\n");
            }
        }
    }

    // Clasa Consumer
    public static class Consumer extends Thread {
        private final Depot depot;
        private final Phaser phaser;
        private final int id;

        public Consumer(Depot depot, Phaser phaser, int id) {
            super("Consumer-" + id);
            this.depot = depot;
            this.phaser = phaser;
            this.id = id;
        }

        @Override
        public void run() {
            phaser.register();

            try {
                while (!depot.done && depot.totalConsumed < TOTAL_ITEMS) {
                    int phase = phaser.getPhase();

                    if (phase % 2 == 1) {
                        while (!depot.done &&
                                depot.totalConsumed < TOTAL_ITEMS &&
                                !depot.buffer.isEmpty()) {

                            boolean consumed = depot.tryConsume(getName());
                            if (!consumed) break;

                            try { Thread.sleep(20); } catch (InterruptedException e) { break; }
                        }

                        depot.checkAndAnnounceEmpty();

                    }

                    phaser.arriveAndAwaitAdvance();
                }
            } catch (Exception e) {
                // Phaser terminat
            } finally {
                phaser.arriveAndDeregister();
                Logger.log("[" + getName() + "] ieșire.\n");
            }
        }
    }

    // Metoda principală de rulare a simulării
    public static void runSimulation(SimpleSimulationGUI guiInstance) throws InterruptedException {
        // Configurează logger-ul cu GUI
        Logger.setGUI(guiInstance);

        // Actualizează status-ul
        Logger.updateStatus("Initializing simulation...");

        // Afișează informațiile inițiale
        Logger.log("=== PORNIRE PROGRAM ===\n");
        Logger.log("Obiective: " + TOTAL_ITEMS + " obiecte totale (Z=" + TOTAL_ITEMS + ")\n");
        Logger.log("Capacitate buffer: " + BUFFER_CAPACITY + "\n");
        Logger.log("Producători: " + NUM_PRODUCERS + "\n");
        Logger.log("Consumatori: " + NUM_CONSUMERS + "\n\n");

        // Creăm depozitul
        Depot depot = new Depot();

        // Creăm phaser-ul
        Phaser phaser = new Phaser(1);

        // Creăm și pornim producătorii
        Thread[] producers = new Thread[NUM_PRODUCERS];
        for (int i = 0; i < NUM_PRODUCERS; i++) {
            producers[i] = new Producer(depot, phaser, i + 1);
            producers[i].start();
        }

        // Creăm și pornim consumatorii
        Thread[] consumers = new Thread[NUM_CONSUMERS];
        for (int i = 0; i < NUM_CONSUMERS; i++) {
            consumers[i] = new Consumer(depot, phaser, i + 1);
            consumers[i].start();
        }

        // Actualizează status-ul
        Logger.updateStatus("Simulation running...");

        // Thread-ul principal controlează fazele
        try {
            while (!depot.done && depot.totalConsumed < TOTAL_ITEMS) {
                phaser.arriveAndAwaitAdvance();
                Thread.sleep(50);
            }
        } finally {
            phaser.forceTermination();
        }

        // Așteaptă terminarea thread-urilor
        for (Thread p : producers) p.join();
        for (Thread c : consumers) c.join();

        // Rezultate finale
        Logger.log("\n=== PROGRAM TERMINAT ===\n");
        Logger.log("Total obiecte produse: " + depot.totalProduced + "\n");
        Logger.log("Total obiecte consumate: " + depot.totalConsumed + "\n");
        Logger.log("Obiecte necesare (Z): " + TOTAL_ITEMS + "\n");

        // Închide logger-ul
        Logger.close();

        // Actualizează status-ul final
        Logger.updateStatus("Simulation completed successfully!");
        Logger.updateProgress(TOTAL_ITEMS);

        // Mesaj suplimentar
        Logger.log("\nRezultatele au fost salvate în fișierul: producer_consumer_log.txt\n");
    }
}