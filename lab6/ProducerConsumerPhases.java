import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadLocalRandom;

public class ProducerConsumerPhases {

    // === Clasa Principală ===

    // Parametri configurabili
    static final int NUM_PRODUCERS = 3;
    static final int NUM_CONSUMERS = 4;
    static final int BUFFER_CAPACITY = 5;
    static final int TOTAL_ITEMS = 180; // Z = 180 obiecte totale

    // Resurse comune
    static final ArrayBlockingQueue<Integer> buffer = new ArrayBlockingQueue<>(BUFFER_CAPACITY);
    static final AtomicInteger totalProduced = new AtomicInteger(0);
    static final AtomicInteger totalConsumed = new AtomicInteger(0);

    // Phaser pentru sincronizare
    static final Phaser phaser = new Phaser(1);

    // Flag-uri pentru detectarea stărilor
    static final AtomicInteger fullAnnounced = new AtomicInteger(0);
    static final AtomicInteger emptyAnnounced = new AtomicInteger(0);

    // Thread pools
    static ExecutorService producerPool;
    static ExecutorService consumerPool;

    // Flag pentru oprire
    static volatile boolean simulationRunning = true;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== PORNIRE PROGRAM ===");
        System.out.println("Obiective: " + TOTAL_ITEMS + " obiecte totale (Z=" + TOTAL_ITEMS + ")");
        System.out.println("Capacitate buffer: " + BUFFER_CAPACITY);
        System.out.println("Producători: " + NUM_PRODUCERS);
        System.out.println("Consumatori: " + NUM_CONSUMERS);
        System.out.println();

        // Creăm thread pools
        producerPool = Executors.newFixedThreadPool(NUM_PRODUCERS);
        consumerPool = Executors.newFixedThreadPool(NUM_CONSUMERS);

        // Listă pentru Future-uri
        Future<?>[] producerFutures = new Future[NUM_PRODUCERS];
        Future<?>[] consumerFutures = new Future[NUM_CONSUMERS];

        // Lansăm producătorii prin pool
        for (int i = 0; i < NUM_PRODUCERS; i++) {
            final int id = i + 1;
            producerFutures[i] = producerPool.submit(new Producer(id));
        }

        // Lansăm consumatorii prin pool
        for (int i = 0; i < NUM_CONSUMERS; i++) {
            final int id = i + 1;
            consumerFutures[i] = consumerPool.submit(new Consumer(id));
        }

        // Control ciclurile de producție-consum
        while (totalConsumed.get() < TOTAL_ITEMS && simulationRunning) {
            int currentPhase = phaser.getPhase();

            if (currentPhase % 2 == 0) { // Faza de producție
                // Așteptă să se umple buffer-ul
                while (buffer.size() < BUFFER_CAPACITY && totalProduced.get() < TOTAL_ITEMS && simulationRunning) {
                    Thread.yield();
                }

                // Anunță dacă buffer-ul e plin
                if (buffer.size() == BUFFER_CAPACITY && fullAnnounced.get() == 0 && simulationRunning) {
                    synchronized (buffer) {
                        if (buffer.size() == BUFFER_CAPACITY && fullAnnounced.get() == 0 && simulationRunning) {
                            System.out.println("\n*** DEPOZIT PLIN (size=" + BUFFER_CAPACITY + ") -> trecem la consum ***\n");
                            fullAnnounced.set(1);
                            emptyAnnounced.set(0);
                        }
                    }
                }

                // Trece la faza de consum
                if (simulationRunning) {
                    phaser.arriveAndAwaitAdvance();
                }

            } else { // Faza de consum
                // Așteptă să se golească buffer-ul
                while (!buffer.isEmpty() && totalConsumed.get() < TOTAL_ITEMS && simulationRunning) {
                    Thread.yield();
                }

                // Anunță dacă buffer-ul e gol
                if (buffer.isEmpty() && emptyAnnounced.get() == 0 && simulationRunning) {
                    synchronized (buffer) {
                        if (buffer.isEmpty() && emptyAnnounced.get() == 0 && simulationRunning) {
                            System.out.println("\n*** DEPOZIT GOL (size=0) -> trecem la producție ***\n");
                            emptyAnnounced.set(1);
                            fullAnnounced.set(0);
                        }
                    }
                }

                // Verifică dacă s-a terminat
                if (totalConsumed.get() >= TOTAL_ITEMS || !simulationRunning) {
                    break;
                }

                // Trece la faza de producție
                if (simulationRunning) {
                    phaser.arriveAndAwaitAdvance();
                }
            }
        }

        // Termină phaser-ul
        if (phaser.getPhase() % 2 == 0) {
            phaser.arriveAndDeregister();
        } else {
            phaser.forceTermination();
        }

