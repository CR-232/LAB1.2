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


    // CLASA helper statică - gestionează toate mesajele și actualizările pentru GUI și consolă
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


    //CLASA Depozit
    public static class Depozit {
        final Deque<Integer> buffer = new ArrayDeque<>(CAPACITATE_DEPOZIT);
        final Random rnd = new Random();
        final ReentrantLock lock = new ReentrantLock(true);
        final Condition notFull = lock.newCondition();
        final Condition notEmpty = lock.newCondition();

        int totalProduse = 0;
        int totalConsumate = 0;
        boolean producatoriActive = true;
        boolean consumatoriActive = false;

        public void produce(String nume) throws InterruptedException {
            lock.lock();
            try {
                // Așteaptă dacă depozitul e plin sau producătorii nu sunt activi
                while (buffer.size() == CAPACITATE_DEPOZIT || !producatoriActive) {
                    if (totalProduse >= NUMAR_TOTAL_OBIECTE) return;
                    if (buffer.size() == CAPACITATE_DEPOZIT) {
                        producatoriActive = false;
                        consumatoriActive = true;
                        Logger.log("\n*** DEPOZIT PLIN (" + CAPACITATE_DEPOZIT + "/" + CAPACITATE_DEPOZIT + ") -> Consumatori pot consuma ***\n");
                        notEmpty.signalAll();
                    }
                    notFull.await();
                }

                if (totalProduse >= NUMAR_TOTAL_OBIECTE) return;

                int valoare;
                do {
                    valoare = rnd.nextInt(1000);
                } while (valoare % 2 == 0);

                buffer.addLast(valoare);
                totalProduse++;

                Logger.log(String.format("[%s] Produs %d (depozit=%d, totalProduse=%d)\n",
                        nume, valoare, buffer.size(), totalProduse));

                Logger.updateProgress(totalProduse);

                // Dacă am umplut depozitul, activăm consumatorii
                if (buffer.size() == CAPACITATE_DEPOZIT) {
                    producatoriActive = false;
                    consumatoriActive = true;
                    Logger.log("\n*** DEPOZIT PLIN (" + CAPACITATE_DEPOZIT + "/" + CAPACITATE_DEPOZIT + ") -> Consumatori pot consuma ***\n");
                    notEmpty.signalAll();
                }

            } finally {
                lock.unlock();
            }
        }

        public void consuma(String nume) throws InterruptedException {
            lock.lock();
            try {
                // Așteaptă dacă depozitul e gol sau consumatorii nu sunt activi
                while (buffer.isEmpty() || !consumatoriActive) {
                    if (totalConsumate >= NUMAR_TOTAL_OBIECTE) return;
                    if (buffer.isEmpty() && consumatoriActive) {
                        consumatoriActive = false;
                        producatoriActive = true;
                        Logger.log("\n*** DEPOZIT GOL (0/" + CAPACITATE_DEPOZIT + ") -> Producători pot produce ***\n");
                        notFull.signalAll();
                    }
                    notEmpty.await();
                }

                if (buffer.isEmpty() || totalConsumate >= NUMAR_TOTAL_OBIECTE) return;

                int valoare = buffer.removeFirst();
                totalConsumate++;

                Logger.log(String.format("[%s] Consumat %d (depozit=%d, totalConsumate=%d)\n",
                        nume, valoare, buffer.size(), totalConsumate));

                // Dacă am golit depozitul, activăm producătorii
                if (buffer.isEmpty()) {
                    consumatoriActive = false;
                    producatoriActive = true;
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

        public boolean esteTerminat() {
            lock.lock();
            try {
                return totalConsumate >= NUMAR_TOTAL_OBIECTE;
            } finally {
                lock.unlock();
            }
        }
    }


    //CLASA Producator - Vadim
    public static class Producator extends Thread {
        private final Depozit depozit;
        private final int id;

        public Producator(Depozit depozit, int id) {
            super("Producator-" + id);
            this.depozit = depozit;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                while (!depozit.esteTerminat()) {
                    depozit.produce(getName());
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                Logger.log("[" + getName() + "] terminat.\n");
            }
        }
    }


    //CLASA Consumator - Maxim
    public static class Consumator extends Thread {
        private final Depozit depozit;
        private final int id;

        public Consumator(Depozit depozit, int id) {
            super("Consumator-" + id);
            this.depozit = depozit;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                while (!depozit.esteTerminat()) {
                    depozit.consuma(getName());
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                Logger.log("[" + getName() + "] terminat.\n");
            }
        }
    }


    // METODA: runSimulation - coordonează și rulează întreaga simulare producător-consumator
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

        Depozit depozit = new Depozit();

        Thread[] producatori = new Thread[NUMAR_PRODUCATORI];
        for (int i = 0; i < NUMAR_PRODUCATORI; i++) {
            producatori[i] = new Producator(depozit, i + 1);
            producatori[i].start();
        }

        Thread[] consumatori = new Thread[NUMAR_CONSUMATORI];
        for (int i = 0; i < NUMAR_CONSUMATORI; i++) {
            consumatori[i] = new Consumator(depozit, i + 1);
            consumatori[i].start();
        }

        Logger.updateStatus("Simulare în derulare...");

        for (Thread p : producatori) {
            p.join();
        }
        for (Thread c : consumatori) {
            c.join();
        }

        Logger.log("\n=== SIMULARE TERMINATĂ ===\n");
        Logger.log("Total obiecte produse: " + depozit.totalProduse + "\n");
        Logger.log("Total obiecte consumate: " + depozit.totalConsumate + "\n");
        Logger.log("Obiecte necesare (Z): " + NUMAR_TOTAL_OBIECTE + "\n");

        if (depozit.totalConsumate == NUMAR_TOTAL_OBIECTE) {
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