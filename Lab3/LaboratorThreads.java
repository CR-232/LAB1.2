import javax.swing.SwingUtilities;
import java.util.Random;

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

    public static void main(String[] args) throws InterruptedException {

        gui = new Lab3GUI("Laborator PCD - CR_232");

        System.out.println("=== LABORATOR CR-232 ===");
        System.out.println("Echipa: Cruc Maxim Cotoman si Cotoman Vadim\n");

        mas = new int[100];
        gui.appendText("Tablou generat (10 linii x 10 coloane):\n");
        Random rand = new Random();

        for (int i = 0; i < mas.length; i++) {
            mas[i] = rand.nextInt(1448) + 120;

            if (mas[i] < 1000) gui.appendText(" " + mas[i] + " ");
            else gui.appendText(mas[i] + " ");

            if ((i + 1) % 10 == 0) gui.appendText("\n");
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

        ThreadCalc th1 = new ThreadCalc(0, 99, mas, "Th1", gui);
        ThreadCalc th2 = new ThreadCalc(0, 99, mas, "Th2", gui);
        ThreadCalcule th3 = new ThreadCalcule(0, 99, mas, "Th3", gui);
        ThreadCalcule th4 = new ThreadCalcule(0, 99, mas, "Th4", gui);

        th1.start();
        th2.start();
        th3.start();
        th4.start();

        th1.join();
        th2.join();
        th3.join();
        th4.join();

        Thread.sleep(500);

        gui.appendText("\nToate firele de execuție s-au încheiat.\n");
        System.out.println("\nToate firele de execuție s-au încheiat.");
    }

    private static void printArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
            if ((i + 1) % 10 == 0) System.out.println();
        }
        System.out.println();
    }

    private static synchronized void threadFinished() {
        finishedThreads++;
    }

    private static void waitForAllThreads() {
        while (finishedThreads < 4) {
            try { Thread.sleep(10); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    private static void displayInOrder(String threadName, String text) {
        while (currentDisplay < DISPLAY_ORDER.length &&
                !DISPLAY_ORDER[currentDisplay].equals(threadName)) {
            try { Thread.sleep(10); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }

        SwingUtilities.invokeLater(() -> gui.appendText(threadName + ": "));

        for (char c : text.toCharArray()) {
            final String letter = String.valueOf(c);
            SwingUtilities.invokeLater(() -> gui.appendText(letter));
            try { Thread.sleep(100); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }

        SwingUtilities.invokeLater(() -> gui.appendText("\n"));

        currentDisplay++;
    }

    //CLASA ThreadCalc - MAXIM
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
            printToGUI(nameThread + " a început execuția.\n");

            int pos1 = -1, pos2 = -1, count = 0;

            if (nameThread.equals("Th1")) {
                for (int i = startIndex; i <= endIndex; i++) {
                    if (mas[i] % 2 == 0) {
                        if (pos1 == -1) pos1 = i;
                        else {
                            pos2 = i;
                            count++;
                            printToGUI(nameThread + " -> Suma pozițiilor " + count +
                                    ": " + pos1 + " + " + pos2 + " = " + (pos1 + pos2) + "\n");
                            pos1 = pos2 = -1;
                        }
                    }
                }
            }

            if (nameThread.equals("Th2")) {
                for (int i = endIndex; i >= startIndex; i--) {
                    if (mas[i] % 2 == 0) {
                        if (pos1 == -1) pos1 = i;
                        else {
                            pos2 = i;
                            count++;
                            printToGUI(nameThread + " -> Suma pozițiilor " + count +
                                    ": " + pos1 + " + " + pos2 + " = " + (pos1 + pos2) + "\n");
                            pos1 = pos2 = -1;
                        }
                    }
                }
            }

            printToGUI(nameThread + " -> Total sume calculate: " + count + "\n");
            printToGUI(nameThread + " a terminat execuția.\n");

            threadFinished();
            waitForAllThreads();

            if (nameThread.equals("Th1")) displayInOrder(nameThread, PRENUME_STUDENT);
            if (nameThread.equals("Th2")) displayInOrder(nameThread, NUME_STUDENT);
        }

        private void printToGUI(String text) {
            SwingUtilities.invokeLater(() -> gui.appendText(text));
        }
    }

    //CLASA ThreadCalcule - VADIM
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
            printToGUI(nameThread + " a început execuția.\n");

            if (nameThread.equals("Th3")) {
                for (int i = startIndex; i <= endIndex; i++) {
                    if (mas[i] >= 120 && mas[i] <= 690) {
                        printToGUI(nameThread + " -> " + mas[i] + " (poz: " + i + ")\n");
                    }
                }
            }

            if (nameThread.equals("Th4")) {
                for (int i = endIndex; i >= startIndex; i--) {
                    if (mas[i] >= 1000 && mas[i] <= 1567) {
                        printToGUI(nameThread + " -> " + mas[i] + " (poz: " + i + ")\n");
                    }
                }
            }

            printToGUI(nameThread + " a terminat execuția.\n");

            threadFinished();
            waitForAllThreads();

            if (nameThread.equals("Th3")) displayInOrder(nameThread, DISCIPLINA);
            if (nameThread.equals("Th4")) displayInOrder(nameThread, GRUPA);
        }

        private void printToGUI(String text) {
            SwingUtilities.invokeLater(() -> gui.appendText(text));
        }
    }
}