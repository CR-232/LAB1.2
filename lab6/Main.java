import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class Main {
    public static final int CAPACITATE_DEPOZIT = 5;
    public static final int NUMAR_TOTAL_OBIECTE = 45;
    public static final int NUMAR_PRODUCATORI = 3;
    public static final int NUMAR_CONSUMATORI = 4;

    private static GUI gui;

    // CLASA helper statică
    public static class Logger {
        public static void setGUI(GUI guiInstance) {
            gui = guiInstance;
        }

        public static void log(String message) {
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

    // CLASA Producator cu metodele sale de sincronizare
    public static class Producator extends Thread {
        private final Deque<Integer> depozit;
        private final ReentrantLock lock;
        private final Condition notFull;
        private final Condition notEmpty;
        private final Random rnd = new Random();
        private static int totalProduse = 0;
        private static boolean producatoriActive = true;
        private static boolean consumatoriActive = false;

        public Producator(Deque<Integer> depozit, ReentrantLock lock, Condition notFull, Condition notEmpty, int id) {
            super("Producator-" + id);
            this.depozit = depozit;
            this.lock = lock;
            this.notFull = notFull;
            this.notEmpty = notEmpty;
        }

        // Metoda de sincronizare pentru producție
        public void producereSincronizata() throws InterruptedException {
            lock.lock();
            try {
                // Așteaptă dacă depozitul e plin sau producătorii nu sunt activi
                while (depozit.size() == CAPACITATE_DEPOZIT || !producatoriActive) {
                    if (totalProduse >= NUMAR_TOTAL_OBIECTE) return;
                    notFull.await();
                }

                if (totalProduse >= NUMAR_TOTAL_OBIECTE) return;

                // Generează număr impar
                int valoare;
                do {
                    valoare = rnd.nextInt(1000);
                } while (valoare % 2 == 0);

                depozit.addLast(valoare);
                totalProduse++;

                Logger.log(String.format("[%s] Produs %d (depozit=%d, totalProduse=%d)\n",
                        getName(), valoare, depozit.size(), totalProduse));

                Logger.updateProgress(totalProduse);

                // Dacă am umplut depozitul, activăm consumatorii
                if (depozit.size() == CAPACITATE_DEPOZIT) {
                    producatoriActive = false;
                    consumatoriActive = true;
                    Logger.log("\n*** DEPOZIT PLIN (" + CAPACITATE_DEPOZIT + "/" + CAPACITATE_DEPOZIT + ") -> Consumatori pot consuma ***\n");
                    notEmpty.signalAll();
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void run() {
            try {
                while (totalProduse < NUMAR_TOTAL_OBIECTE) {
                    producereSincronizata();
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                Logger.log("[" + getName() + "] terminat.\n");
            }
        }
    }

    // CLASA Consumator cu metodele sale de sincronizare
    public static class Consumator extends Thread {
        private final Deque<Integer> depozit;
        private final ReentrantLock lock;
        private final Condition notFull;
        private final Condition notEmpty;
        private static int totalConsumate = 0;

        public Consumator(Deque<Integer> depozit, ReentrantLock lock, Condition notFull, Condition notEmpty, int id) {
            super("Consumator-" + id);
            this.depozit = depozit;
            this.lock = lock;
            this.notFull = notFull;
            this.notEmpty = notEmpty;
        }

        // Metoda de sincronizare pentru consum
        public void consumareSincronizata() throws InterruptedException {
            lock.lock();
            try {
                // Așteaptă dacă depozitul e gol sau consumatorii nu sunt activi
                while (depozit.isEmpty() || !Producator.consumatoriActive) {
                    if (totalConsumate >= NUMAR_TOTAL_OBIECTE) return;
                    notEmpty.await();
                }

                if (totalConsumate >= NUMAR_TOTAL_OBIECTE) return;

                int valoare = depozit.removeFirst();
                totalConsumate++;

                Logger.log(String.format("[%s] Consumat %d (depozit=%d, totalConsumate=%d)\n",
                        getName(), valoare, depozit.size(), totalConsumate));

                // Dacă am golit depozitul, activăm producătorii
                if (depozit.isEmpty()) {
                    Producator.producatoriActive = true;
                    Producator.consumatoriActive = false;
                    Logger.log("\n*** DEPOZIT GOL (0/" + CAPACITATE_DEPOZIT + ") -> Producători pot produce ***\n");
                    notFull.signalAll();
                }

                if (totalConsumate >= NUMAR_TOTAL_OBIECTE) {
                    Logger.log("\n*** S-au consumat toate cele " + NUMAR_TOTAL_OBIECTE + " obiecte ***\n");
                    // Notifică toate thread-urile că s-a terminat
                    notFull.signalAll();
                    notEmpty.signalAll();
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void run() {
            try {
                while (totalConsumate < NUMAR_TOTAL_OBIECTE) {
                    consumareSincronizata();
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                Logger.log("[" + getName() + "] terminat.\n");
            }
        }
    }

    // METODA: runSimulation
    public static void runSimulation(GUI guiInstance) throws InterruptedException {
        Logger.setGUI(guiInstance);
        Logger.updateStatus("Se inițializează simularea...");

        Logger.log("=== ÎNCEPUT SIMULARE ===\n");
        Logger.log("Obiecte totale (Z): " + NUMAR_TOTAL_OBIECTE + "\n");
        Logger.log("Capacitate depozit (D): " + CAPACITATE_DEPOZIT + "\n");
        Logger.log("Producători (X): " + NUMAR_PRODUCATORI + "\n");
        Logger.log("Consumatori (Y): " + NUMAR_CONSUMATORI + "\n");
        Logger.log("Obiecte generate: numere impare\n");
        Logger.log("Sincronizare: ReentrantLock + Condition\n\n");

        // Depozit simplu (doar o coadă)
        Deque<Integer> depozit = new ArrayDeque<>(CAPACITATE_DEPOZIT);

        // Mecanisme de sincronizare comune
        ReentrantLock lock = new ReentrantLock(true);
        Condition notFull = lock.newCondition();
        Condition notEmpty = lock.newCondition();

        // Crează și porneste producătorii
        Producator[] producatori = new Producator[NUMAR_PRODUCATORI];
        for (int i = 0; i < NUMAR_PRODUCATORI; i++) {
            producatori[i] = new Producator(depozit, lock, notFull, notEmpty, i + 1);
            producatori[i].start();
        }

        // Crează și porneste consumatorii
        Consumator[] consumatori = new Consumator[NUMAR_CONSUMATORI];
        for (int i = 0; i < NUMAR_CONSUMATORI; i++) {
            consumatori[i] = new Consumator(depozit, lock, notFull, notEmpty, i + 1);
            consumatori[i].start();
        }

        Logger.updateStatus("Simulare în derulare...");

        // Așteaptă terminarea producătorilor
        for (Producator p : producatori) {
            p.join();
        }

        // Așteaptă terminarea consumatorilor
        for (Consumator c : consumatori) {
            c.join();
        }

        Logger.log("\n=== SIMULARE TERMINATĂ ===\n");
        Logger.log("Total obiecte produse: " + Producator.totalProduse + "\n");
        Logger.log("Total obiecte consumate: " + Consumator.totalConsumate + "\n");
        Logger.log("Obiecte necesare (Z): " + NUMAR_TOTAL_OBIECTE + "\n");

        if (Consumator.totalConsumate == NUMAR_TOTAL_OBIECTE) {
            Logger.log("✓ Toate cele " + NUMAR_TOTAL_OBIECTE + " obiecte au fost produse și consumate!\n");
        } else {
            Logger.log("✗ Nu s-a atins obiectivul complet!\n");
        }

        Logger.updateStatus("Simulare completată cu succes!");
        Logger.updateProgress(NUMAR_TOTAL_OBIECTE);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}