        // Oprim producția
        simulationRunning = false;

        // Așteaptă finalizarea task-urilor
        for (Future<?> future : producerFutures) {
            try {
                future.get(1, TimeUnit.SECONDS); // Așteaptă finalizarea
            } catch (Exception e) {
                // Ignoră timeout-uri sau excepții
            }
        }

        for (Future<?> future : consumerFutures) {
            try {
                future.get(1, TimeUnit.SECONDS); // Așteaptă finalizarea
            } catch (Exception e) {
                // Ignoră timeout-uri sau excepții
            }
        }

        // Oprim thread pools
        producerPool.shutdown();
        consumerPool.shutdown();

        try {
            producerPool.awaitTermination(1, TimeUnit.SECONDS);
            consumerPool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n=== PROGRAM TERMINAT ===");
        System.out.println("Total obiecte produse: " + totalProduced.get());
        System.out.println("Total obiecte consumate: " + totalConsumed.get());
        System.out.println("Obiecte necesare (Z): " + TOTAL_ITEMS);
    }

    // === Clasa Producer (clasă internă) ===

    static class Producer implements Runnable {
        private final int id;

        public Producer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            String name = "Producer-" + id;

            // Înregistrează la phaser
            phaser.register();

            try {
                while (totalProduced.get() < TOTAL_ITEMS && simulationRunning) {
                    int phase = phaser.getPhase();

                    // Lucrează doar în fazele pare (producție)
                    if (phase % 2 == 0) {
                        // Verifică dacă poate produce
                        if (buffer.size() < BUFFER_CAPACITY && totalProduced.get() < TOTAL_ITEMS && simulationRunning) {
                            int product = ThreadLocalRandom.current().nextInt(1000);
                            boolean offered = buffer.offer(product);

                            if (offered) {
                                int produced = totalProduced.incrementAndGet();
                                System.out.printf("[%s] Produced %d (buffer size=%d, totalProduced=%d)\n",
                                        name, product, buffer.size(), produced);

                                // Dacă buffer-ul a devenit plin, așteaptă
                                if (buffer.size() == BUFFER_CAPACITY && simulationRunning) {
                                    phaser.arriveAndAwaitAdvance();
                                }
                            }

                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                break;
                            }

                        } else if (buffer.size() == BUFFER_CAPACITY && simulationRunning) {
                            // Buffer-ul e deja plin, așteaptă
                            phaser.arriveAndAwaitAdvance();
                        }
                    } else {
                        // Nu e fază de producție, așteaptă
                        if (simulationRunning) {
                            phaser.arriveAndAwaitAdvance();
                        }
                    }
                }
            } catch (Exception e) {
                // Thread interrupted or phaser terminated
            } finally {
                // Deregistrează
                try {
                    phaser.arriveAndDeregister();
                } catch (Exception e) {
                    // Phaser already terminated
                }
                System.out.println("[" + name + "] ieșire.");
            }
        }
    }

    // === Clasa Consumer (clasă internă) ===

    static class Consumer implements Runnable {
        private final int id;

        public Consumer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            String name = "Consumer-" + id;

            // Înregistrează la phaser
            phaser.register();

            try {
                while (totalConsumed.get() < TOTAL_ITEMS && simulationRunning) {
                    int phase = phaser.getPhase();

                    // Lucrează doar în fazele impare (consum)
                    if (phase % 2 == 1) {
                        // Verifică dacă poate consuma
                        if (!buffer.isEmpty() && totalConsumed.get() < TOTAL_ITEMS && simulationRunning) {
                            Integer item = buffer.poll();
                            if (item != null) {
                                int consumed = totalConsumed.incrementAndGet();
                                System.out.printf("[%s] Consumed %d (buffer size=%d, totalConsumed=%d)\n",
                                        name, item, buffer.size(), consumed);

                                // Dacă buffer-ul a devenit gol, așteaptă
                                if (buffer.isEmpty() && simulationRunning) {
                                    phaser.arriveAndAwaitAdvance();
                                }
                            }

                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                break;
                            }

                        } else if (buffer.isEmpty() && simulationRunning) {
                            // Buffer-ul e deja gol, așteaptă
                            phaser.arriveAndAwaitAdvance();
                        }
                    } else {
                        // Nu e fază de consum, așteaptă
                        if (simulationRunning) {
                            phaser.arriveAndAwaitAdvance();
                        }
                    }
                }
            } catch (Exception e) {
                // Thread interrupted or phaser terminated
            } finally {
                // Deregistrează
                try {
                    phaser.arriveAndDeregister();
                } catch (Exception e) {
                    // Phaser already terminated
                }
                System.out.println("[" + name + "] ieșire.");
            }
        }
    }
}