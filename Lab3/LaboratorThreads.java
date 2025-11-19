import javax.swing.SwingUtilities;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LaboratorThreads {

    private static final String NUME_STUDENT = "Cruc,Cotoman";
    private static final String PRENUME_STUDENT = "Maxim,Vadim";
    private static final String GRUPA = "CR-232";
    private static final String DISCIPLINA = "Programarea concurentă și distribuită";

    private static int[] mas;
    private static Lab3GUI gui;
    private static final Object lock = new Object();

    private static volatile int finishedThreads = 0;
    private static volatile int currentDisplay = 0;

    private static final String[] DISPLAY_ORDER = {
            "Th2", "Th4", "Th1", "Th3"
    };

    private static BlockingQueue<String> guiDisplayQueue = new LinkedBlockingQueue<>();
    private static volatile boolean displayThreadRunning = true;
    private static Thread displayThread;

    static {
        displayThread = new Thread(() -> {
            while (displayThreadRunning || !guiDisplayQueue.isEmpty()) {
                try {
                    String text = guiDisplayQueue.take();
                    if (text.equals("STOP")) break;

                    SwingUtilities.invokeLater(() -> gui.appendText(text + "\n"));
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        displayThread.start();
    }

    public static void main(String[] args) throws InterruptedException {
        // Initialize GUI
        gui = new Lab3GUI("Laborator PCD - CR_232");

        System.out.println("=== LABORATOR CR-232 ===");
        System.out.println("Echipa: Cruc Maxim Cotoman si Cotoman Vadim");
        System.out.println();

        mas = new int[100];
        gui.appendText("Tablou generat (10 linii x 10 coloane):\n");
        Random rand = new Random();

        for (int i = 0; i < mas.length; i++) {
            mas[i] = rand.nextInt(1448) + 120;

            if (mas[i] < 1000) {
                gui.appendText(" " + mas[i] + " ");
            } else {
                gui.appendText(mas[i] + " ");
            }

            if ((i + 1) % 10 == 0) {
                gui.appendText("\n");
            }
        }
        gui.appendText("\n");

        System.out.println("Array generat:");
        printArray(mas);
        System.out.println();

        System.out.println("Starting Thread 1");
        System.out.println("Starting Thread 2");
        System.out.println("Starting Thread 3");
        System.out.println("Starting Thread 4");
        System.out.println();

        ThreadCalc th1 = new ThreadCalc(0, 49, mas, "Th1", gui);
        ThreadCalc th2 = new ThreadCalc(50, 99, mas, "Th2", gui);
        ThreadCalcule th3 = new ThreadCalcule(0, 49, mas, "Th3", gui);
        ThreadCalcule th4 = new ThreadCalcule(50, 99, mas, "Th4", gui);

        th1.start();
        th2.start();
        th3.start();
        th4.start();


        th1.join();
        th2.join();
        th3.join();
        th4.join();


        Thread.sleep(500);
        displayThreadRunning = false;
        try {
            guiDisplayQueue.put("STOP");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        gui.appendText("\nToate firele de execuție s-au încheiat.\n");
        System.out.println("\nToate firele de execuție s-au încheiat.");
    }

    private static void printArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
            if ((i + 1) % 10 == 0) {
                System.out.println();
            }
        }
        System.out.println();
    }


    private static void printWithDelayConsole(String text, int delay) {
        for (char c : text.toCharArray()) {
            System.out.print(c);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
    }


    private static synchronized void threadFinished() {
        finishedThreads++;
    }

    private static void waitForAllThreads() {
        while (finishedThreads < 4) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void displayInOrder(String threadName, String text) {
        while (currentDisplay < DISPLAY_ORDER.length &&
                !DISPLAY_ORDER[currentDisplay].equals(threadName)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Afișare în GUI - folosind coada pentru ordonare
        String guiText = threadName + ": " + text;
        try {
            guiDisplayQueue.put(guiText);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Afișare în consolă - cu delay
        System.out.print(threadName + ": ");
        printWithDelayConsole(text, 100);

        currentDisplay++;
    }

    public static void appendTextWithLock(String s) {
        synchronized (lock) {
            SwingUtilities.invokeLater(() -> gui.appendText(s));
        }
    }


    static class ThreadCalcule extends Thread {
        int startIndex, endIndex;
        int[] mas;
        String nameThread;
        Lab3GUI gui;

        public ThreadCalcule(int startIndex, int endIndex, int[] mas, String nameThread, Lab3GUI gui) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.mas = mas;
            this.nameThread = nameThread;
            this.gui = gui;
        }

        @Override
        public void run() {
            int S1 = 0, S2 = 0, k = 0;
            int count = 0;

            printToGUI(nameThread + " a început execuția pe intervalul [" + startIndex + ", " + endIndex + "]\n");

            for (int i = startIndex; i <= endIndex; i++) {
                try {
                    Thread.yield();
                    Thread.sleep((int) (Math.random() * 40 + 10));
                } catch (InterruptedException e) {
                    if (Thread.interrupted()) {
                        printToGUI(nameThread + " a fost întrerupt și se încheie.\n");
                        return;
                    }
                    e.printStackTrace();
                }

                if (isThreadActive()) {
                    if (mas[i] >= 120 && mas[i] <= 690 && mas[i] % 2 == 0) {
                        if (k == 0) {
                            S1 = mas[i];
                            k++;
                        } else {
                            S2 = mas[i];
                            int S = S1 + S2;
                            count++;
                            printToGUI(nameThread + " -> Suma " + count + ": " + S1 + " + " + S2 + " = " + S +
                                    " (poziții: " + findFirstPosition(S1, i) + ", " + i + ")\n");
                            S1 = S2 = 0;
                            k = 0;
                        }
                    }
                }
            }

            if (k == 1) {
                printToGUI(nameThread + " -> Valoare pară rămasă singură: " + S1 + " (poziție: " + findFirstPosition(S1, endIndex) + ")\n");
            }

            LaboratorThreads.appendTextWithLock(nameThread + " -> Total sume calculate: " + count + "\n");
            printToGUI(nameThread + " a terminat execuția.\n");

            threadFinished();
            waitForAllThreads();

            if (nameThread.equals("Th3")) {
                displayInOrder(nameThread, DISCIPLINA);
            } else if (nameThread.equals("Th4")) {
                displayInOrder(nameThread, GRUPA);
            }
        }

        private boolean isThreadActive() {
            Thread currentThread = Thread.currentThread();
            return currentThread.isAlive() && !currentThread.isInterrupted();
        }

        private void printToGUI(String text) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (Math.random() > 0.5) {
                SwingUtilities.invokeLater(() -> gui.appendText(text));
            } else {
                LaboratorThreads.appendTextWithLock(text);
            }
        }

        private int findFirstPosition(int value, int currentIndex) {
            for (int i = startIndex; i <= currentIndex; i++) {
                Thread.yield();
                if (mas[i] == value) {
                    return i;
                }
            }
            return currentIndex - 1;
        }
    }

    static class ThreadCalc extends Thread {
        int startIndex, endIndex;
        int[] mas;
        String nameThread;
        Lab3GUI gui;

        public ThreadCalc(int startIndex, int endIndex, int[] mas, String nameThread, Lab3GUI gui) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.mas = mas;
            this.nameThread = nameThread;
            this.gui = gui;
        }

        @Override
        public void run() {
            int S1 = 0, S2 = 0, k = 0;
            int count = 0;

            printToGUI(nameThread + " a început execuția pe intervalul [" + startIndex + ", " + endIndex + "]\n");

            for (int i = startIndex; i <= endIndex; i++) {
                try {
                    Thread.yield();
                    Thread.sleep((int) (Math.random() * 40 + 10));
                } catch (InterruptedException e) {
                    if (Thread.interrupted()) {
                        printToGUI(nameThread + " a fost întrerupt.\n");
                        return;
                    }
                    e.printStackTrace();
                }

                if (isThreadValid()) {
                    if (mas[i] >= 1000 && mas[i] <= 1567 && mas[i] % 2 == 0) {
                        if (k == 0) {
                            S1 = mas[i];
                            k++;
                        } else {
                            S2 = mas[i];
                            int S = S1 + S2;
                            count++;
                            printToGUI(nameThread + " -> Suma " + count + ": " + S1 + " + " + S2 + " = " + S +
                                    " (poziții: " + findFirstPosition(S1, i) + ", " + i + ")\n");
                            S1 = S2 = 0;
                            k = 0;
                        }
                    }
                }
            }

            if (k == 1) {
                printToGUI(nameThread + " -> Valoare pară rămasă singură: " + S1 + " (poziție: " + findFirstPosition(S1, endIndex) + ")\n");
            }

            LaboratorThreads.appendTextWithLock(nameThread + " -> Total sume calculate: " + count + "\n");
            printToGUI(nameThread + " a terminat execuția.\n");

            threadFinished();
            waitForAllThreads();

            if (nameThread.equals("Th1")) {
                displayInOrder(nameThread, PRENUME_STUDENT);
            } else if (nameThread.equals("Th2")) {
                displayInOrder(nameThread, NUME_STUDENT);
            }
        }

        private boolean isThreadValid() {
            return Thread.currentThread().isAlive();
        }

        private void printToGUI(String text) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (Math.random() > 0.5) {
                SwingUtilities.invokeLater(() -> gui.appendText(text));
            } else {
                LaboratorThreads.appendTextWithLock(text);
            }
        }

        private int findFirstPosition(int value, int currentIndex) {
            for (int i = startIndex; i <= currentIndex; i++) {
                Thread.yield();
                if (mas[i] == value) {
                    return i;
                }
            }
            return currentIndex - 1;
        }
    }
}